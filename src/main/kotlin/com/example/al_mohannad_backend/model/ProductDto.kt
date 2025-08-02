package com.example.al_mohannad_backend.model

data class ProductDto(
    val id: Long,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val categoryId: Long,
    val categoryName: String
)