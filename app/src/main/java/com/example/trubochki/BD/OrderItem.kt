package com.example.trubochki.models

data class OrderItem(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 1,
    val totalPrice: Double = 0.0,
    val date: String = "",
    val status: String = "новый"
)