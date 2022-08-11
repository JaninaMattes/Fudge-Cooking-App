package com.mobilesystems.feedme.ui.shoppinglist

import com.mobilesystems.feedme.domain.model.Product

interface BaseShoppingListViewModel {

    fun setProduct(product: Product) // Helper function

    fun setNewProduct(product: Product) // Helper function

    fun loadAllCurrentShoppingListProducts()

    fun loadAllOldShoppingListProducts()

    fun saveCurrentState()

    fun addNewProductToCurrentShoppingList(product: Product)//newproduct

    fun addProductToCurrentShoppingList(product: Product)//product from oldshoppinglist

    fun addProductToOldShoppingList(product: Product)

    fun updateProductOnOldShoppingList(product: Product)

    fun updateProductOnCurrentShoppingList(product: Product)

    fun updateCurrentShoppingList(shoppingList: List<Product>?)

    fun updateOldShoppingList(shoppingList: List<Product>?)

    fun removeProductFromOldShoppingList(product: Product)

    fun removeProductFromCurrentShoppingList(product: Product)

    fun deleteCurrentProductByPosition(position: Int): Product?

    fun deleteOldProductByPosition(position: Int): Product?

}