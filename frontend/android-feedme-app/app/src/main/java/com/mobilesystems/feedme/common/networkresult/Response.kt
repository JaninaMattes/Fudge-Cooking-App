package com.mobilesystems.feedme.common.networkresult

/**
 * A generic class that holds a value with a success, error or loading status.
 * This class can be put around any type of object which represents results from network call.
 *
 * E.g. searching for products in inventorylist might involve Rest call for view to differentiate
 * between data being loaded, network error and empty list.
 *
 * Documentation: https://developer.android.com/jetpack/guide#addendum
 *
 * @param <T>
 * @param message
 */

sealed class Response<T>(val data: T? = null, val message: String? = null){
    class Success<T>(data: T) : Response<T>(data)
    class Error<T>(message: String, data: T? = null) : Response<T>(data, message)
    class Loading<T>(data: T? = null) : Response<T>(data)
}

