package com.mobilesystems.feedme.ui.search

import androidx.lifecycle.LiveData
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe

interface BaseSearchViewModel {

    fun loadAllRecipes()

    fun loadAllProductsFromInventory()

    fun searchGlobal(searchText: String): LiveData<List<Any>?> // Helper function

    fun searchProduct(searchText: String): LiveData<List<Product>?> // Helper function

    fun searchRecipe(searchText: String): LiveData<List<Recipe>?> // Helper function
}