package com.mobilesystems.feedme.common.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Tests for [NotificationSchedule].
 *
 * The happy path is one line: at six in the morning the notification of eight
 * is two hours away. The other cases are the ones that a day produces — the
 * hour has passed, the hour is exactly now, and the day is the last of a
 * month.
 */
class NotificationScheduleTest {
    private fun momentOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): Calendar {
        val moment = Calendar.getInstance()
        moment.clear()
        moment.set(year, month - 1, day, hour, minute, 0)
        return moment
    }

    private fun hoursOf(millis: Long): Double = millis.toDouble() / TimeUnit.HOURS.toMillis(1).toDouble()

    @Test
    fun `gives the hours until the notification of today`() {
        val now = momentOf(2026, 11, 14, 6, 0)

        assertEquals(2.0, hoursOf(NotificationSchedule.notificationDelayMillis(now)), 0.01)
    }

    @Test
    fun `points at tomorrow when the hour has passed`() {
        val now = momentOf(2026, 11, 14, 9, 30)

        assertEquals(22.5, hoursOf(NotificationSchedule.notificationDelayMillis(now)), 0.01)
    }

    @Test
    fun `points at tomorrow when the hour is exactly now`() {
        val now = momentOf(2026, 11, 14, 8, 0)

        assertEquals(24.0, hoursOf(NotificationSchedule.notificationDelayMillis(now)), 0.01)
    }

    @Test
    fun `counts over the end of a month`() {
        val now = momentOf(2026, 1, 31, 23, 30)

        assertEquals(8.5, hoursOf(NotificationSchedule.notificationDelayMillis(now)), 0.01)
    }

    @Test
    fun `reads the recipes a quarter of an hour before the notification`() {
        val now = momentOf(2026, 11, 14, 6, 0)
        val refresh = NotificationSchedule.refreshDelayMillis(now)
        val notification = NotificationSchedule.notificationDelayMillis(now)

        assertEquals(TimeUnit.MINUTES.toMillis(15), notification - refresh)
    }

    @Test
    fun `the answer is never zero and never longer than a day`() {
        for (hour in 0..23) {
            val millis =
                NotificationSchedule.millisUntil(
                    NotificationSchedule.NOTIFICATION_HOUR,
                    NotificationSchedule.NOTIFICATION_MINUTE,
                    momentOf(2026, 11, 14, hour, 0),
                )

            assertTrue("the answer of $hour o'clock is more than zero", millis > 0)
            assertTrue(
                "the answer of $hour o'clock is at most a day",
                millis <= TimeUnit.DAYS.toMillis(1),
            )
        }
    }
}
