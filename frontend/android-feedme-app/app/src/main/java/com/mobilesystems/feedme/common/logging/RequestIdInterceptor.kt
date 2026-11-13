package com.mobilesystems.feedme.common.logging

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

/**
 * Gives each request an ID and sends the ID in the header `X-Request-Id`.
 *
 * The REST API writes the same ID into every line of its own log, and
 * [NetworkLogInterceptor] writes it into Logcat. One ID therefore connects the
 * log of the telephone and the log of the server. A bug report of a user needs
 * the ID only.
 */
class RequestIdInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(REQUEST_ID_HEADER) != null) {
            return chain.proceed(request)
        }
        val withId =
            request
                .newBuilder()
                .header(REQUEST_ID_HEADER, newRequestId())
                .build()
        return chain.proceed(withId)
    }

    companion object {
        /** Name of the header that holds the ID of the request. */
        const val REQUEST_ID_HEADER = "X-Request-Id"

        /** Number of characters of an ID. */
        const val ID_LENGTH = 8

        /**
         * Makes an ID of a request.
         *
         * @return eight characters of a random value
         */
        fun newRequestId(): String = UUID.randomUUID().toString().take(ID_LENGTH)
    }
}
