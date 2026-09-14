package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UniversityAdapter(
    private val universities: List<University>,
    private val onDetailsClick: (University) -> Unit
) : RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder>() {

    class UniversityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo: ImageView = view.findViewById(R.id.ivUniLogo)
        val tvName: TextView = view.findViewById(R.id.tvUniName)
        val tvLocation: TextView = view.findViewById(R.id.tvUniLocation)
        val tvShortDesc: TextView = view.findViewById(R.id.tvUniShortDesc)
        val btnDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_university, parent, false)
        return UniversityViewHolder(view)
    }

    override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
        val uni = universities[position]
        holder.tvName.text = uni.name
        holder.tvLocation.text = uni.location
        holder.tvShortDesc.text = uni.description
        holder.ivLogo.setImageResource(uni.logoResId)

        holder.btnDetails.setOnClickListener {
            onDetailsClick(uni)
        }
    }

    override fun getItemCount() = universities.size
}
