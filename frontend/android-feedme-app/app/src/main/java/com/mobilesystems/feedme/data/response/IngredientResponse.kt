package com.mobilesystems.feedme.data.response

data class IngredientResponse(
    val ingredientId: Int,
    val ingredientName: String,
    val quantity: String = "1 Stück"
)