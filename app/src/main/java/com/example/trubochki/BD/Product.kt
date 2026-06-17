package com.example.trubochki.models

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val inStock: Boolean = true,
    val masterIds: List<String> = emptyList()  // ← Добавляем список ID мастеров
)