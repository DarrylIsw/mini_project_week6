package com.example.mini_project_week6

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DestinationAdapter
    private val viewModel: SharedViewModel by activityViewModels() // 👈 shared VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvSavedCountries)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 👇 isHomeList = true → countries always white here
        adapter = DestinationAdapter(requireContext(), mutableListOf(), { destination ->
            Toast.makeText(requireContext(), "Selected ${destination.name}", Toast.LENGTH_SHORT).show()
        }, isHomeList = true)

        recyclerView.adapter = adapter

        // Observe ViewModel and block duplicates
        viewModel.destinations.observe(viewLifecycleOwner) { updatedList ->
            val unique = updatedList.distinctBy { it.name } // ✅ keep only unique country names
            adapter.updateData(unique)
        }
    }
}
