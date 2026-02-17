package com.poetic.card.model

data class UpdateCardRequest(
    val isListed: Boolean? = null,
    val price: Double? = null
)
