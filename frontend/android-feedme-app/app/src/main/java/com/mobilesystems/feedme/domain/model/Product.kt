package com.mobilesystems.feedme.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val productId: Int,
    val productName: String,
    val expirationDate: String,
    val labels: MutableList<Label>?,
    val quantity: String,
    val manufacturer: String,
    val nutritionValue: String,
    val productImage: Image?) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (productId != other.productId) return false
        if (productName != other.productName) return false
        if (expirationDate != other.expirationDate) return false
        if (labels != other.labels) return false
        if (quantity != other.quantity) return false
        if (manufacturer != other.manufacturer) return false
        if (nutritionValue != other.nutritionValue) return false
        if (productImage != other.productImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId
        result = 31 * result + productName.hashCode()
        result = 31 * result + expirationDate.hashCode()
        result = 31 * result + (labels?.hashCode() ?: 0)
        result = 31 * result + quantity.hashCode()
        result = 31 * result + manufacturer.hashCode()
        result = 31 * result + nutritionValue.hashCode()
        result = 31 * result + (productImage?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Product(productId=$productId, productName='$productName', expirationDate='$expirationDate', " +
                "labels=$labels, quantity='$quantity', manufacturer='$manufacturer', " +
                "nutritionValue='$nutritionValue', productImage=$productImage)"
    }

}