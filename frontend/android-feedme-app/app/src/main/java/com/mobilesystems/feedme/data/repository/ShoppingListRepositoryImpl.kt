package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.ShoppingListDataSourceImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.ui.common.utils.*
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val dataSourceImpl: ShoppingListDataSourceImpl)
    : ShoppingListRepository {

    override suspend fun loadOldShoppingListProducts(userId: Int): List<Product>? {
        // get old shoppinglist for current user
        val result = dataSourceImpl.loadAllProductsInOldShoppingList(userId)
        return convertShoppingListResponse(result.data)
    }

    override suspend fun loadCurrentShoppingListProducts(userId: Int): List<Product>? {
        // get current shoppinglist for current user
        val result = dataSourceImpl.loadAllProductsInCurrentShoppingList(userId)
        return convertShoppingListResponse(result.data)
    }

    override suspend fun removeProductFromCurrentShoppingList(userId: Int, product: Product) {
        val request = convertShoppingListProductIDRequest(userId, product)
        dataSourceImpl.removeProductFromCurrentShoppingList(request)
    }

    override suspend fun addNewProductToCurrentShoppingList(userId: Int, product: Product): Product {
        val request = convertProductRequest(userId, product)
        var newProduct: Product = product
        if(request != null){
            // create new product and get product id as result
            val result = dataSourceImpl.createProductToCurrentShoppingList(request)
            if(result.data != null && result.data.productId != 0){
                newProduct = convertProductWithNewProductId(result.data, product)
            }
        }
        return newProduct
    }

    override suspend fun addProductToCurrentShoppingList(userId: Int, product: Product) {
        val request = convertShoppingListProductIDRequest(userId, product)
        dataSourceImpl.addProductToCurrentShoppingList(request)
    }

    override suspend fun updateSingleProductOnCurrentShoppingList(userId: Int, product: Product) {
        val request = convertProductRequest(userId, product)
        if(request != null) {
            dataSourceImpl.updateSingleProductOnCurrentShoppingList(request)
        }else{
            Log.d("ShoppingList Repository", "Shoppinglist is empty!")
        }
    }

    override suspend fun addProductToOldShoppingList(userId: Int, product: Product) {
        val request = convertShoppingListProductIDRequest(userId, product)
        dataSourceImpl.addProductToOldShoppingList(request)
    }

    override suspend fun updateSingleProductOnOldShoppingList(userId: Int, product: Product){
        val request = convertProductRequest(userId, product)
        if(request != null){
            dataSourceImpl.updateSingleProductOnOldShoppingList(request)
        }else{
            Log.d("ShoppingList Repository", "Shoppinglist is empty!")
        }
    }

    override suspend fun removeProductFromOldShoppingList(userId: Int, product: Product) {
        val request = convertShoppingListProductIDRequest(userId, product)
        val result = dataSourceImpl.removeProductFromOldShoppingList(request)
        Log.d("ShoppingList Repository", "Remove Product from oldshoppinglist: " + result.data.toString())
    }

    override suspend fun updateCurrentShoppingList(userId: Int, currentShoppingList: List<Product>?) {
        // TODO anbinden
        // dataSourceImpl.updateCurrentShoppingList(request)
    }

    override suspend fun updateOldShoppingList(userId: Int, oldShoppingList: List<Product>?) {
        //dataSourceImpl.updateOldShoppingList(userId, oldShoppingList)
    }

    /* //for future features
    override suspend fun suggestProductsForShoppingList(userId: Int): MutableLiveData<List<Product>?> {
         val result = dataSourceImpl.loadSuggestedProductsForShoppingList()

         if (result is Response.Success) {
             suggestedShoppingListProducts.postValue(result.data)
         }
         return suggestedShoppingListProducts
     }*/
}