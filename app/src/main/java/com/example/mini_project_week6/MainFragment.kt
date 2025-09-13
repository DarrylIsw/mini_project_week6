package com.example.mini_project_week6

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // 👇 search list adapter (semi-select allowed here)
        adapter = DestinationAdapter(requireContext(), destinationList, { destination ->
            Toast.makeText(
                requireContext(),
                "${destination.name} is ${if (destination.visited) "selected" else "not selected"}",
                Toast.LENGTH_SHORT
            ).show()
        }, isHomeList = false)

        recyclerView.adapter = adapter

        // swipe to remove only from search list
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                adapter.removeItem(vh.adapterPosition)
                viewModel.saveSearch(etSearch.text.toString(), destinationList.toList()) // ✅ keep state
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // ✅ FAB confirms semi-selected → visited
        fab.setOnClickListener {
            val selectedDestinations = destinationList.filter { it.isSelected }

            if (selectedDestinations.isNotEmpty()) {
                // ✅ Reset semi-selected state (grey) in search list
                selectedDestinations.forEach {
                    it.isSelected = false
                    // Do NOT set visited = true here
                }

                // Push to HomeFragment via shared ViewModel
                viewModel.addDestinations(selectedDestinations)

                // Save current search state
                viewModel.saveSearch(etSearch.text.toString(), destinationList.toList())

                // Refresh search list UI (remove grey highlight)
                adapter.notifyDataSetChanged()

                Toast.makeText(
                    requireContext(),
                    "Added ${selectedDestinations.size} countries to Bucket List!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "No country selected!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔄 Restore saved query + results if available
        viewModel.searchQuery.observe(viewLifecycleOwner) { savedQuery ->
            if (etSearch.text.isNullOrEmpty() && !savedQuery.isNullOrEmpty()) {
                etSearch.setText(savedQuery)
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) { savedResults ->
            if (destinationList.isEmpty() && savedResults.isNotEmpty()) {
                destinationList.addAll(savedResults)
                adapter.notifyDataSetChanged()
            }
        }

        // search input
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

        if (query.length == 1) {
            // 🔍 Local filter: show ALL countries containing the letter
            val filtered = CountryUtils.countries
                .filter { it.contains(query, ignoreCase = true) }
                .map { name ->
                    Destination(
                        name = name,
                        location = "",
                        visited = false,
                        isSelected = false
                    )
                }

            destinationList.addAll(filtered)
            adapter.notifyDataSetChanged()

            // ✅ Save search state
            viewModel.saveSearch(query, destinationList.toList())
        } else {
            // Use Wikipedia API for longer queries
            fetchWikipediaDestinations(query)
        }
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
                        visited = false,
                        isSelected = false
                    )
                }

                val filtered = apiDestinations.filter { dest ->
                    CountryUtils.countries.contains(dest.name)
                }

                activity?.runOnUiThread {
                    destinationList.clear()
                    destinationList.addAll(filtered)
                    adapter.notifyDataSetChanged()

                    // ✅ Save search state
                    viewModel.saveSearch(query, destinationList.toList())
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
