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
import androidx.activity.OnBackPressedCallback


// MainFragment = search + browse screen (continents + countries + add to bucket list)
class MainFragment : Fragment(R.layout.fragment_main) {

    // 🔹 RecyclerView for showing countries (search results or by continent)
    private lateinit var recyclerView: RecyclerView

    // 🔹 Adapter for displaying list of countries
    private lateinit var adapter: DestinationAdapter

    // 🔹 Mutable list holding current search results or countries
    private val destinationList = mutableListOf<Destination>()

    // 🔹 Input field for searching countries
    private lateinit var etSearch: TextInputEditText

    // 🔹 FloatingActionButton for confirming selected countries → add to bucket list
    private lateinit var fab: FloatingActionButton

    // 🔹 RecyclerView for displaying grid of continents
    private lateinit var recyclerContinents: RecyclerView

    // 🔹 Adapter for displaying continents (Asia, Europe, etc.)
    private lateinit var continentAdapter: ContinentAdapter

    // 🔹 Hardcoded list of continents
    private val continents = listOf("Asia", "Europe", "Africa", "Americas", "Oceania")

    // 🔹 Cache for all countries loaded (used to reset or reuse data)
    private val allCountries = mutableListOf<Destination>()

    // 🔹 Shared ViewModel to persist data across fragments
    private val viewModel: SharedViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views first
        recyclerView = view.findViewById(R.id.recyclerView)
        etSearch = view.findViewById(R.id.etSearch)
        fab = view.findViewById(R.id.fabAdd)
        recyclerContinents = view.findViewById(R.id.recyclerContinents)

        // ✅ Clear search bar and reset saved search in ViewModel
        etSearch.setText("")
        viewModel.clearSearch() // clears _searchQuery and _searchResults

        // Setup continent RecyclerView
        recyclerContinents.layoutManager = GridLayoutManager(requireContext(), 3)
        continentAdapter = ContinentAdapter(requireContext(), continents) { continent ->
            // Instead of showCountriesForContinent, use startSearch with continent filter
            showCountriesByContinent(continent)
        }
        recyclerContinents.adapter = continentAdapter

        // Setup country RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = DestinationAdapter(requireContext(), destinationList, { destination ->
            Toast.makeText(
                requireContext(),
                "${destination.name} is ${if (destination.visited) "selected" else "not selected"}",
                Toast.LENGTH_SHORT
            ).show()
        }, isHomeList = false)
        recyclerView.adapter = adapter

        // Delete Function - Swipe to remove countries from search list
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                adapter.removeItem(vh.adapterPosition)
                viewModel.saveSearch(etSearch.text.toString(), destinationList.toList())
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)

        // FAB Button - FAB confirms semi-selected → push to HomeFragment
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
                allCountries.addAll(savedResults) // keep allCountries updated
                adapter.notifyDataSetChanged()
            }
        }

        // Search input listener
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                startSearch(query)
                true
            } else false
        }

        // Back Press Handling
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (recyclerView.visibility == View.VISIBLE) {
                    // If showing search results, hide them and show continents
                    recyclerView.visibility = View.GONE
                    recyclerContinents.visibility = View.VISIBLE
                } else {
                    // Go back to HomeFragment
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        })
    }

    // 🔹 Show list of countries for a specific continent
    private fun showCountriesByContinent(continent: String) {
        // Hide continents, show country list
        recyclerContinents.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        destinationList.clear()
        allCountries.clear()
        adapter.notifyDataSetChanged()

        // Map continent to API region
        val regionMap = mapOf(
            "Asia" to "Asia",
            "Europe" to "Europe",
            "Africa" to "Africa",
            "Americas" to "Americas",
            "Oceania" to "Oceania"
        )
        val apiRegion = regionMap[continent] ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (name in CountryUtils.countries) {
                    val countryResponse = RestCountriesClient.api.getCountry(name).firstOrNull()

                    if (countryResponse?.region.equals(apiRegion, ignoreCase = true)) {
                        val dest = Destination(
                            name = name,
                            location = "",
                            imageUrl = countryResponse?.flags?.png ?: "",
                            continent = countryResponse?.region ?: "",
                            capital = countryResponse?.capital?.firstOrNull() ?: "",
                            population = countryResponse?.population ?: 0L,
                            visited = false,
                            isSelected = false
                        )

                        // Update UI one by one
                        withContext(Dispatchers.Main) {
                            destinationList.add(dest)
                            allCountries.add(dest)
                            adapter.notifyItemInserted(destinationList.size - 1)
                        }
                    }
                }

                // Save whole list in ViewModel after loading
                withContext(Dispatchers.Main) {
                    viewModel.saveSearch(continent, destinationList.toList())
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "API Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 🔹 Start a search by query (short = filter local, long = API call)
    private fun startSearch(query: String) {
        if (query.isEmpty()) {
            // Show continents, hide country list
            recyclerContinents.visibility = View.VISIBLE
            recyclerContinents.alpha = 1f
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
                    val dest = fetchCountryInfo(name)
                    withContext(Dispatchers.Main) {
                        destinationList.add(dest)
                        allCountries.add(dest) // ✅ keep allCountries updated
                        adapter.notifyItemInserted(destinationList.size - 1)
                    }
                }
                withContext(Dispatchers.Main) {
                    viewModel.saveSearch(query, destinationList.toList())
                }
            }
        } else {
            // Longer queries → RestCountries API
            fetchRestCountries(query)
        }
    }

    // 🔹 Fetch details of a single country by name
    private suspend fun fetchCountryInfo(name: String, location: String = ""): Destination {
        return try {
            val countryResponse = RestCountriesClient.api.getCountry(name).firstOrNull()
            Destination(
                name = name,
                location = location,
                imageUrl = countryResponse?.flags?.png ?: "",
                continent = countryResponse?.region ?: "",
                capital = countryResponse?.capital?.firstOrNull() ?: "",
                population = countryResponse?.population ?: 0L,
                visited = false,
                isSelected = false
            )
        } catch (e: Exception) {
            Destination(
                name = name,
                location = location,
                imageUrl = "",
                continent = "",
                capital = "",
                population = 0L,
                visited = false,
                isSelected = false
            )
        }
    }

    // 🔹 Fetch countries by name from RestCountries API
    private fun fetchRestCountries(query: String) {
        destinationList.clear()
        adapter.notifyDataSetChanged()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RestCountriesClient.api.getCountry(query)

                for (countryResponse in response) {
                    val dest = Destination(
                        name = countryResponse.name?.common ?: query, // safe call
                        location = "", // no description here
                        imageUrl = countryResponse.flags?.png ?: "",
                        continent = countryResponse.region ?: "",
                        capital = countryResponse.capital?.firstOrNull() ?: "",
                        population = countryResponse.population ?: 0L,
                        visited = false,
                        isSelected = false
                    )

                    // Update UI one by one
                    withContext(Dispatchers.Main) {
                        destinationList.add(dest)
                        allCountries.add(dest)
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