package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductTagRequest(
    private val tagId: Int,
    private val label: String
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductTagRequest

        if (tagId != other.tagId) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tagId
        result = 31 * result + label.hashCode()
        return result
    }

    override fun toString(): String {
        return "ProductTagRequest (tagId=$tagId, label='$label')"
    }

}
