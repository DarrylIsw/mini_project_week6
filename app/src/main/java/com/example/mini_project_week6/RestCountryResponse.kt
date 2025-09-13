package com.example.mini_project_week6

data class RestCountryResponse(
    val name: Name?,
    val capital: List<String>?,
    val region: String?,          // Continent/region
    val population: Long?,
    val flags: Flags?
) {
    data class Name(
        val common: String?,
        val official: String?
    )

    data class Flags(
        val png: String?,
        val svg: String?
    )
}
