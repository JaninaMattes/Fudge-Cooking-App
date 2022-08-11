package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.InventoryDataSourceImpl
import com.mobilesystems.feedme.data.datasource.RecipeDataSourceImpl
import com.mobilesystems.feedme.data.datasource.UserDataSourceImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.common.utils.*
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val inventoryDataSource: InventoryDataSourceImpl,
    private val recipeDataSourceImpl: RecipeDataSourceImpl,
    private val userDataSourceImpl: UserDataSourceImpl)
    : DashboardRepository {

    override suspend fun getCurrentLoggedInUser(userId: Int): User? {
        // get currently logged in user
        var user: User? = null
        val result = userDataSourceImpl.getUserById(userId)
        val userResponse = result.data
        if(userResponse != null) {
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
        //val products = filterByExpDate(tempList)
        return convertExpiringProductList(result.data)
    }

    /*private fun filterByExpDate(productList: List<Product>?) : List<Product> {
        val tempList= mutableListOf<Product>()
        if(productList != null){
            Log.d("Dashboard", "Filter products by expiration date.")
            productList.forEach{ p->
                val expDate = getTimeDiff(p.expirationDate)
                if(expDate <= 3){
                    tempList.add(p)
                    tempList.sortedBy{expDate}
                }
            }
        } else {
            Log.d("Dashboard", "Productlist is null.")
        }
        return tempList
    }*/

    /*//for further features: Future integration of a map to bring in social component
    override suspend fun getProductsAroundMe(usrId: Int): Map<Location, Product>? {
    }*/
}