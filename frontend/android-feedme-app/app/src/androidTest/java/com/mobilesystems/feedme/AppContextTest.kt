package com.mobilesystems.feedme

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves that the test runs against the correct application.
 *
 * The test is short on purpose. It tells the team that the build of the tests
 * for a telephone works, before a longer test such as
 * [DemoWalkthroughTest] runs.
 */
@RunWith(AndroidJUnit4::class)
class AppContextTest {
    @Test
    fun theTestRunsAgainstTheFudgeApplication() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("com.mobilesystems.feedme", appContext.packageName)
    }
}
