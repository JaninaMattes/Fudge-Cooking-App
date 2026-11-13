package com.mobilesystems.feedme.common.logging

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Writes one line for each request to the REST API.
 *
 * The line holds the method, the path, the status code, the time and the ID of
 * the request. A request that was successful gives a line with the level
 * "debug"; a request that failed gives a line with the level "error". The
 * interceptor runs in every build, also in a release, because a user can send
 * only the log of a release build in a bug report.
 *
 * The line never holds the body and never holds a header, so no password and
 * no token reaches the log. [okhttp3.logging.HttpLoggingInterceptor] writes the
 * body, and `AppModule` adds that interceptor to a debug build only.
 */
class NetworkLogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val name = "${request.method} ${request.url.encodedPath}"
        val requestId = request.header(RequestIdInterceptor.REQUEST_ID_HEADER) ?: MISSING_ID
        val start = System.nanoTime()
        try {
            val response = chain.proceed(request)
            val millis = millisSince(start)
            if (response.isSuccessful) {
                logDebug("$name answered ${response.code} after $millis ms [$requestId]")
            } else {
                logError("send $name", "status ${response.code} after $millis ms [$requestId]")
            }
            return response
        } catch (error: IOException) {
            logError("reach $name", "${error.message} after ${millisSince(start)} ms [$requestId]")
            throw error
        }
    }

    private fun millisSince(startNanos: Long): Long = (System.nanoTime() - startNanos) / NANOS_IN_MILLI

    companion object {
        /** Text that stands in the line when the request holds no ID. */
        const val MISSING_ID = "no-id"

        private const val NANOS_IN_MILLI = 1_000_000L
    }
}
