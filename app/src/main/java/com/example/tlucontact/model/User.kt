package com.example.tlucontact.model

data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val staffId: Int = 0,
    val isAdmin: Boolean = false
)