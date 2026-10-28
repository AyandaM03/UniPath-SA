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

class ExploreCourseAdapter(
    private var courses: List<Course>,
    private val activity: Activity,
    private var userAps: Int = 0,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<ExploreCourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvExploreCourseName)
        val university: TextView = view.findViewById(R.id.tvExploreUniversity)
        val badge: TextView = view.findViewById(R.id.tvEligibilityBadge)
        val aps: TextView = view.findViewById(R.id.tvExploreAps)
        val duration: TextView = view.findViewById(R.id.tvExploreDuration)
        val category: TextView = view.findViewById(R.id.tvExploreCategory)
        val btnBookmark: ImageView = view.findViewById(R.id.btnBookmarkCourse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name.ifBlank { "Untitled course" }
        holder.university.text = course.institution.ifBlank { "South African University" }
        holder.aps.text = "APS ${course.apsRequirement}"
        holder.duration.text = course.duration.ifBlank { "3 Years" }
        holder.category.text = course.field.ifBlank { "General" }

        // Qualification Badge
        if (userAps > 0 && course.apsRequirement > 0) {
            holder.badge.visibility = View.VISIBLE
            if (userAps >= course.apsRequirement) {
                holder.badge.text = "✔ You qualify"
                holder.badge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_accepted_bg))
                holder.badge.setTextColor(ContextCompat.getColor(activity, R.color.status_accepted_fg))
            } else {
                val diff = course.apsRequirement - userAps
                holder.badge.text = "⚠ $diff pts short"
                holder.badge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.status_review_bg))
                holder.badge.setTextColor(ContextCompat.getColor(activity, R.color.status_review_fg))
            }
        } else {
            holder.badge.visibility = View.GONE
        }

        // Bookmark state
        BookmarkHelper.isSaved(course.id) { isSaved ->
            BookmarkHelper.updateBookmarkIcon(holder.btnBookmark, isSaved)
        }

        val itemDataMap = mapOf<String, Any>(
            "id" to course.id,
            "title" to course.name,
            "name" to course.name,
            "institution" to course.institution,
            "category" to course.field,
            "apsRequirement" to course.apsRequirement,
            "type" to "course"
        )

        holder.btnBookmark.setOnClickListener {
            BookmarkHelper.toggleBookmark(
                activity = activity,
                anchorView = holder.itemView,
                bookmarkImageView = holder.btnBookmark,
                itemId = course.id,
                itemType = "course",
                itemData = itemDataMap
            )
        }

        holder.itemView.setOnClickListener { onCourseClick(course) }
    }

    override fun getItemCount(): Int = courses.size

    fun updateList(newList: List<Course>, newAps: Int = userAps) {
        courses = newList
        userAps = newAps
        notifyDataSetChanged()
    }
}