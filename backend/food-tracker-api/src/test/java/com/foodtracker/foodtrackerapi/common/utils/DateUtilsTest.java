package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link DateUtils}.
 *
 * @author Janina Mattes
 */
class DateUtilsTest {

    private static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    @Test
    @DisplayName("addDays adds the days to the date")
    void addDaysAddsDays() {
        Date result = DateUtils.addDays(dateOf(2022, 2, 2), 3);

        assertThat(result).isEqualTo(dateOf(2022, 2, 5));
    }

    @Test
    @DisplayName("addDays removes the days when the number is negative")
    void addDaysRemovesDays() {
        Date result = DateUtils.addDays(dateOf(2022, 2, 2), -3);

        assertThat(result).isEqualTo(dateOf(2022, 1, 30));
    }

    @Test
    @DisplayName("addDays gives null when the date is null")
    void addDaysGivesNullForNull() {
        assertThat(DateUtils.addDays(null, 3)).isNull();
    }

    @Test
    @DisplayName("convertDateFormat changes the ISO format into the display format")
    void convertDateFormatChangesFormat() {
        assertThat(DateUtils.convertDateFormat("2022-02-02")).isEqualTo("02.02.2022");
    }

    @ParameterizedTest
    @DisplayName("convertDateFormat gives the current date for a value that is not a date")
    @ValueSource(strings = { "02.02.2022", "not-a-date", "2022-13-45" })
    void convertDateFormatFallsBackToToday(String invalidDate) {
        String expected = DateUtils.DISPLAY_FORMAT.format(LocalDate.now());

        assertThat(DateUtils.convertDateFormat(invalidDate)).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("isIsoDate gives false for an empty value")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "01.01.2022", "2022/01/01" })
    void isIsoDateGivesFalse(String invalidDate) {
        assertThat(DateUtils.isIsoDate(invalidDate)).isFalse();
    }

    @Test
    @DisplayName("isIsoDate gives true for a date in the ISO format")
    void isIsoDateGivesTrue() {
        assertThat(DateUtils.isIsoDate("2022-02-02")).isTrue();
    }

    @Test
    @DisplayName("parseIsoDate removes the spaces around the value")
    void parseIsoDateTrimsValue() {
        assertThat(DateUtils.parseIsoDate("  2022-02-02  ")).isEqualTo(LocalDate.of(2022, 2, 2));
    }

    @Test
    @DisplayName("toSqlDate reads a date in the ISO format")
    void toSqlDateReadsIsoDate() {
        java.sql.Date result = DateUtils.toSqlDate("2022-02-02", LocalDate.of(1999, 1, 1));

        assertThat(result).isEqualTo(java.sql.Date.valueOf(LocalDate.of(2022, 2, 2)));
    }

    @Test
    @DisplayName("toSqlDate gives the fallback date for a value that is not a date")
    void toSqlDateGivesFallback() {
        java.sql.Date result = DateUtils.toSqlDate("not-a-date", LocalDate.of(1999, 1, 1));

        assertThat(result).isEqualTo(java.sql.Date.valueOf(LocalDate.of(1999, 1, 1)));
    }

    @Test
    @DisplayName("toSqlDate gives null when the value and the fallback date are empty")
    void toSqlDateGivesNullWithoutFallback() {
        assertThat(DateUtils.toSqlDate(null, null)).isNull();
    }

    @Test
    @DisplayName("toSqlDate keeps a date that already is a database date")
    void toSqlDateKeepsSqlDate() {
        java.sql.Date sqlDate = java.sql.Date.valueOf("2022-02-02");

        assertThat(DateUtils.toSqlDate(sqlDate)).isSameAs(sqlDate);
    }

    @Test
    @DisplayName("toSqlDate converts a date into a database date")
    void toSqlDateConvertsUtilDate() {
        Date date = dateOf(2022, 2, 2);

        java.sql.Date result = DateUtils.toSqlDate(date);

        assertThat(result).isNotNull();
        assertThat(result.getTime()).isEqualTo(date.getTime());
    }

    @Test
    @DisplayName("toSqlDate gives null when the date is null")
    void toSqlDateGivesNullForNull() {
        assertThat(DateUtils.toSqlDate((Date) null)).isNull();
    }

    @Test
    @DisplayName("startOfToday gives the current date with the time 00:00")
    void startOfTodayHasNoTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.startOfToday());

        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isZero();
        assertThat(calendar.get(Calendar.MINUTE)).isZero();
        assertThat(calendar.get(Calendar.SECOND)).isZero();
    }
}
