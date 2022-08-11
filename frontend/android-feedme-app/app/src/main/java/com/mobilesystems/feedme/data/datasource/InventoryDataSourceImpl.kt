package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.request.DeleteProductRequest
import com.mobilesystems.feedme.data.request.ImageRequest
import com.mobilesystems.feedme.data.request.InventoryListRequest
import com.mobilesystems.feedme.data.request.ProductRequest
import com.mobilesystems.feedme.data.response.ImageIdResponse
import com.mobilesystems.feedme.di.AppModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InventoryDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BaseDataSource(), InventoryDataSource {

    override suspend fun getAllProductsInInventoryList(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getAllProductsByUserId(userId) }
    }

    override suspend fun getAllExpiringProductsByUserId(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getExpiringProductsByUserId(userId) }
    }

    override suspend fun addProductToInventory(request: ProductRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.addProductToInventory(request) }
    }

    override suspend fun updateProductOnInventoryList(request: ProductRequest)= withContext(ioDispatcher) {
        getResult { foodTrackerApi.updateProductOnInventory(request) }
    }

    override suspend fun updateProductImage(request: ImageRequest): Resource<ImageIdResponse> = withContext(ioDispatcher) {
        getResult { foodTrackerApi.updateProductImage(request) }
    }

    override suspend fun updateInventoryList(request: InventoryListRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.updateInventoryList(request) }
    }

    override suspend fun removeProductFromInventoryList(request: DeleteProductRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.deleteProductFromInventory(request) }
    }

    override suspend fun updateProductOnInventory(request: ProductRequest) = withContext(ioDispatcher){
        getResult { foodTrackerApi.updateProductOnInventory(request) }
    }

    override suspend fun getProductFromBarcodeScanResult(result: String) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getProductFromBarcode(result) }
    }

}