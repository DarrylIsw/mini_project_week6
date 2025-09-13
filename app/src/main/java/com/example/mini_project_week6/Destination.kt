package com.example.mini_project_week6

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    val name: String,
    val location: String = "",
    val price: String = "",
    val imageUrl: String = "",
    var visited: Boolean = false,     // True = added to HomeFragment list
    var isSelected: Boolean = false,  // Keep your old logic (if used elsewhere)
    var semiSelected: Boolean = false, // 👈 NEW: used in search to mark grey state
    var flagUrl: String = ""   // 👈 new field for country flag
) : Parcelable
