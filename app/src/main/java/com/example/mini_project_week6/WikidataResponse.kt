package com.example.mini_project_week6

data class WikidataResponse(
    val entities: Map<String, Entity>
) {
    data class Entity(
        val claims: Map<String, List<Claim>>?
    )

    data class Claim(
        val mainsnak: Mainsnak
    )

    data class Mainsnak(
        val datavalue: DataValue?
    )

    data class DataValue(
        val value: Value?
    )

    data class Value(
        val id: String? // e.g. Q6256 for "sovereign state"
    )
}
