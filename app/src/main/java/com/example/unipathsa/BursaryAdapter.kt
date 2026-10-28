package com.example.unipathsa

import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BursaryAdapter(
    private var bursaries: List<Bursary>,
    private val activity: Activity,
    private val onBursaryClick: (Bursary) -> Unit
) : RecyclerView.Adapter<BursaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvBursaryName)
        val provider: TextView = view.findViewById(R.id.tvBursaryProvider)
        val amount: TextView = view.findViewById(R.id.tvBursaryAmount)
        val category: TextView = view.findViewById(R.id.tvBursaryCategory)
        val closing: TextView = view.findViewById(R.id.tvBursaryClosing)
        val btnBookmark: ImageView = view.findViewById(R.id.btnBookmarkBursary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bursary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bursary = bursaries[position]
        holder.name.text = bursary.name.ifBlank { "Untitled bursary" }
        holder.provider.text = bursary.provider.ifBlank { "Education Trust" }
        holder.amount.text = bursary.amount.ifBlank { "Full Tuition" }
        holder.category.text = bursary.category.ifBlank { "General" }

        val closingText = bursary.closingDate.ifBlank { "31 Oct 2025" }
        holder.closing.text = "Closes $closingText"

        // Format Closing Date Chip color (amber if < 30 days, red if closed/< 7 days)
        val daysRemaining = getDaysUntilClosing(closingText)
        when {
            daysRemaining < 0 -> {
                holder.closing.text = "Closed $closingText"
                holder.closing.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_rejected_bg))
                holder.closing.setTextColor(ContextCompat.getColor(activity, R.color.status_rejected_fg))
            }
            daysRemaining <= 14 -> {
                holder.closing.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_rejected_bg))
                holder.closing.setTextColor(ContextCompat.getColor(activity, R.color.status_rejected_fg))
            }
            daysRemaining <= 30 -> {
                holder.closing.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_review_bg))
                holder.closing.setTextColor(ContextCompat.getColor(activity, R.color.status_review_fg))
            }
            else -> {
                holder.closing.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_submitted_bg))
                holder.closing.setTextColor(ContextCompat.getColor(activity, R.color.status_submitted_fg))
            }
        }

        // Bookmark state
        BookmarkHelper.isSaved(bursary.id) { isSaved ->
            BookmarkHelper.updateBookmarkIcon(holder.btnBookmark, isSaved)
        }

        val itemDataMap = mapOf<String, Any>(
            "id" to bursary.id,
            "title" to bursary.name,
            "name" to bursary.name,
            "provider" to bursary.provider,
            "amount" to bursary.amount,
            "category" to bursary.category,
            "closingDate" to bursary.closingDate,
            "type" to "bursary"
        )

        holder.btnBookmark.setOnClickListener {
            BookmarkHelper.toggleBookmark(
                activity = activity,
                anchorView = holder.itemView,
                bookmarkImageView = holder.btnBookmark,
                itemId = bursary.id,
                itemType = "bursary",
                itemData = itemDataMap
            )
        }

        holder.itemView.setOnClickListener { onBursaryClick(bursary) }
    }

    override fun getItemCount(): Int = bursaries.size

    fun updateList(newList: List<Bursary>) {
        bursaries = newList
        notifyDataSetChanged()
    }

    private fun getDaysUntilClosing(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val date = format.parse(dateStr) ?: return 45
            val diffMs = date.time - System.currentTimeMillis()
            diffMs / (1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            30L
        }
    }
}