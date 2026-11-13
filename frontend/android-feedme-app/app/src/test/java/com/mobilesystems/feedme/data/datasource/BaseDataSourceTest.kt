package com.mobilesystems.feedme.data.datasource

import com.google.common.truth.Truth.assertThat
import com.mobilesystems.feedme.common.networkresult.Resource
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.Response
import java.io.IOException

/**
 * Tests of [BaseDataSource]. The class turns the answer of the REST API into a
 * [Resource], so that a ViewModel never sees an exception.
 */
class BaseDataSourceTest {
    /** Gives the protected operation of the base class to the test. */
    private class TestDataSource : BaseDataSource() {
        suspend fun <T> result(call: suspend () -> Response<T>): Resource<T> = getResult(call)
    }

    private val dataSource = TestDataSource()

    private fun errorBody() = "{}".toResponseBody("application/json".toMediaType())

    @Test
    fun `an answer with a body gives the body`() =
        runBlocking {
            val resource = dataSource.result { Response.success("the body") }

            assertThat(resource.status).isEqualTo(Resource.Status.SUCCESS)
            assertThat(resource.data).isEqualTo("the body")
            assertThat(resource.message).isNull()
        }

    @Test
    fun `an answer with a status code of an error gives the code and the reason`() =
        runBlocking {
            val resource = dataSource.result { Response.error<String>(404, errorBody()) }

            assertThat(resource.status).isEqualTo(Resource.Status.ERROR)
            assertThat(resource.data).isNull()
            assertThat(resource.message).contains("404")
        }

    @Test
    fun `an answer that was successful and holds no body gives an error`() =
        runBlocking {
            val resource = dataSource.result { Response.success<String>(null) }

            assertThat(resource.status).isEqualTo(Resource.Status.ERROR)
        }

    @Test
    fun `the name of the endpoint stands in the message of the error`() =
        runBlocking {
            val resource = dataSource.result { Response.error<String>(500, errorBody()) }

            assertThat(resource.message).contains("send GET")
        }

    @Test
    fun `an error of the network gives an error and no exception`() =
        runBlocking {
            val resource = dataSource.result<String> { throw IOException("no connection") }

            assertThat(resource.status).isEqualTo(Resource.Status.ERROR)
            assertThat(resource.message).contains("no connection")
        }

    @Test
    fun `an exception without a message gives the name of its class`() =
        runBlocking {
            val resource = dataSource.result<String> { throw IOException() }

            assertThat(resource.message).contains("IOException")
        }
}
