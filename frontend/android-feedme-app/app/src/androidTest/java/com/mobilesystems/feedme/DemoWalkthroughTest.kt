package com.mobilesystems.feedme

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks through the application for the demo video.
 *
 * The test does the steps that a new user does: it logs in, reads the
 * inventory list, opens the recipe suggestions and opens the shopping list.
 * The script `tools/demo/record-demo.sh` records the screen while this test
 * runs, so the video shows a real walk through the application.
 *
 * The test waits between the steps, because a video that changes too fast is
 * difficult to watch.
 *
 * Run it with `make frontend-ui-test`. An emulator or a telephone must run.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class DemoWalkthroughTest {

    private companion object {
        /** Time that the video shows each screen. */
        const val SCREEN_PAUSE_MILLIS = 2_500L

        /** Time that the application needs to read the data of a screen. */
        const val LOAD_PAUSE_MILLIS = 4_000L

        const val DEMO_EMAIL = "demo@fudge.app"
        const val DEMO_PASSWORD = "demo1234"
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    /**
     * Stops the test for a short time, so that the video shows the screen.
     *
     * @param millis time in milliseconds
     */
    private fun pause(millis: Long) {
        Thread.sleep(millis)
    }

    @Test
    fun walksThroughTheApplication() {
        pause(SCREEN_PAUSE_MILLIS)

        // 1. The user writes the login data.
        onView(withId(R.id.email)).check(matches(isDisplayed()))
        onView(withId(R.id.email)).perform(typeText(DEMO_EMAIL), closeSoftKeyboard())
        pause(1_000L)
        onView(withId(R.id.password)).perform(typeText(DEMO_PASSWORD), closeSoftKeyboard())
        pause(SCREEN_PAUSE_MILLIS)

        // 2. The user logs in. The dashboard reads the data of the server.
        onView(withId(R.id.login)).perform(click())
        pause(LOAD_PAUSE_MILLIS)

        // 3. The user reads each screen of the navigation bar.
        showScreen(R.id.navigation_inventorylist)
        showScreen(R.id.navigation_recipes)
        showScreen(R.id.navigation_shoppingList)
        showScreen(R.id.navigation_dashboard)
    }

    /**
     * Opens one screen of the navigation bar and waits.
     *
     * @param navigationItemId identifier of the item of the navigation bar
     */
    private fun showScreen(navigationItemId: Int) {
        runCatching {
            onView(withId(navigationItemId)).perform(click())
            pause(LOAD_PAUSE_MILLIS)
        }.onFailure {
            // The demo must not stop when one screen needs a server. The
            // video then shows the other screens.
            pause(SCREEN_PAUSE_MILLIS)
        }
    }
}
