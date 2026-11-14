package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.logging.logError
import com.mobilesystems.feedme.common.networkresult.Resource
import retrofit2.Response

/**
 * Base class of each data source. It calls the REST API and turns the answer
 * into a [Resource], so that a ViewModel never sees an exception.
 *
 * Every failure reaches Logcat under the name "Fudge" and holds the method and
 * the path of the request. The first version wrote the message only, so a
 * reader did not see which endpoint failed.
 */
abstract class BaseDataSource {
    /**
     * Calls the REST API and gives the answer as a [Resource].
     *
     * @param T type of the body of the answer
     * @param call operation that sends the request
     * @return the body of the answer, or an error with its reason
     */
    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Resource<T> =
        try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.success(body)
            } else {
                failed(nameOf(response), "status ${response.code()} ${response.message()}")
            }
        } catch (error: Exception) {
            failed("reach the server", error.message ?: error.toString())
        }

    private fun <T> failed(
        name: String,
        reason: String,
    ): Resource<T> {
        logError(name, reason)
        return Resource.error("Failed to $name: $reason")
    }

    private fun <T> nameOf(response: Response<T>): String {
        val request = response.raw().request
        return "send ${request.method} ${request.url.encodedPath}"
    }
}
