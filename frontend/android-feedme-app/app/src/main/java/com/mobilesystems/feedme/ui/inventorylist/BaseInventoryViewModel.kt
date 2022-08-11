package com.mobilesystems.feedme.ui.inventorylist

import androidx.lifecycle.LiveData
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.Label
import com.mobilesystems.feedme.domain.model.Product

interface BaseInventoryViewModel {

    fun selectProduct(product: Product) // Select single product from inventory

    fun getProductFromBarcodeScanResult(barcodeScanRes: String) : Product?

    fun addProductFromBarcodeScanResultToInventory(product: Product)

    fun loadAllProductsOfInventoryList()

    fun loadSelectedProductTagList(): LiveData<List<Label>?> // helper function

    fun addProductToInventoryList(product: Product)

    fun updateProductOnInventory(product: Product)

    fun updateInventoryList()

    fun updateImage(image: Image)

    fun deleteProductInInventoryList(product: Product)

    fun deleteProductByPosition(position: Int): Product? // helper function
}
