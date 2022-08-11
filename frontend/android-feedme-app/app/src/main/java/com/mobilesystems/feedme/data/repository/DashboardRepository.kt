package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.domain.model.User

interface DashboardRepository {

    suspend fun getCurrentLoggedInUser(userId: Int): User?

    suspend fun getNumberOneRecipes(userId: Int): List<Recipe>?

    suspend fun getAllExpiringProducts(userId: Int): List<Product>

     /*suspend fun getProductsAroundMe(usrId: Int): Map<Location, Product>?*/
}