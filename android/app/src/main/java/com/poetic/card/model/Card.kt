package com.poetic.card.model

data class Card(
    val id: String,
    val text: String,
    val backgroundUrl: String, // or Uri string
    val price: Double,
    val ownerId: String,
    val isListed: Boolean = false,
    val owner: UserDto? = null
)

data class UserDto(
    val username: String?
)
