package com.example.unipathsa


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView

class InstitutionAdapter(
    private var institutions: List<Institution>,
    private val onInstitutionClick: (Institution) -> Unit
) : RecyclerView.Adapter<InstitutionAdapter.InstitutionViewHolder>() {

    class InstitutionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.imgInstitution)

        val name: TextView =
            itemView.findViewById(R.id.tvInstitutionName)

        val province: TextView =
            itemView.findViewById(R.id.tvProvince)

        val type: TextView =
            itemView.findViewById(R.id.tvInstitutionType)

        val viewButton: Button =
            itemView.findViewById(R.id.btnViewInstitution)

        val favouriteButton: ImageButton =
            itemView.findViewById(R.id.btnFavourite)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InstitutionViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_institution, parent, false)

        return InstitutionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: InstitutionViewHolder,
        position: Int
    ) {

        val institution = institutions[position]

        holder.name.text = institution.name
        holder.province.text = institution.province
        holder.type.text = institution.type

        Glide.with(holder.itemView.context)
            .load(institution.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.viewButton.setOnClickListener {
            onInstitutionClick(institution)
        }

        holder.itemView.setOnClickListener {
            onInstitutionClick(institution)
        }

        holder.favouriteButton.setOnClickListener {
            holder.favouriteButton.setImageResource(
                android.R.drawable.btn_star_big_on
            )
        }
    }

    override fun getItemCount(): Int {
        return institutions.size
    }

    fun updateList(newList: List<Institution>) {
        institutions = newList
        notifyDataSetChanged()
    }
}