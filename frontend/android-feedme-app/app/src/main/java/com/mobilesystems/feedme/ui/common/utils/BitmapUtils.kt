package com.mobilesystems.feedme.ui.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
    var width = bitmap.width
    var height = bitmap.height

    val bitmapRatio = width/height
    if(bitmapRatio > 1){
        width = maxSize
        height = width/bitmapRatio
    } else{
        height = maxSize
        width = height * bitmapRatio
    }
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}

fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray? {
    var byteArray: ByteArray? = null
    try {
        val stream = ByteArrayOutputStream()
        val compressFormat = Bitmap.CompressFormat.JPEG
        Log.d("Utils", "Convert with compress format $compressFormat.")
        bitmap.compress(compressFormat, 100, stream)
        byteArray = stream.toByteArray()
        Log.d("Utils", "Convert bitmap to byte array.")
        // bitmap.recycle()
    } catch (e: java.lang.Exception) {
        e.stackTrace
        Log.d("Utils", "Error: Convert bitmap to byte array.")
    }
    return byteArray
}

fun convertByteArrayToBitmap(bytes: ByteArray): Bitmap?{
    var bitmap: Bitmap? = null
    try {
        bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
        Log.d("Utils", "Convert byte array to bitmap.")
    } catch (e: java.lang.Exception) {
        e.stackTrace
        Log.d("Utils", "Error: Convert byte array to bitmap.")
    }
    return bitmap
}

fun bytesToBase64(bytes: ByteArray?): String? {
    var result: String? = null
    try {
        result = Base64.getEncoder().encodeToString(bytes)
        Log.d("Utils", "Convert byte array to base64 string.")
    }catch (e: Exception){
        e.printStackTrace()
        Log.d("Utils", "Error: Convert byte array to base64 string.")
    }
    return result
}

fun base64ToBytes(base64Str: String?): ByteArray? {
    var result: ByteArray? = null
    try{
        result = Base64.getDecoder().decode(base64Str)
        Log.d("Utils", "Convert base64 string to bytes.")
    }catch (e: Exception){
        e.printStackTrace()
        Log.d("Utils", "Error: Convert base64 string to bytes.")
    }
    return result
}

fun filePathToBitmap(pathname: String): Bitmap {
    val imgFile = File(pathname)
    return BitmapFactory.decodeFile(imgFile.absolutePath)
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }
    val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}