package com.mobilesystems.feedme.common.networkresult

/**
 * A generic class that holds a value with its loading status.
 * This class can be put around any type of object which represents result from network request.
 * E.g. searching for products in inventorylist might involve Rest call.
 *
 * @param <T>
 */
sealed class ResultAuth<out T : Any> {

    data class Success<out T : Any>(val data: T) : ResultAuth<T>()
    data class Error(val exception: Exception) : ResultAuth<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}