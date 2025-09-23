package com.example.mini_project_week6

// Animation + color imports
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

// UI + RecyclerView imports
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

// Glide for image loading
import com.bumptech.glide.Glide

// RecyclerView Adapter for displaying list of Destination objects
class DestinationAdapter(
    private val context: Context,                       // Context for inflating layouts, accessing colors, etc.
    private val destinations: MutableList<Destination>, // Mutable list of destinations to display and modify
    private val onItemClick: (Destination) -> Unit,     // Callback when a destination item is clicked
    private val isHomeList: Boolean = false             // Flag: true = used in HomeFragment, false = used in MainFragment search
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    // Inflate layout and create ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        val destination = destinations[position] // Get destination at position
        holder.bind(destination)                 // Bind to UI

        // Handle item click differently depending on fragment type
        holder.itemView.setOnClickListener {
            if (!isHomeList) {
                // MainFragment → toggle semi-selected state
                destination.isSelected = !destination.isSelected
                notifyItemChanged(position)      // Update UI for changed item
                onItemClick(destination)         // Notify click listener
            } else {
                // HomeFragment → just trigger click listener (toast / details)
                onItemClick(destination)
            }
        }
    }

    // Number of items
    override fun getItemCount(): Int = destinations.size

    // Remove item from list
    fun removeItem(position: Int) {
        destinations.removeAt(position)
        notifyItemRemoved(position)
    }

    // Replace data with a new list
    fun updateData(newList: List<Destination>) {
        destinations.clear()
        destinations.addAll(newList)
        notifyDataSetChanged()
    }

    // Get a single item by position
    fun getItem(position: Int): Destination = destinations[position]

    // ViewHolder class for managing item views
    inner class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)               // Root card
        private val imageIv: ImageView = itemView.findViewById(R.id.ivDestination)         // Flag image
        private val nameTv: TextView = itemView.findViewById(R.id.tvName)                  // Destination name
        private val ivTick: ImageView = itemView.findViewById(R.id.ivTick)                 // Tick icon for visited state
        private val continentCapitalTv: TextView = itemView.findViewById(R.id.tvContinentCapital) // Continent + capital
        private val populationTv: TextView = itemView.findViewById(R.id.tvPopulation)      // Population text

        // Bind destination data into views
        fun bind(destination: Destination) {
            // Name
            nameTv.text = destination.name

            // Flag image (load with Glide)
            Glide.with(context)
                .load(destination.imageUrl)
                .placeholder(R.drawable.flag_placeholder_bg) // while loading
                .error(R.drawable.flag_placeholder_bg)       // if error
                .into(imageIv)

            // Continent + Capital
            continentCapitalTv.text = "${destination.continent} • ${destination.capital}"

            // Population (formatted with commas, fallback if unknown)
            populationTv.text = if (destination.population > 0) {
                "Population: %,d".format(destination.population)
            } else {
                "Population: Unknown"
            }

            if (isHomeList) {
                // --- HomeFragment behavior ---
                // Show visited state

                // Background color based on visited state
                cardView.setCardBackgroundColor(
                    if (destination.visited) context.getColor(R.color.green_faint)
                    else context.getColor(R.color.white)
                )

                // Tick icon based on visited state
                ivTick.setImageResource(
                    if (destination.visited) R.drawable.ic_tick_filled
                    else R.drawable.ic_tick_empty
                )
                ivTick.visibility = View.VISIBLE

                // Handle tick icon click → toggle visited
                ivTick.setOnClickListener {
                    destination.visited = !destination.visited // Flip state

                    // Update tick drawable
                    ivTick.setImageResource(
                        if (destination.visited) R.drawable.ic_tick_filled
                        else R.drawable.ic_tick_empty
                    )

                    // Animate background color change
                    val fromColor = (cardView.background as? ColorDrawable)?.color ?: Color.WHITE
                    val toColor = if (destination.visited) context.getColor(R.color.green_faint) else context.getColor(R.color.white)
                    ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
                        duration = 300
                        addUpdateListener { animator ->
                            cardView.setCardBackgroundColor(animator.animatedValue as Int)
                        }
                        start()
                    }

                    // Reorder item: visited items move up, unvisited go down
                    if (destination.visited) moveVisitedToTops(adapterPosition)
                    else moveVisitedToBottoms(adapterPosition)
                }

            } else {
                // --- MainFragment search behavior ---
                // Highlight selected card with gray
                cardView.setCardBackgroundColor(
                    if (destination.isSelected) context.getColor(R.color.gray_visited)
                    else context.getColor(R.color.white)
                )
                ivTick.visibility = View.GONE // Hide tick in search mode
            }
        }
    }

    // --- Helpers for reordering destinations in HomeFragment ---

    private fun moveVisitedToTops(position: Int) {
        val dest = destinations.removeAt(position)
        destinations.add(0, dest) // move to top
        notifyItemMoved(position, 0)
    }

    private fun moveVisitedToBottoms(position: Int) {
        val item = destinations.removeAt(position)
        destinations.add(item) // move to end
        notifyItemMoved(position, destinations.lastIndex)
    }

    // Extra versions (not used directly but available)
    private fun moveVisitedToBottom(position: Int) {
        val item = destinations[position]
        destinations.removeAt(position)
        destinations.add(item)
        notifyItemMoved(position, destinations.lastIndex)
    }

    private fun moveVisitedToTop(position: Int) {
        val dest = destinations.removeAt(position)
        destinations.add(0, dest)
        notifyItemMoved(position, 0)
    }
}
