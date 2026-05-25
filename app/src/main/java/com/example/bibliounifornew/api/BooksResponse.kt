package com.example.bibliounifornew.api

data class BooksResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val id: String?,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val publishedDate: String?,
    val categories: List<String>?,
    val imageLinks: ImageLinks?,
    val industryIdentifiers: List<IndustryIdentifier>?,
    val previewLink: String?
)

data class ImageLinks(
    val thumbnail: String?
)

data class IndustryIdentifier(
    val type: String?,
    val identifier: String?
)