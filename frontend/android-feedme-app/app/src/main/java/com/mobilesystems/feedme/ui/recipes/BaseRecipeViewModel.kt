package com.mobilesystems.feedme.ui.recipes

import androidx.lifecycle.LiveData
import com.mobilesystems.feedme.data.response.IngredientResponse
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe

interface BaseRecipeViewModel {

    fun selectedRecipe(recipe: Recipe)

    fun updateRecipe(recipe: Recipe) // Rating of recipe

    fun addRecipeToFavorites(recipeId: Int) // For future

    fun loadInventoryList()

    fun loadMatchingRecipes()

    fun loadShoppingList()

    fun saveCurrentShoppingState()

    fun exportUnavailableIngredientsToShoppingList()

    fun removeRecipeByPosition(position: Int) // Needs discussion - Can be removed

    fun loadAllRecipeIngredients(): LiveData<List<Product>?> // Helper function

    fun loadAllUnAvailableIngredients(): LiveData<List<Product>?> // Helper function

    fun loadAllAvailableIngredients(): LiveData<List<Product>?> // Helper function

}
