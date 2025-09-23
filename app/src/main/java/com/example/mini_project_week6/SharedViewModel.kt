package com.example.mini_project_week6

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// ViewModel class that holds shared data (destinations & search state)
// between different fragments in the app
class SharedViewModel : ViewModel() {

    // 🔹 Holds the list of saved destinations (private, mutable)
    private val _destinations = MutableLiveData<List<Destination>>(emptyList())

    // 🔹 Public immutable LiveData that fragments can observe
    val destinations: LiveData<List<Destination>> = _destinations

    // 🔹 Holds the current search query (private, mutable)
    private val _searchQuery = MutableLiveData<String>()

    // 🔹 Public immutable LiveData for observing current search query
    val searchQuery: LiveData<String> = _searchQuery

    // 🔹 Holds search results from API (private, mutable)
    private val _searchResults = MutableLiveData<List<Destination>>(emptyList())

    // 🔹 Public immutable LiveData for observing search results
    val searchResults: LiveData<List<Destination>> = _searchResults

    // ➡️ Add new destinations into the saved list (with deduplication)
    fun addDestinations(newDestinations: List<Destination>) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()

        for (dest in newDestinations) {
            // Avoid duplicates by checking if name already exists
            if (currentList.none { it.name == dest.name }) {
                currentList.add(dest.copy(isSelected = true)) // mark added items as selected
            }
        }

        _destinations.value = currentList.toList() // update LiveData
    }

    // ➡️ Remove a specific destination from the saved list
    fun removeDestination(destination: Destination) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.name == destination.name }
        _destinations.value = currentList.toList()
    }

    // ➡️ Clear all saved destinations
    fun clearDestinations() {
        _destinations.value = emptyList()
    }

    // ➡️ Reset search query & results
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    // ➡️ Save the latest search query and its results
    fun saveSearch(query: String, results: List<Destination>) {
        _searchQuery.value = query
        _searchResults.value = results
    }
}
