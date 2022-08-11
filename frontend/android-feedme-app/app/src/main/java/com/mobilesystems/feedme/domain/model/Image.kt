package com.mobilesystems.feedme.domain.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Image(val imageId: Int,
                 val imageName: String,
                 val imageUrl: String,
                 val bitmap: Bitmap? = null) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (imageId != other.imageId) return false
        if (imageName != other.imageName) return false
        if (imageUrl != other.imageUrl) return false
        if (bitmap != other.bitmap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageId
        result = 31 * result + imageName.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + (bitmap?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Image(imageId=$imageId, imageName='$imageName', imageUrl='$imageUrl', bitmap=$bitmap)"
    }

}
