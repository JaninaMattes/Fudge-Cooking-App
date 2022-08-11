package com.mobilesystems.feedme.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    val recipeId: Int,
    @SerializedName("title")
    val recipeName: String,
    val recipeLabel: String = "",
    @SerializedName("nutritionValue")
    val recipeNutrition: String,
    @SerializedName("recipeDescription")
    val description: String,
    val cummulativeRating: Float,
    val amountOfRatings: Int,
    val difficulty: String,
    val cookingTime: String,
    val instructions: String,
    val ingredients: List<Product>? = null,
    val imageUrl: String): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (recipeId != other.recipeId) return false
        if (recipeName != other.recipeName) return false
        if (recipeLabel != other.recipeLabel) return false
        if (recipeNutrition != other.recipeNutrition) return false
        if (description != other.description) return false
        if (cummulativeRating != other.cummulativeRating) return false
        if (difficulty != other.difficulty) return false
        if (cookingTime != other.cookingTime) return false
        if (instructions != other.instructions) return false
        if (ingredients != other.ingredients) return false
        if (imageUrl != other.imageUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recipeId
        result = 31 * result + recipeName.hashCode()
        result = 31 * result + recipeLabel.hashCode()
        result = 31 * result + recipeNutrition.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + cummulativeRating.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + cookingTime.hashCode()
        result = 31 * result + instructions.hashCode()
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + imageUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return "Recipe(recipeId=$recipeId, recipeName='$recipeName', recipeLabel='$recipeLabel', " +
                "recipeNutrition='$recipeNutrition', description='$description', rating=$cummulativeRating, " +
                "difficulty='$difficulty', cookingTime='$cookingTime', " +
                "instruction='$instructions', ingredients=$ingredients, imageUrl='$imageUrl')"
    }


}