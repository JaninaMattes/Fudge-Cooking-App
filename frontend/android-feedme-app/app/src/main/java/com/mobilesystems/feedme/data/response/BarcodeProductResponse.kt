package com.mobilesystems.feedme.data.response

data class BarcodeProductResponse(
    val foodId: String,
    val label: String,
    val brand: String,
    val nutrients: String,
    val category: String,
    val categoryLabel: String,
)
