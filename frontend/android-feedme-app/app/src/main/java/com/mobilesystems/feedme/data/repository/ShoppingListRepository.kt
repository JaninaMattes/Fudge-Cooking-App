package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.domain.model.Product

interface ShoppingListRepository {

    suspend fun loadOldShoppingListProducts(userId: Int): List<Product>?

    suspend fun loadCurrentShoppingListProducts(userId: Int): List<Product>?

    suspend fun updateCurrentShoppingList(userId: Int, currentShoppingList: List<Product>?)

    suspend fun removeProductFromCurrentShoppingList(userId: Int, product: Product)

    suspend fun addNewProductToCurrentShoppingList(userId: Int, product: Product): Product? // new created product

    suspend fun addProductToCurrentShoppingList(userId: Int, product: Product)//product from oldShoppinglist

    suspend fun updateSingleProductOnCurrentShoppingList(userId: Int, product: Product)

    suspend fun updateOldShoppingList(userId: Int, oldShoppingList: List<Product>?)

    suspend fun removeProductFromOldShoppingList(userId: Int, product: Product)

    suspend fun addProductToOldShoppingList(userId: Int, product: Product) // product from currentShoppinglist

    suspend fun updateSingleProductOnOldShoppingList(userId: Int, product: Product)

    //suspend fun suggestProductsForShoppingList(userId: Int): MutableLiveData<List<Product>?> //for future features
}