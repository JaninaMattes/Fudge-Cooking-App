package com.foodtracker.foodtrackerapi.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.common.diet.DietRules;
import com.foodtracker.foodtrackerapi.models.Ingredient;

/**
 * Tests for the demo data in {@code db/data.sql}.
 *
 * <p>The demo of this project stands or falls with this file, and these tests
 * hold it to the promises that a demo needs: no date is written into it, the
 * pantry of the demo user shows every state at once, the application speaks
 * English, every recipe carries three or four tags, and a recipe that calls
 * itself vegan really is vegan — the last one checked against the same rules
 * that the application uses.
 *
 * @author Janina Mattes
 */
class DemoDataTest {

    /** The warning window of the application, in days. */
    private static final int WARNING_DAYS = 3;

    /** The smallest and the largest number of tags of one recipe. */
    private static final int FEWEST_TAGS = 3;

    /** The largest number of tags of one recipe. */
    private static final int MOST_TAGS = 4;

    /** The sum of the weights of the wishes of one user. */
    private static final int FULL_WEIGHT = 100;

    /** The file with the demo data, relative to the module. */
    private static final Path DATA_FILE = Path.of("db", "data.sql");

    /**
     * One row of {@code et_product}: the key, the three list keys, the name
     * and the date.
     */
    private static final Pattern PRODUCT_ROW = Pattern.compile(
            "\\((\\d+),\\s*(NULL|\\d+),\\s*(NULL|\\d+),\\s*(NULL|\\d+),"
            + "\\s*'([^']+)',\\s*(CURRENT_DATE[^,]*)");

    /** One row of a table with two keys, such as {@code et_recipe_tag}. */
    private static final Pattern PAIR_ROW = Pattern.compile("\\((\\d+),\\s*(\\d+)\\)");

    /** One row of {@code et_dietry_tag}: the key, the label and the kind. */
    private static final Pattern TAG_ROW =
            Pattern.compile("\\((\\d+),\\s*'([^']+)',\\s*'([^']+)'\\)");

    /** One wish of a user: the user, the tag, the weight and the veto. */
    private static final Pattern WISH_ROW = Pattern.compile(
            "\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*(TRUE|FALSE)\\)");

    /** One row of {@code et_ingredient}: the key, the name and the amount. */
    private static final Pattern INGREDIENT_ROW =
            Pattern.compile("\\((\\d+),\\s*'([^']+)',\\s*'([^']+)'\\)");

    private static String sql;

    private static DietRules rules;

    @BeforeAll
    static void readTheFiles() throws IOException {
        sql = Files.readString(DATA_FILE, StandardCharsets.UTF_8);
        rules = DietRules.fromClasspath(DietRules.DEFAULT_RESOURCE);
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
     * Counts how often a piece of text stands in another text.
     *
     * @param text the text to look in
     * @param part the piece to count
     * @return the number of times the piece stands in the text
     */
    private static long countOf(String text, String part) {
        return Pattern.compile(Pattern.quote(part)).matcher(text).results().count();
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

    /**
     * Gives the label of each tag of the vocabulary.
     *
     * @return the key of the tag and its label
     */
    private static Map<Integer, String> tagLabels() {
        Map<Integer, String> labels = new HashMap<>();
        Matcher matcher = TAG_ROW.matcher(rowsOf("et_dietry_tag"));
        while (matcher.find()) {
            labels.put(Integer.valueOf(matcher.group(1)), matcher.group(2));
        }
        return labels;
    }

    /**
     * Gives the kind of each tag of the vocabulary.
     *
     * @return the key of the tag and its kind
     */
    private static Map<Integer, String> tagKinds() {
        Map<Integer, String> kinds = new HashMap<>();
        Matcher matcher = TAG_ROW.matcher(rowsOf("et_dietry_tag"));
        while (matcher.find()) {
            kinds.put(Integer.valueOf(matcher.group(1)), matcher.group(3));
        }
        return kinds;
    }

    /**
     * Gives the keys that belong together in a table of two keys.
     *
     * @param table the name of the table
     * @return the first key and the list of second keys
     */
    private static Map<Integer, List<Integer>> pairsOf(String table) {
        Map<Integer, List<Integer>> pairs = new HashMap<>();
        Matcher matcher = PAIR_ROW.matcher(rowsOf(table));
        while (matcher.find()) {
            pairs.computeIfAbsent(Integer.valueOf(matcher.group(1)), key -> new ArrayList<>())
                    .add(Integer.valueOf(matcher.group(2)));
        }
        return pairs;
    }

    /**
     * Gives the ingredients of one recipe as the application sees them.
     *
     * @param recipeId the key of the recipe
     * @return the ingredients of that recipe
     */
    private static List<Ingredient> ingredientsOf(Integer recipeId) {
        Map<Integer, String> names = new HashMap<>();
        Matcher matcher = INGREDIENT_ROW.matcher(rowsOf("et_ingredient"));
        while (matcher.find()) {
            names.put(Integer.valueOf(matcher.group(1)), matcher.group(2));
        }
        List<Ingredient> ingredients = new ArrayList<>();
        for (Integer id : pairsOf("et_recipe_ingredient")
                .getOrDefault(recipeId, List.of())) {
            Ingredient ingredient = new Ingredient();
            ingredient.setIngredientName(names.get(id));
            ingredients.add(ingredient);
        }
        return ingredients;
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
    @DisplayName("every recipe carries three or four tags")
    void everyRecipeCarriesThreeOrFourTags() {
        Map<Integer, List<Integer>> tagsOfRecipe = pairsOf("et_recipe_tag");

        assertThat(tagsOfRecipe).as("every recipe of the demo carries tags").hasSize(6);
        tagsOfRecipe.forEach((recipe, tags) -> assertThat(tags.size())
                .as("the number of tags of the recipe %s", recipe)
                .isBetween(FEWEST_TAGS, MOST_TAGS));
    }

    @Test
    @DisplayName("a recipe carries at most one diet, and never a diet and its parent")
    void aRecipeCarriesAtMostOneDiet() {
        Map<Integer, String> kinds = tagKinds();

        pairsOf("et_recipe_tag").forEach((recipe, tags) -> assertThat(
                tags.stream().filter(tag -> "diet".equals(kinds.get(tag))).count())
                .as("the diets of the recipe %s", recipe)
                .isLessThanOrEqualTo(1));
    }

    @Test
    @DisplayName("every tag of a recipe stands in the vocabulary")
    void everyTagOfARecipeStandsInTheVocabulary() {
        Map<Integer, String> labels = tagLabels();

        assertThat(labels).isNotEmpty();
        pairsOf("et_recipe_tag").forEach((recipe, tags) -> assertThat(labels.keySet())
                .as("the tags of the recipe %s", recipe).containsAll(tags));
    }

    @Test
    @DisplayName("a recipe that carries a diet really follows that diet")
    void aRecipeThatCarriesADietFollowsIt() {
        Map<Integer, String> labels = tagLabels();
        Map<Integer, String> kinds = tagKinds();

        pairsOf("et_recipe_tag").forEach((recipe, tags) -> tags.stream()
                .filter(tag -> "diet".equals(kinds.get(tag)))
                .forEach(tag -> assertThat(
                        rules.conflicts(List.of(labels.get(tag)), ingredientsOf(recipe)))
                        .as("the recipe %s carries the tag %s", recipe, labels.get(tag))
                        .isEmpty()));
    }

    @Test
    @DisplayName("the wishes of a user add up to one hundred")
    void theWishesOfAUserAddUpToOneHundred() {
        Map<Integer, Integer> sumOfUser = new HashMap<>();
        Matcher matcher = WISH_ROW.matcher(rowsOf("et_user_tag"));
        while (matcher.find()) {
            if ("FALSE".equals(matcher.group(4))) {
                sumOfUser.merge(Integer.valueOf(matcher.group(1)),
                        Integer.valueOf(matcher.group(3)), Integer::sum);
            }
        }

        assertThat(sumOfUser).as("the users hold wishes").isNotEmpty();
        sumOfUser.forEach((user, sum) -> assertThat(sum)
                .as("the weights of the wishes of the user %s", user)
                .isEqualTo(FULL_WEIGHT));
    }

    @Test
    @DisplayName("a medical requirement carries no weight, because it is a veto")
    void aMedicalRequirementCarriesNoWeight() {
        Matcher matcher = WISH_ROW.matcher(rowsOf("et_user_tag"));
        boolean seen = false;
        while (matcher.find()) {
            if ("TRUE".equals(matcher.group(4))) {
                seen = true;
                assertThat(Integer.parseInt(matcher.group(3)))
                        .as("the weight of the medical requirement of the user %s",
                                matcher.group(1))
                        .isZero();
            }
        }
        assertThat(seen).as("the demo shows at least one medical requirement").isTrue();
    }
}
