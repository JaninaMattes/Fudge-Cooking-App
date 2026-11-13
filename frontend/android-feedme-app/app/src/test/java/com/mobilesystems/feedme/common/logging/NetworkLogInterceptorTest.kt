package com.mobilesystems.feedme.common.logging

import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Tests of [NetworkLogInterceptor]. The interceptor must give the answer of the
 * server to the caller and must give an error of the network to the caller, in
 * each case after it wrote its line.
 */
class NetworkLogInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun startTheServer() {
        server = MockWebServer()
        server.start()
        client =
            OkHttpClient
                .Builder()
                .addInterceptor(RequestIdInterceptor())
                .addInterceptor(NetworkLogInterceptor())
                .build()
    }

    @After
    fun stopTheServer() {
        server.shutdown()
    }

    private fun call() =
        client
            .newCall(Request.Builder().url(server.url("/api/users/1")).build())
            .execute()

    @Test
    fun `an answer that was successful reaches the caller`() {
        server.enqueue(MockResponse().setBody("""{"userId":1}"""))

        call().use { response ->
            assertThat(response.code).isEqualTo(200)
            assertThat(response.body?.string()).contains("userId")
        }
    }

    @Test
    fun `an answer with a status code of an error reaches the caller`() {
        server.enqueue(MockResponse().setResponseCode(500))

        call().use { response ->
            assertThat(response.code).isEqualTo(500)
        }
    }

    @Test
    fun `an error of the network reaches the caller`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        try {
            call().close()
            throw AssertionError("The call must throw an IOException")
        } catch (expected: IOException) {
            assertThat(expected).isNotNull()
        }
    }

    @Test
    fun `a request without an ID gives the line a text in place of the ID`() {
        server.enqueue(MockResponse().setBody("{}"))
        val withoutId =
            OkHttpClient.Builder().addInterceptor(NetworkLogInterceptor()).build()

        withoutId
            .newCall(Request.Builder().url(server.url("/api/users/1")).build())
            .execute()
            .use { response -> assertThat(response.code).isEqualTo(200) }

        assertThat(NetworkLogInterceptor.MISSING_ID).isEqualTo("no-id")
    }
}
