package com.example.mini_project_week6

data class WikiResponse(
    val query: QueryResult?
)

data class QueryResult(
    val search: List<SearchResult>
)

data class SearchResult(
    val title: String,
    val snippet: String
)
