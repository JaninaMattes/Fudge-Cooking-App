package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.RecipeDataSourceImpl
import com.mobilesystems.feedme.data.datasource.ShoppingListDataSourceImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.ui.common.utils.*
import javax.inject.Inject

/**
 * https://developer.android.com/kotlin/flow
 * https://developer.android.com/topic/libraries/architecture/livedata
 */

class RecipeRepositoryImpl @Inject constructor(
    private val dataSourceImpl: RecipeDataSourceImpl,
    private val shoppingDataSourceImpl: ShoppingListDataSourceImpl)
    : RecipeRepository {

    override suspend fun loadAllRecipesBasedOnInventory(userId: Int): List<Recipe> {
        // get all products for current user
        val result = dataSourceImpl.getAllRecipesByUserId(userId)
        return convertRecipeResponse(result.data)
    }

    override suspend fun loadBestRatedRecipesBasedOnInventory(userId: Int): List<Recipe>? {
        // get best rated recipes for current user
        val result = dataSourceImpl.getAllRecipesByUserId(userId)
        return convertRecipeResponse(result.data)
    }

    override suspend fun rateRecipe(recipe: Recipe) {
        dataSourceImpl.updateRatingByRecipeId(recipe.recipeId, recipe.cummulativeRating)
    }

    override suspend fun addRecipeToFavoriteList(userId: Int, recipeId: Int) {
        // Implement in future
        dataSourceImpl.addRecipeToFavoritesByUserId(userId, recipeId)
    }

    override suspend fun loadCurrentShoppingListProducts(userId: Int): List<Product>? {
        // get current shoppinglist for current user
        val result = shoppingDataSourceImpl.loadAllProductsInCurrentShoppingList(userId)
        return convertShoppingListResponse(result.data)
    }

    override suspend fun updateCurrentShoppingList(userId: Int, shoppingList: List<Product>?){
        //add request
        //shoppingDataSourceImpl.updateCurrentShoppingList(request)
    }

    override suspend fun updateSingleProductOnCurrentShoppingList(userId: Int, product: Product) {
        val request = convertProductRequest(userId, product)
        if(request != null) {
            shoppingDataSourceImpl.updateSingleProductOnCurrentShoppingList(request)
        }else{
            Log.d("RecipeRepository", "Updated products are empty!")
        }
    }

    override suspend fun addNewProductToCurrentShoppingList(userId: Int, product: Product): Product {
        val request = convertProductRequest(userId, product)
        var newProduct: Product = product

        if(request != null){
            val result = shoppingDataSourceImpl.createProductToCurrentShoppingList(request)
            if(result.data != null && result.data.productId != 0){
                newProduct = convertProductWithNewProductId(result.data, product)
            }
        }
        return newProduct
    }

}

