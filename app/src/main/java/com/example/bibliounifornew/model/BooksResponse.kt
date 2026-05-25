package com.example.bibliounifor.model

data class BooksResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val imageLinks: ImageLinks?,
    val industryIdentifiers: List<IndustryIdentifier>?
)

data class ImageLinks(
    val thumbnail: String?
)

data class IndustryIdentifier(
    val type: String?,
    val identifier: String?
)