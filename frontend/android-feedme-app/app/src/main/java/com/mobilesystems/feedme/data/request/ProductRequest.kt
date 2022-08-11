package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductRequest(
    private val userId: Int,
    private val productId: Int,
    private val productName: String,
    private val expirationDate: String,
    private val quantity: String,
    private val manufacturer: String,
    private val nutritionValue: String,
    private val productImage: ImageRequest? = null,
    private val productTags:  List<ProductTagRequest>? = null): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductRequest

        if (userId != other.userId) return false
        if (productId != other.productId) return false
        if (productName != other.productName) return false
        if (expirationDate != other.expirationDate) return false
        if (quantity != other.quantity) return false
        if (manufacturer != other.manufacturer) return false
        if (nutritionValue != other.nutritionValue) return false
        if (productImage!= other.productImage) return false
        if (productTags != other.productTags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + productId.hashCode()
        result = 31 * result + productName.hashCode()
        result = 31 * result + expirationDate.hashCode()
        result = 31 * result + quantity.hashCode()
        result = 31 * result + manufacturer.hashCode()
        result = 31 * result + nutritionValue.hashCode()
        result = 31 * result + (productImage?.hashCode() ?: 0)
        result = 31 * result + (productTags?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ProductRequest( userId='$userId', productId='$productId', productName='$productName', " +
                "expirationDate='$expirationDate', quantity='$quantity', " +
                "manufacturer='$manufacturer', nutritionValue='$nutritionValue'," +
                "productImage='$productImage', productTags='$productTags' )"
    }
    }