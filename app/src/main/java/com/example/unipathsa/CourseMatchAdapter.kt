package com.example.unipathsa


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseMatchAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<CourseMatchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvUniversity: TextView = view.findViewById(R.id.tvUniversity)
        val tvApsRequired: TextView = view.findViewById(R.id.tvApsRequired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val course = courses[position]
        holder.tvCourseName.text = course.name
        holder.tvUniversity.text = "${course.university} • ${course.faculty}"
        holder.tvApsRequired.text = "APS required: ${course.apsRequired}"
    }

    override fun getItemCount(): Int = courses.size
}