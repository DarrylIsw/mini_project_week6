package com.example.mini_project_week6

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Parcelable data class representing a travel destination or country
// Parcelable makes it easy to pass objects between activities/fragments
@Parcelize
data class Destination(
    val name: String,                 // Country or destination name
    var location: String = "",        // Additional location info (optional)
    val price: String = "",           // Price info (if used in UI)
    val imageUrl: String = "",        // Image URL for displaying destination picture
    var visited: Boolean = false,     // True if added to saved list in HomeFragment
    var isSelected: Boolean = false,  // Used to mark selection state (legacy logic support)
    var semiSelected: Boolean = false,// Used in search to indicate partial/greyed selection
    var flagUrl: String = "",         // Country flag image URL
    var continent: String = "",       // Continent or region
    var capital: String = "",         // Capital city
    val population: Long = 0L,        // Population number
) : Parcelable
