package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.Product

interface InventoryRepository {

    suspend fun loadInventoryListProducts(userId: Int): List<Product>

    suspend fun removeProductFromInventory(userId: Int, product: Product)

    suspend fun updateProductOnInventory(userId: Int, product: Product)

    suspend fun updateProductImage(userId: Int, image: Image)

    suspend fun addProductToInventory(userId: Int, product: Product): Product

    suspend fun updateProductInventoryList(userId: Int, inventoryList: List<Product>)

    suspend fun getBarcodeScanResult(result: String): Product?
}