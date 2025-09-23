package com.example.mini_project_week6

// Android imports for context, views, layouts, and RecyclerView components
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

// RecyclerView Adapter class for displaying a list of continents
class ContinentAdapter(
    private val context: Context,                   // Context for inflating layouts and accessing resources
    private val continents: List<String>,           // List of continent names to display
    private val onClick: (String) -> Unit           // Lambda callback triggered when a continent item is clicked
) : RecyclerView.Adapter<ContinentAdapter.ContinentViewHolder>() {

    // Called when RecyclerView needs a new ViewHolder (inflate the item layout)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContinentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_continent, parent, false) // Inflate single item layout
        return ContinentViewHolder(view) // Return the custom ViewHolder
    }

    // Binds data to the ViewHolder at the given position
    override fun onBindViewHolder(holder: ContinentViewHolder, position: Int) {
        val continent = continents[position]           // Get the current continent name
        holder.bind(continent)                         // Bind the data to the ViewHolder UI
        holder.itemView.setOnClickListener { onClick(continent) } // Set click listener to trigger callback with the continent
    }

    // Returns the total number of items (continents) in the list
    override fun getItemCount(): Int = continents.size

    // Custom ViewHolder class that holds and binds UI for each item
    inner class ContinentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTv: TextView = itemView.findViewById(R.id.tvContinentName) // TextView to display continent name
        private val imageIv: ImageView = itemView.findViewById(R.id.ivContinent)   // ImageView to display continent icon

        // Bind continent data (name + icon) to the item views
        fun bind(continent: String) {
            nameTv.text = continent // Set continent name to TextView

            // Select appropriate icon for each continent
            val iconRes = when (continent) {
                "Asia" -> R.drawable.ic_asia
                "Europe" -> R.drawable.ic_europe
                "Africa" -> R.drawable.ic_africa
                "Americas" -> R.drawable.ic_americas  // icon for Americas
                "Oceania" -> R.drawable.ic_oceania
                else -> R.drawable.ic_continent_placeholder // fallback icon if no match
            }

            // Set the ImageView resource
            imageIv.setImageResource(iconRes)
        }
    }
}
