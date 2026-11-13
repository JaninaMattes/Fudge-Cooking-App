package com.mobilesystems.feedme.ui.common.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Converts the date values between the three formats of the application.
 *
 * The REST API sends and receives a date in the ISO format (`2022-02-02`). The
 * user interface shows a date in the German format (`02.02.2022`).
 *
 * The functions use `java.text.SimpleDateFormat` and not `android.icu.text`.
 * Both give the same result on a telephone, but only the first one runs in a
 * unit test on the development machine. The tests therefore need no emulator.
 */

/** Format of the dates that the REST API sends, for example 2022-02-02. */
const val ISO_DATE_PATTERN = "yyyy-MM-dd"

/** Format of the dates that the user interface shows, for example 02.02.2022. */
const val DISPLAY_DATE_PATTERN = "dd.MM.yyyy"

/**
 * Gives the number of days until a date.
 *
 * @param dateStr date in the display format, for example 02.02.2022
 * @return the number of days. The number is negative when the date is in the
 *         past, and 0 when the date is today or when the text is not a date.
 */
fun getTimeDiff(dateStr: String): Long {
    val endDate = parseDate(dateStr, DISPLAY_DATE_PATTERN) ?: return 0L
    val today = startOfDay(Calendar.getInstance().time)
    val endOfDay = startOfDay(endDate)
    return TimeUnit.MILLISECONDS.toDays(endOfDay.time - today.time)
}

/**
 * Adds a number of days to the current date.
 *
 * @param days number of days to add. A negative number removes days.
 * @return the new date
 */
fun addDaysToCurrentDate(days: Int): Calendar {
    val date = getCurrentDate()
    date.add(Calendar.DATE, days)
    return date
}

/**
 * Gives the current date.
 *
 * @return the current date
 */
fun getCurrentDate(): Calendar = Calendar.getInstance()

/**
 * Changes a date from the ISO format into the display format.
 *
 * @param dateStr date in the ISO format, for example 2022-02-02
 * @return the date in the display format, or the text itself when the text is
 *         not a date
 */
fun convertDateFormat(dateStr: String): String =
    reformat(dateStr, ISO_DATE_PATTERN, DISPLAY_DATE_PATTERN)

/**
 * Changes a date from the display format into the ISO format.
 *
 * @param dateStr date in the display format, for example 02.02.2022
 * @return the date in the ISO format, or the text itself when the text is not
 *         a date
 */
fun reconvertDateFormat(dateStr: String): String =
    reformat(dateStr, DISPLAY_DATE_PATTERN, ISO_DATE_PATTERN)

/**
 * Reads a date in the display format.
 *
 * @param dateStr date in the display format, for example 02.02.2022
 * @return the date, or `null` when the text is not a date
 */
fun convertStringToDate(dateStr: String): Date? = parseDate(dateStr, DISPLAY_DATE_PATTERN)

/**
 * Changes a date from one format into another format.
 *
 * @param dateStr date to change
 * @param from format of the text
 * @param to format of the result
 * @return the date in the new format, or the text itself when the text is not
 *         a date. The user then sees the original value and no wrong date.
 */
private fun reformat(dateStr: String, from: String, to: String): String {
    val date = parseDate(dateStr, from) ?: return dateStr
    return SimpleDateFormat(to, Locale.GERMANY).format(date)
}

/**
 * Reads a date in the given format.
 *
 * @param dateStr text to read. It can be empty.
 * @param pattern format of the text
 * @return the date, or `null` when the text is not a date
 */
private fun parseDate(dateStr: String, pattern: String): Date? {
    if (dateStr.isEmpty()) {
        return null
    }
    val format = SimpleDateFormat(pattern, Locale.GERMANY)
    // A date such as 32.01.2022 must not become 01.02.2022.
    format.isLenient = false
    return try {
        format.parse(dateStr)
    } catch (e: ParseException) {
        null
    }
}

/**
 * Sets the time of a date to 00:00.
 *
 * Two dates are then comparable by day. Without this step, a product that
 * expires tomorrow at 00:00 gives 0 days when the current time is 13:00.
 *
 * @param date date to change
 * @return the date with the time 00:00
 */
private fun startOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}
