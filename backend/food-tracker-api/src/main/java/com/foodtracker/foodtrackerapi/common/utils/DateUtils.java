package com.foodtracker.foodtrackerapi.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Converts the date values that come from the client into the format that the
 * application uses.
 * </p>
 *
 * <p>
 * The client sends a date as an ISO string, for example {@code 2022-02-02}. The
 * user interface shows a date in the German format, for example
 * {@code 02.02.2022}. The database stores a {@link java.sql.Date}.
 * </p>
 *
 * @author Janina Mattes
 */
public final class DateUtils {

    private static final Logger LOG = LogManager.getLogger(DateUtils.class);

    /** Format of the dates that the client sends, for example 2022-02-02. */
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Format of the dates that the user interface shows, for example 02.02.2022. */
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private DateUtils() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Adds the given number of days to the date.
     *
     * @param date date to start from
     * @param days number of days to add. A negative number removes days.
     * @return a new date, or {@code null} if the given date is {@code null}
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     * Changes a date string from the ISO format into the display format.
     *
     * @param dateStr date in the ISO format, for example 2022-02-02
     * @return the date in the display format, for example 02.02.2022. If the
     *         string is not a valid date, the method gives the current date in
     *         the display format.
     */
    public static String convertDateFormat(String dateStr) {
        LocalDate date = parseIsoDate(dateStr);
        if (date == null) {
            LOG.warn("Cannot read the date '{}'. The current date is used.", dateStr);
            date = LocalDate.now();
        }
        return DISPLAY_FORMAT.format(date);
    }

    /**
     * Tells if the string holds a valid date in the ISO format.
     *
     * @param dateStr string to test. It can be {@code null}.
     * @return {@code true} if the string is a valid ISO date
     */
    public static boolean isIsoDate(String dateStr) {
        return parseIsoDate(dateStr) != null;
    }

    /**
     * Reads a date in the ISO format.
     *
     * @param dateStr string to read. It can be {@code null}.
     * @return the date, or {@code null} if the string is not a valid ISO date
     */
    public static LocalDate parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), ISO_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Reads a date in the ISO format and gives a date for the database.
     *
     * @param dateStr  string to read. It can be {@code null}.
     * @param fallback date to give if the string is not a valid ISO date
     * @return the date for the database, or the fallback date
     */
    public static java.sql.Date toSqlDate(String dateStr, LocalDate fallback) {
        LocalDate date = parseIsoDate(dateStr);
        if (date == null) {
            LOG.warn("Cannot read the date '{}'. The date {} is used.", dateStr, fallback);
            date = fallback;
        }
        return date != null ? java.sql.Date.valueOf(date) : null;
    }

    /**
     * Converts a date into a date for the database.
     *
     * @param date date to convert. It can be {@code null}.
     * @return the date for the database, or {@code null} if the given date is
     *         {@code null}
     */
    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) {
            return null;
        }
        return date instanceof java.sql.Date ? (java.sql.Date) date
                : new java.sql.Date(date.getTime());
    }

    /**
     * Gives the current date at midnight.
     *
     * @return the current date, with the time set to 00:00
     */
    public static Date startOfToday() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
