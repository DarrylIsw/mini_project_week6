package com.example.mini_project_week6

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DestinationAdapter
    private val destinationList = mutableListOf<Destination>()
    private lateinit var etSearch: TextInputEditText
    private lateinit var fab: FloatingActionButton

    // Continent recycler and adapter
    private lateinit var recyclerContinents: RecyclerView
    private lateinit var continentAdapter: ContinentAdapter
    private val continents = listOf("Asia", "Europe", "Africa", "North America", "South America", "Oceania")
    private val allCountries = mutableListOf<Destination>() // store all loaded countries

    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        etSearch = view.findViewById(R.id.etSearch)
        fab = view.findViewById(R.id.fabAdd)

        recyclerContinents = view.findViewById(R.id.recyclerContinents)

        recyclerContinents.layoutManager = GridLayoutManager(requireContext(), 3) // 2x3 grid
        continentAdapter = ContinentAdapter(requireContext(), continents) { continent ->
            showCountriesForContinent(continent)
        }
        recyclerContinents.adapter = continentAdapter

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DestinationAdapter(requireContext(), destinationList, { destination ->
            Toast.makeText(
                requireContext(),
                "${destination.name} is ${if (destination.visited) "selected" else "not selected"}",
                Toast.LENGTH_SHORT
            ).show()
        }, isHomeList = false)
        recyclerView.adapter = adapter

        // Swipe to remove
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                adapter.removeItem(vh.adapterPosition)
                viewModel.saveSearch(etSearch.text.toString(), destinationList.toList())
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // FAB confirms semi-selected → push to HomeFragment
        fab.setOnClickListener {
            val selectedDestinations = destinationList.filter { it.isSelected }
            if (selectedDestinations.isNotEmpty()) {
                selectedDestinations.forEach { it.isSelected = false }
                viewModel.addDestinations(selectedDestinations)
                viewModel.saveSearch(etSearch.text.toString(), destinationList.toList())
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

        // Restore saved search
        viewModel.searchQuery.observe(viewLifecycleOwner) { savedQuery ->
            if (etSearch.text.isNullOrEmpty() && !savedQuery.isNullOrEmpty()) {
                etSearch.setText(savedQuery)
            }
        }
        viewModel.searchResults.observe(viewLifecycleOwner) { savedResults ->
            if (destinationList.isEmpty() && savedResults.isNotEmpty()) {
                destinationList.addAll(savedResults)
                allCountries.addAll(savedResults)
                adapter.notifyDataSetChanged()
            }
        }

        // Search input listener
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) startSearch(query)
                true
            } else false
        }
    }

    private fun showCountriesForContinent(continent: String) {
        recyclerContinents.animate().alpha(0f).setDuration(300).withEndAction {
            recyclerContinents.visibility = View.GONE
        }
        recyclerView.visibility = View.VISIBLE

        val filteredCountries = allCountries.filter { it.continent.equals(continent, ignoreCase = true) }
        destinationList.clear()
        destinationList.addAll(filteredCountries)
        adapter.notifyDataSetChanged()
    }

    private fun toggleContinentAndCountryView(showCountries: Boolean) {
        recyclerContinents.visibility = if (showCountries) View.GONE else View.VISIBLE
        recyclerView.visibility = if (showCountries) View.VISIBLE else View.GONE
    }
    private fun startSearch(query: String) {
        if (query.isEmpty()) {
            // Show continents, hide country list
            recyclerContinents.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            destinationList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        // Hide continents, show country list
        recyclerContinents.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        destinationList.clear()
        adapter.notifyDataSetChanged()

        if (query.length <= 1) {
            // Short queries → fetch countries containing the letter
            CoroutineScope(Dispatchers.IO).launch {
                val filtered = CountryUtils.countries.filter { it.contains(query, ignoreCase = true) }
                for (name in filtered) {
                    val dest = try {
                        val countryResponse = RestCountriesClient.api.getCountry(name).firstOrNull()
                        Destination(
                            name = name,
                            location = "",
                            imageUrl = countryResponse?.flags?.png ?: "",
                            continent = countryResponse?.region ?: "",
                            capital = countryResponse?.capital?.firstOrNull() ?: "",
                            population = countryResponse?.population ?: 0L,
                            visited = false,
                            isSelected = false
                        )
                    } catch (e: Exception) {
                        Destination(name = name, location = "", imageUrl = "", continent = "", capital = "", population = 0L, visited = false, isSelected = false)
                    }

                    withContext(Dispatchers.Main) {
                        destinationList.add(dest)
                        adapter.notifyItemInserted(destinationList.size - 1)
                    }
                }

                withContext(Dispatchers.Main) {
                    viewModel.saveSearch(query, destinationList.toList())
                }
            }
        } else {
            // Longer queries → Wikipedia API
            fetchWikipediaDestinations(query)
        }
    }

    private fun fetchWikipediaDestinations(query: String) {
        destinationList.clear()
        adapter.notifyDataSetChanged()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = WikiRetrofitClient.instance.searchDestinations(query)
                val titles = response[1] as List<String>
                val descriptions = response[2] as List<String>

                for ((index, title) in titles.withIndex()) {
                    if (!CountryUtils.countries.contains(title)) continue

                    val dest = try {
                        val countryResponse = RestCountriesClient.api.getCountry(title).firstOrNull()
                        Destination(
                            name = title,
                            location = descriptions.getOrNull(index) ?: "",
                            imageUrl = countryResponse?.flags?.png ?: "",
                            continent = countryResponse?.region ?: "",
                            capital = countryResponse?.capital?.firstOrNull() ?: "",
                            population = countryResponse?.population ?: 0L,
                            visited = false,
                            isSelected = false
                        )
                    } catch (e: Exception) {
                        Destination(
                            name = title,
                            location = descriptions.getOrNull(index) ?: "",
                            imageUrl = "",
                            continent = "",
                            capital = "",
                            population = 0L,
                            visited = false,
                            isSelected = false
                        )
                    }

                    withContext(Dispatchers.Main) {
                        destinationList.add(dest)
                        allCountries.add(dest) // ✅ important
                        adapter.notifyItemInserted(destinationList.size - 1)
                    }
                }

                withContext(Dispatchers.Main) {
                    viewModel.saveSearch(query, destinationList.toList())
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
