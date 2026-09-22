package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExploreCourseAdapter(private var courses: List<Course>,
                           private val onCourseClick: (Course) -> Unit) :
    RecyclerView.Adapter<ExploreCourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvExploreCourseName)
        val university: TextView = view.findViewById(R.id.tvExploreUniversity)
        val aps: TextView = view.findViewById(R.id.tvExploreAps)
        val duration: TextView = view.findViewById(R.id.tvExploreDuration)
        val category: TextView = view.findViewById(R.id.tvExploreCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name
        holder.university.text = course.university
        holder.aps.text = "APS ${course.apsRequired}+"
        holder.duration.text = course.duration
        holder.category.text = course.category

        holder.itemView.setOnClickListener { onCourseClick(course) }
    }

    override fun getItemCount(): Int = courses.size

    fun updateList(newList: List<Course>) {
        courses = newList
        notifyDataSetChanged()
    }
}