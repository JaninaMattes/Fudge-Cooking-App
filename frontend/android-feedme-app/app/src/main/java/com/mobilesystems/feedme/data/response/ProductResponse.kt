package com.mobilesystems.feedme.data.response

data class ProductResponse(
    val productId: Int,
    val expirationDate: String,
    val manufacturer: String,
    val nutritionValue: String,
    val productName: String,
    val productImage: ImageResponse? = null,
    val productTags: List<ProductTag>? = null,
    val quantity: String)