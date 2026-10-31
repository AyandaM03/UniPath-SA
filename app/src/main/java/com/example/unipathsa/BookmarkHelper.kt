package com.example.unipathsa

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object BookmarkHelper {

    private const val TAG = "BookmarkHelper"

    /**
     * Checks if an item is currently saved in users/{uid}/savedItems
     */
    fun isSaved(itemId: String, onResult: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty() || itemId.isEmpty()) {
            onResult(false)
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).collection("savedItems").document(itemId)
            .get()
            .addOnSuccessListener { doc ->
                val saved = doc != null && doc.exists()
                onResult(saved)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking saved status for $itemId", e)
                onResult(false)
            }
    }

    /**
     * Toggles bookmark state in Firestore, plays animation, triggers haptic feedback,
     * and shows a Snackbar with an Undo action. If operation fails, reverts state.
     */
    fun toggleBookmark(
        activity: Activity,
        anchorView: View,
        bookmarkImageView: ImageView?,
        itemId: String,
        itemType: String, // "course", "institution", "bursary"
        itemData: Map<String, Any>,
        onStateChanged: ((Boolean) -> Unit)? = null
    ) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Snackbar.make(anchorView, "Please log in to save items", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (itemId.isEmpty()) {
            Log.w(TAG, "Cannot save item with empty ID")
            return
        }

        // Play feedback animation & haptic tap
        anchorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        animateBookmarkButton(bookmarkImageView ?: anchorView)

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(uid).collection("savedItems").document(itemId)

        docRef.get().addOnSuccessListener { snapshot ->
            val isCurrentlySaved = snapshot != null && snapshot.exists()

            if (isCurrentlySaved) {
                // Optimistically update UI to unsaved
                updateBookmarkIcon(bookmarkImageView, false)
                onStateChanged?.invoke(false)

                // Delete document
                docRef.delete().addOnSuccessListener {
                    Log.d(TAG, "Removed item $itemId from savedItems")
                    Snackbar.make(anchorView, "Removed from Saved Items", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            // Re-save on Undo
                            docRef.set(itemData).addOnSuccessListener {
                                updateBookmarkIcon(bookmarkImageView, true)
                                onStateChanged?.invoke(true)
                            }
                        }.show()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to remove saved item $itemId", e)
                    // Revert icon on failure
                    updateBookmarkIcon(bookmarkImageView, true)
                    onStateChanged?.invoke(true)
                    Toast.makeText(activity, "Failed to remove item: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Optimistically update UI to saved
                updateBookmarkIcon(bookmarkImageView, true)
                onStateChanged?.invoke(true)

                val payload = HashMap(itemData)
                payload["id"] = itemId
                payload["type"] = itemType
                payload["savedAt"] = System.currentTimeMillis()

                docRef.set(payload).addOnSuccessListener {
                    Log.d(TAG, "Saved item $itemId to savedItems")

                    // NEW: notify when a bursary or institution is saved
                    if (itemType == "bursary" || itemType == "institution") {
                        val itemName = (itemData["name"] as? String) ?: "Item"
                        val label = if (itemType == "bursary") "Bursary" else "Institution"
                        NotificationHelper.show(activity, "$label saved", "$itemName was added to your saved list")
                    }

                    Snackbar.make(anchorView, "Saved to Saved Items", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            // Delete on Undo
                            docRef.delete().addOnSuccessListener {
                                updateBookmarkIcon(bookmarkImageView, false)
                                onStateChanged?.invoke(false)
                            }
                        }.show()
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save item $itemId", e)
                    // Revert icon on failure
                    updateBookmarkIcon(bookmarkImageView, false)
                    onStateChanged?.invoke(false)
                    Toast.makeText(activity, "Failed to save item: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error checking saved item doc $itemId", e)
        }
    }

    /**
     * Updates bookmark ImageView icon state (filled vs outline)
     */
    fun updateBookmarkIcon(imageView: ImageView?, isSaved: Boolean) {
        if (imageView == null) return
        val iconRes = if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        imageView.setImageResource(iconRes)
        imageView.setColorFilter(
            if (isSaved) ContextCompat.getColor(imageView.context, R.color.primary)
            else ContextCompat.getColor(imageView.context, R.color.text_tertiary)
        )
    }

    // Scale animation helper
    private fun animateBookmarkButton(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f, 1.25f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f, 1.25f, 1.0f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 250
            start()
        }
    }
}