package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageRequest( private val imageId: Int,
                         private val imageName: String,
                         private val imageUrl: String,
                         private val imageBytes: String?) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageRequest

        if (imageId != other.imageId) return false
        if (imageName != other.imageName) return false
        if (imageUrl != other.imageUrl) return false
        if (imageBytes != other.imageBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageId
        result = 31 * result + imageName.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + (imageBytes?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ImageRequest(imageId=$imageId, imageName='$imageName', imageUrl='$imageUrl', imageBytes=$imageBytes)"
    }

}