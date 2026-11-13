package com.mobilesystems.feedme.common.logging

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests of [RequestIdInterceptor]. The test uses a small server of the library
 * OkHttp, so it needs no network and no server of the project.
 */
class RequestIdInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun startTheServer() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder().addInterceptor(RequestIdInterceptor()).build()
    }

    @After
    fun stopTheServer() {
        server.shutdown()
    }

    private fun call(builder: Request.Builder.() -> Unit = {}) {
        val request = Request.Builder().url(server.url("/api/users/1")).apply(builder).build()
        client.newCall(request).execute().use { it.body?.string() }
    }

    @Test
    fun `each request holds an ID of eight characters`() {
        server.enqueue(MockResponse().setBody("{}"))

        call()

        val sent = server.takeRequest().getHeader(RequestIdInterceptor.REQUEST_ID_HEADER)
        assertThat(sent).isNotNull()
        assertThat(sent).hasLength(RequestIdInterceptor.ID_LENGTH)
    }

    @Test
    fun `the interceptor keeps an ID that the caller gives`() {
        server.enqueue(MockResponse().setBody("{}"))

        call { header(RequestIdInterceptor.REQUEST_ID_HEADER, "own-id") }

        assertThat(server.takeRequest().getHeader(RequestIdInterceptor.REQUEST_ID_HEADER))
            .isEqualTo("own-id")
    }

    @Test
    fun `two requests get two different IDs`() {
        assertThat(RequestIdInterceptor.newRequestId())
            .isNotEqualTo(RequestIdInterceptor.newRequestId())
    }
}
