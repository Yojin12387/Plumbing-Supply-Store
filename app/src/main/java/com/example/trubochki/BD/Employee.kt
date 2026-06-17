package com.example.trubochki.models

data class Employee(
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val experience: String = "",
    val specialty: String = "",
    val services: List<Service> = emptyList()
)

data class Service(
    val name: String = "",
    val price: String = ""
)