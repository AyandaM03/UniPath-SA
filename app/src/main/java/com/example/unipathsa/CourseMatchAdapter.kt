package com.example.unipathsa

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CourseMatchAdapter(private var courses: List<Course>) :
    RecyclerView.Adapter<CourseMatchAdapter.ViewHolder>() {

    private companion object {
        private const val TAG = "CourseMatchAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCourseName: TextView? = view.findViewById(R.id.tvCourseName)
        val tvUniversity: TextView? = view.findViewById(R.id.tvUniversity)
        val tvApsRequired: TextView? = view.findViewById(R.id.tvApsRequired)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val course = courses[position]
            val courseName = course.name.ifBlank { "Untitled course" }
            val instName = course.institution.ifBlank { course.university.ifBlank { "University" } }
            val fieldName = course.field.ifBlank { course.category.ifBlank { "General" } }

            holder.tvCourseName?.text = courseName
            holder.tvUniversity?.text = "$instName • $fieldName"
            holder.tvApsRequired?.text = "APS required: ${course.apsRequirement}"

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, CourseDetailActivity::class.java)
                intent.putExtra("COURSE_DATA", course)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding CourseMatch item at position $position", e)
        }
    }

    override fun getItemCount(): Int = courses.size

    fun updateList(newList: List<Course>) {
        courses = newList
        notifyDataSetChanged()
    }
}