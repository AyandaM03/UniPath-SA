package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeCourseAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<HomeCourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvHomeCourseName)
        val university: TextView = view.findViewById(R.id.tvHomeCourseUni)
        val aps: TextView = view.findViewById(R.id.tvHomeCourseAps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_course, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.name.text = course.name
        holder.university.text = course.university
        holder.aps.text = "APS ${course.apsRequired}+"
    }

    override fun getItemCount(): Int = courses.size
}