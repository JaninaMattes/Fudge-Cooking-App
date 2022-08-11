package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.request.ProductRequest
import com.mobilesystems.feedme.data.request.ShoppingListProductIDRequest
import com.mobilesystems.feedme.data.request.ShoppingListRequest
import com.mobilesystems.feedme.di.AppModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShoppingListDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BaseDataSource(), ShoppingListDataSource{

    override suspend fun loadAllProductsInCurrentShoppingList(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.loadAllProductsInCurrentShoppingList(userId) }
    }

    override suspend fun loadAllProductsInOldShoppingList(userId: Int) = withContext(defaultDispatcher) {
        getResult{ foodTrackerApi.loadAllProductsInOldShoppingList(userId)}
    }

    override suspend fun updateCurrentShoppingList(request: ShoppingListRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.updateCurrentShoppingList(request)}
    }

    override suspend fun updateOldShoppingList(request: ShoppingListRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.updateOldShoppingList(request)}
    }

    override suspend fun createProductToCurrentShoppingList(request: ProductRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.createProductToCurrentShoppingList(request)}
    }

    override suspend fun addProductToCurrentShoppingList(request: ShoppingListProductIDRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.addProductToCurrentShoppingList(request)}
    }

    override suspend fun addProductToOldShoppingList(request: ShoppingListProductIDRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.addProductToOldShoppingList(request)}
    }

    override suspend fun removeProductFromCurrentShoppingList(request: ShoppingListProductIDRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.removeProductFromCurrentShoppingList(request)}
    }

    override suspend fun removeProductFromOldShoppingList(request: ShoppingListProductIDRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.removeProductFromOldShoppingList(request)}
    }

    override suspend fun updateSingleProductOnCurrentShoppingList(request: ProductRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.updateSingleProductOnCurrentShoppingList(request)}
    }

    override suspend fun updateSingleProductOnOldShoppingList(request: ProductRequest) = withContext(ioDispatcher){
        getResult{foodTrackerApi.updateSingleProductOnOldShoppingList(request)}
    }

   /* //for future features
   override suspend fun loadSuggestedProductsForShoppingList(): Response<List<Product>>? {
        // Helper function for future features
        return try {
            fakeOldShoppingList = ShoppingListPlaceholderContent.VARIOUS_ITEMS_OLD_SHOP
            Response.Success(fakeCurrentShoppingList)
        } catch (e: Throwable) {
            Response.Error("IOException")
        }
    }*/
}