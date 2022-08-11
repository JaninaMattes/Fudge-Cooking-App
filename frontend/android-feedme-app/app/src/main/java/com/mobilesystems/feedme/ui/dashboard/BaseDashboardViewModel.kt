package com.mobilesystems.feedme.ui.dashboard

import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe

interface BaseDashboardViewModel {

    fun selectProduct(product: Product)

    fun selectedRecipe(recipe: Recipe)

    fun loadExpiringProducts()

    fun loadNumberOneRecipes()

    fun loadLoggedInUser()

    fun logout()

    fun updateLogin()
}