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

    // Add new destinations to the list
    fun addDestinations(newDestinations: List<Destination>) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()
        currentList.addAll(newDestinations)
        _destinations.value = currentList
    }

    // Optional: remove a destination
    fun removeDestination(destination: Destination) {
        val currentList = _destinations.value?.toMutableList() ?: mutableListOf()
        currentList.remove(destination)
        _destinations.value = currentList
    }

    // Optional: clear all
    fun clearDestinations() {
        _destinations.value = emptyList()
    }

    fun saveSearch(query: String, results: List<Destination>) {
        _searchQuery.value = query
        _searchResults.value = results
    }
}
