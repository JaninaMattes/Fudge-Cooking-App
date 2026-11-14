package com.foodtracker.foodtrackerapi.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the demo data in {@code db/data.sql}.
 *
 * <p>The demo of this project stands or falls with this file. Every product
 * once held a date of February 2022, so each one was years past its date and
 * the dashboard showed a pantry of rotten food. These tests hold the file to
 * three promises: no date is written into the file, the pantry of the demo
 * user shows every state at once, and the dietary requirements of a user do
 * not contradict each other.
 *
 * @author Janina Mattes
 */
class DemoDataTest {

    /** The warning window of the application, in days. */
    private static final int WARNING_DAYS = 3;

    /** The file with the demo data, relative to the module. */
    private static final Path DATA_FILE = Path.of("db", "data.sql");

    /**
     * One row of {@code et_product}: the key, the three list keys, the name
     * and the date.
     */
    private static final Pattern PRODUCT_ROW = Pattern.compile(
            "\\((\\d+),\\s*(NULL|\\d+),\\s*(NULL|\\d+),\\s*(NULL|\\d+),"
            + "\\s*'([^']+)',\\s*(CURRENT_DATE[^,]*)");

    /** One row of a table with two keys, such as {@code et_user_tag}. */
    private static final Pattern PAIR_ROW = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)");

    /** One row of {@code et_dietry_tag}: the key and the label. */
    private static final Pattern TAG_ROW = Pattern.compile("\\((\\d+),\\s*'([^']+)'\\)");

    private static String sql;

    @BeforeAll
    static void readTheFile() throws IOException {
        sql = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
    }

    /**
     * Gives the rows of one insert statement.
     *
     * @param table the name of the table
     * @return the text between the name of the table and the semicolon
     */
    private static String rowsOf(String table) {
        Matcher matcher = Pattern
                .compile("INSERT INTO public\\." + table + "\\b[^;]+;", Pattern.DOTALL)
                .matcher(sql);
        assertThat(matcher.find()).as("the table %s stands in the file", table).isTrue();
        return matcher.group();
    }

    /**
     * Reads the number of days out of the SQL of one date.
     *
     * @param date for example {@code CURRENT_DATE - INTERVAL '2 days'}
     * @return the days from today, negative for a date that has passed
     */
    private static int daysFromToday(String date) {
        Matcher matcher = Pattern.compile("([+-])\\s*INTERVAL\\s*'(\\d+) day").matcher(date);
        if (!matcher.find()) {
            return 0;
        }
        int days = Integer.parseInt(matcher.group(2));
        return "-".equals(matcher.group(1)) ? -days : days;
    }

    /**
     * Gives the days from today of every product of one inventory.
     *
     * @param inventoryId the key of the inventory list
     * @return the days of each product of that list
     */
    private static List<Integer> pantryOf(String inventoryId) {
        List<Integer> days = new ArrayList<>();
        Matcher matcher = PRODUCT_ROW.matcher(rowsOf("et_product"));
        while (matcher.find()) {
            if (inventoryId.equals(matcher.group(4))) {
                days.add(daysFromToday(matcher.group(6)));
            }
        }
        return days;
    }

    @Test
    @DisplayName("no date is written into the demo data")
    void noDateIsWrittenIntoTheFile() {
        assertThat(sql).doesNotContain("TO_DATE");
        assertThat(sql).as("a date such as 08.02.2022 would age").doesNotContainPattern(
                "\\d{2}\\.\\d{2}\\.\\d{4}");
    }

    @Test
    @DisplayName("every product counts its date from today")
    void everyProductCountsFromToday() {
        String products = rowsOf("et_product");
        long rows = PRODUCT_ROW.matcher(products).results().count();

        assertThat(rows).as("the file holds products").isPositive();
        assertThat(countOf(products, "CURRENT_DATE"))
                .as("each of the %d products counts from today", rows)
                .isEqualTo(rows);
    }

    private static long countOf(String text, String part) {
        return Pattern.compile(Pattern.quote(part)).matcher(text).results().count();
    }

    @Test
    @DisplayName("the pantry of the demo user shows a bad, an expiring and a fresh product")
    void pantryOfTheDemoUserShowsEveryState() {
        List<Integer> days = pantryOf("1");

        assertThat(days).as("the demo user owns products").isNotEmpty();
        assertThat(days.stream().filter(day -> day < 0).count())
                .as("products that are already bad").isGreaterThanOrEqualTo(2);
        assertThat(days.stream().filter(day -> day >= 0 && day <= WARNING_DAYS).count())
                .as("products inside the warning window of %d days", WARNING_DAYS)
                .isGreaterThanOrEqualTo(3);
        assertThat(days.stream().filter(day -> day > WARNING_DAYS).count())
                .as("products that are still fresh").isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("both pantries hold products, so either user can show the demo")
    void bothPantriesHoldProducts() {
        assertThat(pantryOf("1")).isNotEmpty();
        assertThat(pantryOf("2")).isNotEmpty();
    }

    @Test
    @DisplayName("three recipes are vegetarian, so a vegetarian user still receives three")
    void threeRecipesAreVegetarian() {
        assertThat(countOf(rowsOf("et_recipe"), "'Vegetarian'"))
                .as("a vegetarian user needs the three recipes of FR-3.5")
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("the demo data speaks English")
    void theDemoDataSpeaksEnglish() {
        assertThat(sql).as("the application is English, see FR-0.12")
                .doesNotContainPattern("[äöüÄÖÜß]");
        assertThat(sql).doesNotContain("Produktbild", "Profilbild", "Vegetarisch",
                "Beliebt");
    }

    @Test
    @DisplayName("no user wants vegan food and meat at the same time")
    void noUserWantsVeganAndMeat() {
        Map<String, Integer> tags = new HashMap<>();
        Matcher tagMatcher = TAG_ROW.matcher(rowsOf("et_dietry_tag"));
        while (tagMatcher.find()) {
            tags.putIfAbsent(tagMatcher.group(2), Integer.valueOf(tagMatcher.group(1)));
        }
        assertThat(tags).containsKeys("Vegan", "Meat");

        Map<String, Set<Integer>> ofUser = new HashMap<>();
        Matcher userTags = PAIR_ROW.matcher(rowsOf("et_user_tag"));
        while (userTags.find()) {
            ofUser.computeIfAbsent(userTags.group(1), key -> new HashSet<>())
                    .add(Integer.valueOf(userTags.group(2)));
        }

        assertThat(ofUser).isNotEmpty();
        ofUser.forEach((user, chosen) -> assertThat(
                chosen.contains(tags.get("Vegan")) && chosen.contains(tags.get("Meat")))
                .as("the user %s cannot be vegan and eat meat", user)
                .isFalse());
    }
}
