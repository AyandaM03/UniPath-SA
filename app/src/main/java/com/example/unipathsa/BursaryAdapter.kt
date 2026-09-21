package com.example.unipathsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BursaryAdapter(private var bursaries: List<Bursary>) :
    RecyclerView.Adapter<BursaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvBursaryName)
        val provider: TextView = view.findViewById(R.id.tvBursaryProvider)
        val amount: TextView = view.findViewById(R.id.tvBursaryAmount)
        val category: TextView = view.findViewById(R.id.tvBursaryCategory)
        val closing: TextView = view.findViewById(R.id.tvBursaryClosing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bursary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bursary = bursaries[position]
        holder.name.text = bursary.name
        holder.provider.text = bursary.provider
        holder.amount.text = bursary.amount
        holder.category.text = bursary.category
        holder.closing.text = "Closes: ${bursary.closingDate}"
    }

    override fun getItemCount(): Int = bursaries.size

    fun updateList(newList: List<Bursary>) {
        bursaries = newList
        notifyDataSetChanged()
    }
}