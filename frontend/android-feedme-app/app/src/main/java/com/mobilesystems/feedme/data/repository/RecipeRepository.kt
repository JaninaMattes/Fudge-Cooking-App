package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe

interface RecipeRepository {

    suspend fun loadAllRecipesBasedOnInventory(userId: Int): List<Recipe>

    suspend fun loadBestRatedRecipesBasedOnInventory(userId: Int): List<Recipe>?

    suspend fun rateRecipe(recipe: Recipe)

    suspend fun addRecipeToFavoriteList(userId: Int, recipeId: Int)

    suspend fun loadCurrentShoppingListProducts(userId: Int): List<Product>?

    suspend fun updateCurrentShoppingList(userId: Int, shoppingList: List<Product>?)

    suspend fun updateSingleProductOnCurrentShoppingList(userId: Int, product: Product)

    suspend fun addNewProductToCurrentShoppingList(userId: Int, product: Product): Product
}