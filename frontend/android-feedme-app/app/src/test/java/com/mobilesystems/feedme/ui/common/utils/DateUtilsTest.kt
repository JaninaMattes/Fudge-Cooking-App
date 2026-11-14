package com.mobilesystems.feedme.ui.common.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Tests for the functions of DateUtils.kt.
 *
 * The functions now use `java.text.SimpleDateFormat`, so the tests run on the
 * development machine and need no emulator.
 */
class DateUtilsTest {
    private val displayFormat = SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.GERMANY)

    /**
     * Gives a date in the display format, from today plus a number of days.
     */
    private fun displayDateInDays(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, days)
        return displayFormat.format(calendar.time)
    }

    // --- getTimeDiff -----------------------------------------------------

    @Test
    fun `a date of today gives zero days`() {
        assertThat(getTimeDiff(displayDateInDays(0))).isEqualTo(0L)
    }

    @Test
    fun `a date of the future gives a positive number of days`() {
        assertThat(getTimeDiff(displayDateInDays(5))).isEqualTo(5L)
    }

    @Test
    fun `a date of the past gives a negative number of days`() {
        assertThat(getTimeDiff(displayDateInDays(-3))).isEqualTo(-3L)
    }

    @Test
    fun `an empty text gives zero days`() {
        assertThat(getTimeDiff("")).isEqualTo(0L)
    }

    @Test
    fun `a text that is not a date gives zero days`() {
        // The first version stopped the application with an exception here.
        assertThat(getTimeDiff("not-a-date")).isEqualTo(0L)
        assertThat(getTimeDiff("2022-02-02")).isEqualTo(0L)
    }

    @Test
    fun `a day that does not exist gives zero days`() {
        assertThat(getTimeDiff("32.01.2022")).isEqualTo(0L)
    }

    // --- convertDateFormat and reconvertDateFormat ------------------------

    @Test
    fun `a date of the API becomes a date for the screen`() {
        assertThat(convertDateFormat("2022-02-02")).isEqualTo("02.02.2022")
    }

    @Test
    fun `a date of the screen becomes a date for the API`() {
        assertThat(reconvertDateFormat("02.02.2022")).isEqualTo("2022-02-02")
    }

    @Test
    fun `the two directions give the first value back`() {
        val isoDate = "2021-12-24"

        assertThat(reconvertDateFormat(convertDateFormat(isoDate))).isEqualTo(isoDate)
    }

    @Test
    fun `a text that is not a date stays as it is`() {
        // The first version wrote the current date, so the user saw a wrong
        // value and did not see the error.
        assertThat(convertDateFormat("not-a-date")).isEqualTo("not-a-date")
        assertThat(reconvertDateFormat("not-a-date")).isEqualTo("not-a-date")
        assertThat(convertDateFormat("")).isEmpty()
    }

    // --- convertStringToDate ---------------------------------------------

    @Test
    fun `a date of the screen becomes a date object`() {
        val date = convertStringToDate("02.02.2022")

        assertThat(date).isNotNull()
        assertThat(displayFormat.format(date!!)).isEqualTo("02.02.2022")
    }

    @Test
    fun `a text that is not a date gives null`() {
        assertThat(convertStringToDate("not-a-date")).isNull()
        assertThat(convertStringToDate("")).isNull()
    }

    // --- addDaysToCurrentDate and getCurrentDate --------------------------

    @Test
    fun `the function adds the days to the current date`() {
        val today = getCurrentDate()
        val inFiveDays = addDaysToCurrentDate(5)

        val difference = inFiveDays.timeInMillis - today.timeInMillis
        val days = difference / (1000L * 60L * 60L * 24L)
        assertThat(days).isEqualTo(5L)
    }

    @Test
    fun `a negative number of days gives a date of the past`() {
        val today = getCurrentDate()
        val threeDaysAgo = addDaysToCurrentDate(-3)

        assertThat(threeDaysAgo.timeInMillis).isLessThan(today.timeInMillis)
    }
}
