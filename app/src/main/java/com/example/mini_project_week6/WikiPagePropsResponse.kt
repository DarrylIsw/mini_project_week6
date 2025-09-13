package com.example.mini_project_week6

data class WikiPagePropsResponse(
    val query: Query?
) {
    data class Query(
        val pages: Map<String, Page>
    )

    data class Page(
        val pageid: Long?,
        val title: String?,
        val pageprops: PageProps?
    )

    data class PageProps(
        val wikibase_item: String?
    )
}
