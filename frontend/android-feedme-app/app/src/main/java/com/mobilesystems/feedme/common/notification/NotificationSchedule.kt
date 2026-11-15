package com.mobilesystems.feedme.common.notification

import java.util.Calendar

/**
 * Says how long it is until the next time of day.
 *
 * The application sends one notification a day, at the hour of the user. The
 * default is 08:00 of the local time, so that the user can shop after work
 * (FR-0.8 and FR-D of `docs/PRODUCT.md`). The recipes are read a quarter of
 * an hour earlier, so that the notification holds fresh suggestions.
 *
 * The hour of the user is **not** stored yet. Until the column of FR-0.8
 * exists, every user receives the default.
 */
object NotificationSchedule {
    /** The hour of the notification, in the local time of the telephone. */
    const val NOTIFICATION_HOUR = 8

    /** The minute of the notification. */
    const val NOTIFICATION_MINUTE = 0

    /** The hour at which the recipes are read. */
    const val REFRESH_HOUR = 7

    /** The minute at which the recipes are read. */
    const val REFRESH_MINUTE = 45

    /**
     * Gives the milliseconds until the next time of day.
     *
     * When the time has passed today, the answer points at tomorrow. When
     * the time is exactly now, the answer points at tomorrow as well, so that
     * a notification never fires twice in one day.
     *
     * @param hour the hour of the day, from 0 to 23
     * @param minute the minute of the hour
     * @param now the moment to count from
     * @return the milliseconds until that time, always more than zero
     */
    fun millisUntil(
        hour: Int,
        minute: Int,
        now: Calendar = Calendar.getInstance(),
    ): Long {
        val due = now.clone() as Calendar
        due.set(Calendar.HOUR_OF_DAY, hour)
        due.set(Calendar.MINUTE, minute)
        due.set(Calendar.SECOND, 0)
        due.set(Calendar.MILLISECOND, 0)
        if (!due.after(now)) {
            due.add(Calendar.DAY_OF_MONTH, 1)
        }
        return due.timeInMillis - now.timeInMillis
    }

    /**
     * Gives the milliseconds until the next notification.
     *
     * @param now the moment to count from
     * @return the milliseconds until the next 08:00
     */
    fun notificationDelayMillis(now: Calendar = Calendar.getInstance()): Long =
        millisUntil(NOTIFICATION_HOUR, NOTIFICATION_MINUTE, now)

    /**
     * Gives the milliseconds until the recipes are read again.
     *
     * @param now the moment to count from
     * @return the milliseconds until the next 07:45
     */
    fun refreshDelayMillis(now: Calendar = Calendar.getInstance()): Long =
        millisUntil(REFRESH_HOUR, REFRESH_MINUTE, now)
}
