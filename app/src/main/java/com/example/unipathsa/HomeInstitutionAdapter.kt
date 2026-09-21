package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HomeInstitutionAdapter(
    private val institutions: List<Institution>,
    private val onClick: (Institution) -> Unit
) : RecyclerView.Adapter<HomeInstitutionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgHomeInstitution)
        val name: TextView = view.findViewById(R.id.tvHomeInstitutionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_institution, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val institution = institutions[position]
        holder.name.text = institution.name
        Glide.with(holder.itemView.context)
            .load(institution.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)
        holder.itemView.setOnClickListener { onClick(institution) }
    }

    override fun getItemCount(): Int = institutions.size
}