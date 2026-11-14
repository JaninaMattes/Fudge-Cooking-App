package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.InventoryDataSourceImpl
import com.mobilesystems.feedme.data.datasource.RecipeDataSourceImpl
import com.mobilesystems.feedme.data.datasource.UserDataSourceImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.common.utils.convertExpiringProductList
import com.mobilesystems.feedme.ui.common.utils.convertRecipeResponse
import com.mobilesystems.feedme.ui.common.utils.convertUserResponse
import javax.inject.Inject

// A later version gives the products around the user on a map. The
// method getProductsAroundMe() belongs to that version.
class DashboardRepositoryImpl
    @Inject
    constructor(
        private val inventoryDataSource: InventoryDataSourceImpl,
        private val recipeDataSourceImpl: RecipeDataSourceImpl,
        private val userDataSourceImpl: UserDataSourceImpl,
    ) :
    DashboardRepository {
        override suspend fun getCurrentLoggedInUser(userId: Int): User? {
            // get currently logged in user
            var user: User? = null
            val result = userDataSourceImpl.getUserById(userId)
            val userResponse = result.data
            if (userResponse != null) {
                user = convertUserResponse(userResponse)
            }
            return user
        }

        override suspend fun getNumberOneRecipes(userId: Int): List<Recipe> {
            val result = recipeDataSourceImpl.getAllRecipesByUserId(userId)
            val recipes = convertRecipeResponse(result.data)
            Log.d("Dashboard", "Get best rated recipe result $result")
            return recipes
        }

        override suspend fun getAllExpiringProducts(userId: Int): List<Product> {
            // get all products for current user
            val result = inventoryDataSource.getAllExpiringProductsByUserId(userId)
            Log.d("Dashboard", "Get expiring products $result")
            // val products = filterByExpDate(tempList)
            return convertExpiringProductList(result.data)
        }
    }
