package com.mobilesystems.feedme.data.response

import com.google.gson.annotations.SerializedName

data class RecipeResponse(
    val recipeId: Int,
    @SerializedName("title")
    val recipeName: String,
    val recipeLabel: String,
    @SerializedName("nutritionValue")
    val recipeNutrition: String,
    @SerializedName("recipeDescription")
    val description: String,
    val cummulativeRating: Float,
    val amountOfRatings: Int,
    val difficulty: String,
    val cookingTime: String,
    val instructions: String,
    val ingredients: List<IngredientResponse>? = null,
    val imageUrl: String)