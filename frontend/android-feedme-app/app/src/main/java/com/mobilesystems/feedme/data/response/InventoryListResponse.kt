package com.mobilesystems.feedme.data.response

data class InventoryListResponse(
    val inventoryId : Int,
    val inventoryList : List<ProductResponse>)
