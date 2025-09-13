package com.example.mini_project_week6

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DestinationAdapter(
    private val context: Context,
    private val destinations: MutableList<Destination>,
    private val onItemClick: (Destination) -> Unit,
    private val isHomeList: Boolean = false // 👈 default = search, true = home
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position]
        holder.bind(destination)

        holder.itemView.setOnClickListener {
            if (!isHomeList) {
                // In search list → toggle semiSelected (grey highlight)
                destination.isSelected = !destination.isSelected
                notifyItemChanged(position)
                onItemClick(destination)
            } else {
                // In home list → just show toast or click action
                onItemClick(destination)
            }
        }
    }

    override fun getItemCount(): Int = destinations.size

    fun removeItem(position: Int) {
        destinations.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateData(newList: List<Destination>) {
        destinations.clear()
        destinations.addAll(newList)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Destination {
        return destinations[position]
    }

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvName)
        private val imageIv: ImageView = itemView.findViewById(R.id.ivDestination)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)

        fun bind(destination: Destination) {
            nameTv.text = destination.name

            Glide.with(context)
                .load(destination.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imageIv)

            if (isHomeList) {
                // ✅ Always white in HomeFragment
                cardView.setCardBackgroundColor(context.getColor(R.color.white))
            } else {
                // ✅ Grey if semi-selected in MainFragment
                if (destination.isSelected) {
                    cardView.setCardBackgroundColor(context.getColor(R.color.gray_visited))
                } else {
                    cardView.setCardBackgroundColor(context.getColor(R.color.white))
                }
            }
        }
    }
}
