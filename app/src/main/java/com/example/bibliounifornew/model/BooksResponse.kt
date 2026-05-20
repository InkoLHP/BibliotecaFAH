package com.example.bibliounifor.model

// A lista principal de livros que o Google devolve
data class BooksResponse(
    val items: List<BookItem>?
)

// Cada livro individual dentro da lista
data class BookItem(
    val volumeInfo: VolumeInfo
)

// As informações que realmente importam para o seu app
data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val description: String?, // A sinopse
    val imageLinks: ImageLinks? // A capa do livro
)

// O link da imagem da capa
data class ImageLinks(
    val thumbnail: String
)