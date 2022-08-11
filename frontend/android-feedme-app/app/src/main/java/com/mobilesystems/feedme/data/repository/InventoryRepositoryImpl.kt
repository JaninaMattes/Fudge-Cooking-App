package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.InventoryDataSourceImpl
import com.mobilesystems.feedme.data.request.DeleteProductRequest
import com.mobilesystems.feedme.data.request.InventoryListRequest
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.ui.common.utils.*
import javax.inject.Inject

/**
 * https://developer.android.com/kotlin/flow
 * https://developer.android.com/topic/libraries/architecture/livedata
 */
class InventoryRepositoryImpl @Inject constructor(
    private val inventoryDataSource: InventoryDataSourceImpl)
    : InventoryRepository{

    override suspend fun loadInventoryListProducts(userId: Int): List<Product> {
        // get all products for current user
        val result = inventoryDataSource.getAllProductsInInventoryList(userId)
        return convertInventoryListResponse(result.data)
    }

    override suspend fun updateProductOnInventory(userId: Int, product: Product) {
        // update single product for current user
        val request = convertProductRequest(userId, product)
        if(request != null){
            Log.d("InventoryRepository", "Update product.")
            inventoryDataSource.updateProductOnInventoryList(request)
        }
    }

    override suspend fun addProductToInventory(userId: Int, product: Product): Product{
        val request = convertProductRequest(userId, product)
        var newProduct: Product = product
        if(request != null){
            Log.d("InventoryRepository", "Add product.")
            val result = inventoryDataSource.addProductToInventory(request)
            if(result.data != null && result.data.productId != 0) {
                newProduct = convertProductWithNewProductId(result.data, product)
            }
        }
        return newProduct
    }

    override suspend fun updateProductImage(userId: Int, image: Image) {
        // update product image
        val request = convertUpdateImageRequest(userId, image)
        if(request != null){
            Log.d("InventoryRepository", "Update product image.")
            inventoryDataSource.updateProductImage(request)
        }
    }

    override suspend fun updateProductInventoryList(userId: Int, inventoryList: List<Product>) {
        val request = InventoryListRequest(userId, inventoryList)
        Log.d("InventoryRepository", "Update inventorylist.")
        inventoryDataSource.updateInventoryList(request)
    }

    override suspend fun removeProductFromInventory(userId: Int, product: Product) {
        val request = DeleteProductRequest(userId, product.productId)
        Log.d("InventoryRepository", "Remove product.")
        val result = inventoryDataSource.removeProductFromInventoryList(request)
        Log.d("Delete Product from Inventory", result.data.toString())
    }

    override suspend fun getBarcodeScanResult(result: String): Product? {
        // get product from scaned barcode
        val result = inventoryDataSource.getProductFromBarcodeScanResult(result)
        Log.d("InventoryRepository", "Get product $result from barcode scan.")
        return convertBarcodeResultToProduct(result.data)
    }
}