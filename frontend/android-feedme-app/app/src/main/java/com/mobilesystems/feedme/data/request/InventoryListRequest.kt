package com.mobilesystems.feedme.data.request

import com.mobilesystems.feedme.domain.model.Product

data class InventoryListRequest(
    private val userId: Int,
    private val inventoryList: List<Product>
)
