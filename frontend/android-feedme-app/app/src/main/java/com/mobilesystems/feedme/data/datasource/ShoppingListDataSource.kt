package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.request.ProductRequest
import com.mobilesystems.feedme.data.request.ShoppingListProductIDRequest
import com.mobilesystems.feedme.data.request.ShoppingListRequest
import com.mobilesystems.feedme.data.response.ProductIdResponse
import com.mobilesystems.feedme.data.response.ShoppingListResponse

interface ShoppingListDataSource{

    suspend fun loadAllProductsInCurrentShoppingList(userId: Int): Resource<ShoppingListResponse>

    suspend fun loadAllProductsInOldShoppingList(userId: Int): Resource<ShoppingListResponse>

    suspend fun updateCurrentShoppingList(request: ShoppingListRequest): Resource<Int>

    suspend fun updateOldShoppingList(request: ShoppingListRequest): Resource<Int>

    suspend fun createProductToCurrentShoppingList(request: ProductRequest): Resource<ProductIdResponse>

    suspend fun addProductToCurrentShoppingList(request: ShoppingListProductIDRequest): Resource<Int>

    suspend fun addProductToOldShoppingList(request: ShoppingListProductIDRequest): Resource<Int>

    suspend fun removeProductFromCurrentShoppingList(request: ShoppingListProductIDRequest): Resource<Int>

    suspend fun removeProductFromOldShoppingList(request: ShoppingListProductIDRequest): Resource<Int>

    suspend fun updateSingleProductOnCurrentShoppingList(request: ProductRequest): Resource<Int>

    suspend fun updateSingleProductOnOldShoppingList(request: ProductRequest): Resource<Int>

   // suspend fun loadSuggestedProductsForShoppingList(): Response<List<Product>>? // Future feature

}