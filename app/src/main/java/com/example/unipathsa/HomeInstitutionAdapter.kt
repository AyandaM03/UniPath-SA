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
        val logo: ImageView = view.findViewById(R.id.ivHomeInstLogo)
        val fallback: TextView = view.findViewById(R.id.tvHomeInstFallback)
        val name: TextView = view.findViewById(R.id.tvHomeInstitutionName)
        val province: TextView = view.findViewById(R.id.tvHomeInstProvince)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_institution, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val institution = institutions[position]
        holder.name.text = institution.name.ifBlank { "Untitled Institution" }

        val locationText = when {
            institution.province.isNotBlank() && institution.type.isNotBlank() -> "${institution.province} • ${institution.type}"
            institution.province.isNotBlank() -> institution.province
            institution.type.isNotBlank() -> institution.type
            else -> "Public University"
        }
        holder.province.text = locationText

        val initial = institution.name.trim().take(1).uppercase().ifBlank { "U" }
        holder.fallback.text = initial

        if (institution.imageUrl.isNotBlank()) {
            holder.logo.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(institution.imageUrl)
                .centerInside()
                .into(holder.logo)
        } else {
            holder.logo.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(institution) }
    }

    override fun getItemCount(): Int = institutions.size
}