package com.example.mini_project_week6

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DestinationAdapter
    private lateinit var tvEmptyState: TextView
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvSavedCountries)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DestinationAdapter(requireContext(), mutableListOf(), { destination ->
            Toast.makeText(requireContext(), "Selected ${destination.name}", Toast.LENGTH_SHORT).show()
        }, isHomeList = true)
        recyclerView.adapter = adapter

        // Observe shared ViewModel
        viewModel.destinations.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateData(updatedList)
            tvEmptyState.visibility = if (updatedList.isEmpty()) View.VISIBLE else View.GONE
            recyclerView.visibility = if (updatedList.isEmpty()) View.GONE else View.VISIBLE
        }

        // Swipe-to-delete setup
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removed = adapter.getItem(position)
                adapter.removeItem(position)
                viewModel.removeDestination(removed)
                Toast.makeText(requireContext(), "${removed.name} removed!", Toast.LENGTH_SHORT).show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    paint.color = Color.parseColor("#F44336") // Base red
                    paint.alpha = 150 // 0 = fully transparent, 255 = fully solid

                    // Match cardView style (12dp radius)
                    val cornerRadius = 32f // px → adjust for density (12dp approx)
                    val background = RectF(
                        itemView.right + dX + 32f, // small padding inside
                        itemView.top + 16f,
                        itemView.right.toFloat() - 16f,
                        itemView.bottom.toFloat() - 16f
                    )
                    c.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                    // Draw "Delete" text inside rounded card
                    paint.color = Color.WHITE
                    paint.textSize = 42f
                    paint.textAlign = Paint.Align.CENTER
                    val x = background.centerX()
                    val y = background.centerY() - (paint.descent() + paint.ascent()) / 2
                    c.drawText("Delete", x, y, paint)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // 🔹 Handle back press to exit app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish() // exit app
            }
        })
    }
}
