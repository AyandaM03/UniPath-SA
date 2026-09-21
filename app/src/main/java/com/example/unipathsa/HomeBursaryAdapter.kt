package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeBursaryAdapter(private val bursaries: List<Bursary>) :
    RecyclerView.Adapter<HomeBursaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvHomeBursaryName)
        val provider: TextView = view.findViewById(R.id.tvHomeBursaryProvider)
        val closing: TextView = view.findViewById(R.id.tvHomeBursaryClosing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_bursary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bursary = bursaries[position]
        holder.name.text = bursary.name
        holder.provider.text = bursary.provider
        holder.closing.text = "Closes: ${bursary.closingDate}"
    }

    override fun getItemCount(): Int = bursaries.size
}