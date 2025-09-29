package com.example.mini_project_week6

// Android graphics + drawing tools
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

// Android UI + lifecycle
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

// Back press handling
import androidx.activity.OnBackPressedCallback

// Fragment + ViewModel
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

// RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Home screen that displays saved destinations
class HomeFragment : Fragment(R.layout.fragment_home) {

    // RecyclerView for displaying saved destinations
    private lateinit var recyclerView: RecyclerView

    // Adapter for handling destination list UI
    private lateinit var adapter: DestinationAdapter

    // TextView to display empty state message
    private lateinit var tvEmptyState: TextView

    // Shared ViewModel to access destinations across fragments
    private val viewModel: SharedViewModel by activityViewModels()

    // Called when fragment's view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.rvSavedCountries)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        // Setup RecyclerView with vertical list layout
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Setup adapter with empty list initially
        adapter = DestinationAdapter(
            requireContext(),
            mutableListOf(),
            { destination ->
                Toast.makeText(requireContext(), "Selected ${destination.name}", Toast.LENGTH_SHORT).show()
            },
            isHomeList = true // Special behavior for Home list
        )
        recyclerView.adapter = adapter

        // Observe changes in destinations from SharedViewModel
        viewModel.destinations.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateData(updatedList) // Refresh adapter data
            // Show empty state if no items
            tvEmptyState.visibility = if (updatedList.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (updatedList.isEmpty()) View.GONE else View.VISIBLE
        }

        // --- Swipe-to-delete setup ---
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // Not supporting drag & drop → return false
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            // Called when item is swiped left
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removed = adapter.getItem(position) // Get removed destination
                adapter.removeItem(position)            // Remove from adapter
                viewModel.removeDestination(removed)    // Remove from ViewModel
                Toast.makeText(requireContext(), "${removed.name} removed!", Toast.LENGTH_SHORT).show()
            }

            // Custom swipe UI (red rounded background + "Delete" text)
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    // Semi-transparent red background
                    paint.color = Color.parseColor("#F44336") // Base red
                    paint.alpha = 150

                    // Rounded rectangle background (similar to CardView style)
                    val cornerRadius = 32f // px (≈ 12dp)
                    val background = RectF(
                        itemView.right + dX + 32f, // leave padding
                        itemView.top + 16f,
                        itemView.right.toFloat() - 16f,
                        itemView.bottom.toFloat() - 16f
                    )
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                    // Draw "Delete" text inside the background
                    paint.color = Color.WHITE
                    paint.textSize = 42f
                    paint.textAlign = Paint.Align.CENTER
                    val x = background.centerX()
                    val y = background.centerY() - (paint.descent() + paint.ascent()) / 2
                    c.drawText("Delete", x, y, paint)
                }

                // Continue default swipe behavior
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        // Attach swipe handler to RecyclerView
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // --- Back button handling ---
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish() // Exit app completely
            }
        })
    }
}
