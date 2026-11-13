package com.mobilesystems.feedme.common.logging

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

/**
 * Tests of the log of the client. The test reads the lines that the client
 * wrote with the shadow of Logcat that Robolectric gives.
 */
@RunWith(RobolectricTestRunner::class)
class AppLogTest {
    @Before
    fun clearTheLog() {
        ShadowLog.clear()
    }

    private fun lastLine() = ShadowLog.getLogs().last()

    @Test
    fun `every line stands under the one name of the client`() {
        logInfo("the user opened the dashboard")

        assertThat(lastLine().tag).isEqualTo(LOG_TAG)
    }

    @Test
    fun `a line of an error holds the operation, the reason and the error`() {
        val error = IllegalStateException("no connection")

        logError("read the inventory list", error)

        val line = lastLine()
        assertThat(line.type).isEqualTo(Log.ERROR)
        assertThat(line.msg).isEqualTo("Failed to read the inventory list: no connection")
        assertThat(line.throwable).isSameInstanceAs(error)
    }

    @Test
    fun `an error without an exception holds its reason`() {
        logError("send GET /api/users/1", "status 500")

        assertThat(lastLine().msg).isEqualTo("Failed to send GET /api/users/1: status 500")
    }

    @Test
    fun `a warning without an error holds no error`() {
        logWarn("the picture of the product is missing")

        assertThat(lastLine().type).isEqualTo(Log.WARN)
        assertThat(lastLine().throwable).isNull()
    }

    @Test
    fun `a warning with an error holds the error`() {
        val error = IllegalArgumentException("wrong date")

        logWarn("the date of the product has a wrong form", error)

        assertThat(lastLine().throwable).isSameInstanceAs(error)
    }

    @Test
    fun `a line for the development has the level debug`() {
        logDebug("GET /api/users/1 answered 200 after 12 ms")

        assertThat(lastLine().type).isEqualTo(Log.DEBUG)
    }

    @Test
    fun `an operation that works gives its own result`() {
        val result = attemptOrDefault("read a number", 0) { 42 }

        assertThat(result).isEqualTo(42)
    }

    @Test
    fun `an operation that throws gives the fallback value and writes a line`() {
        val result = attemptOrDefault("read a number", -1) { error("broken") }

        assertThat(result).isEqualTo(-1)
        assertThat(lastLine().msg).contains("Failed to read a number")
    }
}
