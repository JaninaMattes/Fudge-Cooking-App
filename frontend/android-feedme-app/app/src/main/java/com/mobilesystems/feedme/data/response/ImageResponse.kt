package com.mobilesystems.feedme.data.response

data class ImageResponse(val imageId: Int,
                         val imageName: String,
                         val imageUrl: String,
                         val base64ImageString: String? = null)
