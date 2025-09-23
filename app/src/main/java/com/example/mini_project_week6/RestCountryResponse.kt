package com.example.mini_project_week6

// Data class representing the JSON response structure from the REST Countries API
data class RestCountryResponse(
    val name: Name?,          // Country name object (common + official names)
    val capital: List<String>?, // Capital(s) of the country (may be null or empty)
    val region: String?,        // Continent/region (e.g., "Asia", "Europe")
    val population: Long?,      // Country population (nullable if missing)
    val flags: Flags?           // Country flag image URLs (PNG, SVG)
) {
    // Nested data class for "name" field in API response
    data class Name(
        val common: String?,   // Commonly used country name (e.g., "Japan")
        val official: String?  // Official country name (e.g., "Japan, State of")
    )

    // Nested data class for "flags" field in API response
    data class Flags(
        val png: String?,      // PNG format flag URL
        val svg: String?       // SVG format flag URL
    )
}
