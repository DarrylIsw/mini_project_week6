package com.example.mini_project_week6

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.fragment.app.activityViewModels

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DestinationAdapter
    private val destinationList = mutableListOf<Destination>()
    private lateinit var etSearch: TextInputEditText
    private lateinit var fab: FloatingActionButton

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        etSearch = view.findViewById(R.id.etSearch)
        fab = view.findViewById(R.id.fabAdd)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 👇 Search list adapter (not home, so allow semi-select grey)
        adapter = DestinationAdapter(requireContext(), destinationList, { destination ->
            Toast.makeText(
                requireContext(),
                "${destination.name} is ${if (destination.visited) "visited" else "not visited"}",
                Toast.LENGTH_SHORT
            ).show()
        }, isHomeList = false)

        recyclerView.adapter = adapter

        // Swipe to remove from search list (not from HomeFragment)
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                adapter.removeItem(vh.adapterPosition)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // ✅ FAB confirms semi-selected → visited
        fab.setOnClickListener {
            val selectedDestinations = destinationList.filter { it.isSelected }

            if (selectedDestinations.isNotEmpty()) {
                selectedDestinations.forEach {
                    it.visited = true     // mark visited
                    it.isSelected = false // reset grey state
                }

                // Push confirmed visited items to shared VM → HomeFragment
                viewModel.addDestinations(selectedDestinations)

                // Refresh search list UI (remove grey)
                adapter.notifyDataSetChanged()

                Toast.makeText(
                    requireContext(),
                    "Added ${selectedDestinations.size} countries to visited!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "No country selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle search input
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) startSearch(query)
                true
            } else false
        }
    }

    private fun startSearch(query: String) {
        destinationList.clear()
        adapter.notifyDataSetChanged()
        fetchWikipediaDestinations(query)
    }

    private fun fetchWikipediaDestinations(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = WikiRetrofitClient.instance.searchDestinations(query)

                val titles = response[1] as List<String>
                val descriptions = response[2] as List<String>

                val apiDestinations = titles.mapIndexed { index, title ->
                    Destination(
                        name = title,
                        location = descriptions.getOrNull(index) ?: "",
                        price = "",
                        imageUrl = "",
                        visited = false,
                        isSelected = false
                    )
                }

                // Only show valid countries
                val filtered = apiDestinations.filter { dest ->
                    CountryUtils.countries.contains(dest.name)
                }

                activity?.runOnUiThread {
                    destinationList.clear()
                    destinationList.addAll(filtered)
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
