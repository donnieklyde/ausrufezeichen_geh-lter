package com.poetic.card.model

data class AuthRequest(
    val idToken: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class User(
    val id: String,
    val username: String?,
    val email: String,
    val picture: String?,
    val balance: Double
)
