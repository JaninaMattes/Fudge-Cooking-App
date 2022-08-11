package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.request.DeleteProductRequest
import com.mobilesystems.feedme.data.request.ImageRequest
import com.mobilesystems.feedme.data.request.InventoryListRequest
import com.mobilesystems.feedme.data.request.ProductRequest
import com.mobilesystems.feedme.data.response.*

interface InventoryDataSource {

    suspend fun getAllProductsInInventoryList(userId: Int): Resource<InventoryListResponse>

    suspend fun getAllExpiringProductsByUserId(userId: Int): Resource<ExpiringProductResponse>

    suspend fun addProductToInventory(request: ProductRequest): Resource<ProductIdResponse>

    suspend fun updateProductOnInventoryList(request: ProductRequest): Resource<ProductIdResponse>

    suspend fun updateProductImage(request: ImageRequest): Resource<ImageIdResponse>

    suspend fun updateInventoryList(request: InventoryListRequest): Resource<Int>

    suspend fun removeProductFromInventoryList(request: DeleteProductRequest): Resource<Int>

    suspend fun updateProductOnInventory(request: ProductRequest): Resource<ProductIdResponse>

    suspend fun getProductFromBarcodeScanResult(result: String): Resource<BarcodeProductResponse>

}