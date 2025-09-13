package com.example.mini_project_week6

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
    private val isHomeList: Boolean = false // false = MainFragment search list
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
                // MainFragment → toggle semi-selected
                destination.isSelected = !destination.isSelected
                notifyItemChanged(position)
                onItemClick(destination)
            } else {
                // HomeFragment → just show toast / click
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

    fun getItem(position: Int): Destination = destinations[position]

    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val imageIv: ImageView = itemView.findViewById(R.id.ivDestination)
        private val nameTv: TextView = itemView.findViewById(R.id.tvName)
        private val locationTv: TextView = itemView.findViewById(R.id.tvLocation)
        private val priceTv: TextView = itemView.findViewById(R.id.tvPrice)
        private val ivTick: ImageView = itemView.findViewById(R.id.ivTick)

        fun bind(destination: Destination) {
            nameTv.text = destination.name
            locationTv.text = destination.location
            priceTv.text = destination.price ?: ""

            Glide.with(context)
                .load(destination.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(imageIv)

            if (isHomeList) {
                // White background in HomeFragment
                cardView.setCardBackgroundColor(context.getColor(R.color.white))

                // Set tick drawable based on visited state
                ivTick.setImageResource(
                    if (destination.visited) R.drawable.ic_tick_filled
                    else R.drawable.ic_tick_empty
                )
                ivTick.visibility = View.VISIBLE

                ivTick.setOnClickListener {
                    destination.visited = !destination.visited

                    // Animate tick (fade in/out)
                    if (destination.visited) {
                        ivTick.setImageResource(R.drawable.ic_tick_filled)
                        ivTick.alpha = 0f
                        ivTick.animate().alpha(1f).setDuration(300).start()
                    } else {
                        ivTick.setImageResource(R.drawable.ic_tick_empty)
                        ivTick.animate().alpha(0f).setDuration(300).withEndAction {
                            ivTick.alpha = 1f
                        }.start()
                    }

                    // Animate card background
                    val fromColor = (cardView.background as? ColorDrawable)?.color ?: Color.WHITE
                    val toColor = if (destination.visited) context.getColor(R.color.green_faint) else context.getColor(R.color.white)
                    val colorAnim = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
                    colorAnim.duration = 300
                    colorAnim.addUpdateListener { animator ->
                        cardView.setCardBackgroundColor(animator.animatedValue as Int)
                    }
                    colorAnim.start()

                    // Move visited item to top
                    moveVisitedToTop(adapterPosition)
                }

            } else {
                // Grey if semi-selected in MainFragment
                cardView.setCardBackgroundColor(
                    if (destination.isSelected) context.getColor(R.color.gray_visited)
                    else context.getColor(R.color.white)
                )
                ivTick.visibility = View.GONE
            }
        }
    }

    private fun moveVisitedToTop(position: Int) {
        val dest = destinations.removeAt(position)
        destinations.add(0, dest)
        notifyItemMoved(position, 0)
    }
}
