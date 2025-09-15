package com.example.mini_project_week6

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _destinations = MutableLiveData<List<Destination>>(emptyList())
    val destinations: LiveData<List<Destination>> = _destinations

    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery

    private val _searchResults = MutableLiveData<List<Destination>>(emptyList())
    val searchResults: LiveData<List<Destination>> = _searchResults

    // Add new destinations (deduplication included)
    fun addDestinations(newDestinations: List<Destination>) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()

        for (dest in newDestinations) {
            // Prevent duplicate by name
            if (currentList.none { it.name == dest.name }) {
                currentList.add(dest.copy(isSelected = true)) // ensure added items are marked
            }
        }

        _destinations.value = currentList.toList() // force LiveData emit
    }

    fun removeDestination(destination: Destination) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.name == destination.name }
        _destinations.value = currentList.toList()
    }

    fun clearDestinations() {
        _destinations.value = emptyList()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun saveSearch(query: String, results: List<Destination>) {
        _searchQuery.value = query
        _searchResults.value = results
    }
}

