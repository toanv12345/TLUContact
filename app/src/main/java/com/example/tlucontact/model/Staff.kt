package com.example.tlucontact.model

data class Staff(
    val id: Int = 0,
    val name: String,
    val position: String,
    val phone: String,
    val email: String,
    val unitId: Int,
    val unitName: String = ""
)