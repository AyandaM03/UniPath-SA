package com.example.unipathsa

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class InstitutionAdapter(
    private var institutions: List<Institution>,
    private val onInstitutionClick: (Institution) -> Unit
) : RecyclerView.Adapter<InstitutionAdapter.InstitutionViewHolder>() {

    private companion object {
        private const val TAG = "InstitutionAdapter"
    }

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    class InstitutionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgInstitution)
        val tvFallbackInitial: TextView = itemView.findViewById(R.id.tvFallbackInitial)
        val name: TextView = itemView.findViewById(R.id.tvInstitutionName)
        val province: TextView = itemView.findViewById(R.id.tvProvince)
        val type: TextView = itemView.findViewById(R.id.tvInstitutionType)
        val tvFreeBadge: TextView = itemView.findViewById(R.id.tvFreeToApplyBadge)
        val viewButton: Button = itemView.findViewById(R.id.btnViewInstitution)
        val favouriteButton: ImageButton = itemView.findViewById(R.id.btnFavourite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstitutionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_institution, parent, false)
        return InstitutionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstitutionViewHolder, position: Int) {
        val institution = institutions[position]

        holder.name.text = institution.name
        holder.province.text = if (institution.province.isNotEmpty()) institution.province else "South Africa"
        holder.type.text = if (institution.type.isNotEmpty()) institution.type else "University"

        // Show/hide Free to Apply badge
        holder.tvFreeBadge.visibility = if (institution.freeToApply) View.VISIBLE else View.GONE

        // Prepare fallback initial letter
        val initial = if (institution.name.isNotBlank()) institution.name.trim().take(1).uppercase() else "U"
        holder.tvFallbackInitial.text = initial

        val rawUrl = institution.imageUrl.trim()
        val logoUrl = if (rawUrl.startsWith("http://")) "https://" + rawUrl.substring(7)
                      else if (rawUrl.isNotBlank() && !rawUrl.startsWith("https://")) "https://$rawUrl"
                      else rawUrl

        if (logoUrl.isNotBlank()) {
            holder.tvFallbackInitial.visibility = View.GONE
            holder.image.visibility = View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(logoUrl)
                .fitCenter()
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, "Failed to load logo for ${institution.name} from URL: $logoUrl", e)
                        holder.image.visibility = View.GONE
                        holder.tvFallbackInitial.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "Successfully loaded logo for ${institution.name}")
                        holder.tvFallbackInitial.visibility = View.GONE
                        holder.image.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(holder.image)
        } else {
            Log.d(TAG, "No logo URL provided for ${institution.name}, showing initial '$initial'")
            holder.image.visibility = View.GONE
            holder.tvFallbackInitial.visibility = View.VISIBLE
        }

        // Check whether institution is saved in user favourites
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("favourites").document(institution.id)
                .get()
                .addOnSuccessListener { doc ->
                    holder.favouriteButton.setImageResource(
                        if (doc.exists()) android.R.drawable.btn_star_big_on
                        else android.R.drawable.btn_star_big_off
                    )
                }
        }

        // Click listeners on logo, button and card
        holder.image.setOnClickListener { onInstitutionClick(institution) }
        holder.tvFallbackInitial.setOnClickListener { onInstitutionClick(institution) }
        holder.viewButton.setOnClickListener { onInstitutionClick(institution) }
        holder.itemView.setOnClickListener { onInstitutionClick(institution) }

        // Bookmark button click listener
        holder.favouriteButton.setOnClickListener {
            if (userId == null) return@setOnClickListener

            val favRef = db.collection("users").document(userId)
                .collection("favourites").document(institution.id)

            favRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    favRef.delete()
                    holder.favouriteButton.setImageResource(android.R.drawable.btn_star_big_off)
                } else {
                    favRef.set(institution)
                    holder.favouriteButton.setImageResource(android.R.drawable.btn_star_big_on)
                }
            }
        }
    }

    override fun getItemCount(): Int = institutions.size

    fun updateList(newList: List<Institution>) {
        institutions = newList
        notifyDataSetChanged()
    }
}