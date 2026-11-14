package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Product;

/**
 * Tests for {@link IngredientMatcher}.
 *
 * <p>The happy path is one line. The rest of this class holds the cases that
 * a kitchen produces: a plural, a capital letter, a product that is already
 * bad, the same ingredient twice, and a list that holds nothing at all.
 *
 * @author Janina Mattes
 */
class IngredientMatcherTest {

    private static final Date TODAY = dateOf(2026, 11, 14);
    private static final Date YESTERDAY = dateOf(2026, 11, 13);
    private static final Date NEXT_WEEK = dateOf(2026, 11, 21);

    private static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private static Ingredient ingredient(String name) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientName(name);
        return ingredient;
    }

    private static Product product(String name, Date expiration) {
        Product product = new Product();
        product.setProductName(name);
        product.setExpirationDate(expiration);
        return product;
    }

    private static List<String> namesOf(List<Ingredient> ingredients) {
        List<String> names = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            names.add(ingredient.getIngredientName());
        }
        return names;
    }

    @Test
    @DisplayName("gives the ingredients that the pantry does not hold")
    void givesWhatThePantryLacks() {
        List<Ingredient> ingredients = Arrays.asList(
                ingredient("Onions"), ingredient("Flour"), ingredient("Salt"));
        List<Product> pantry = Collections.singletonList(product("Onion", NEXT_WEEK));

        List<Ingredient> missing = IngredientMatcher.missing(ingredients, pantry, TODAY);

        assertThat(namesOf(missing)).containsExactly("Flour", "Salt");
    }

    @Test
    @DisplayName("ignores upper case and spaces around a name")
    void ignoresCaseAndSpaces() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("  FLOUR "));
        List<Product> pantry = Collections.singletonList(product("flour", NEXT_WEEK));

        assertThat(IngredientMatcher.missing(ingredients, pantry, TODAY)).isEmpty();
    }

    @Test
    @DisplayName("a product that is already bad does not count")
    void aBadProductDoesNotCount() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("Cheese"));
        List<Product> pantry = Collections.singletonList(product("Cheese", YESTERDAY));

        assertThat(namesOf(IngredientMatcher.missing(ingredients, pantry, TODAY)))
                .containsExactly("Cheese");
    }

    @Test
    @DisplayName("a product without a date counts as usable")
    void aProductWithoutADateCounts() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("Rice"));
        List<Product> pantry = Collections.singletonList(product("Rice", null));

        assertThat(IngredientMatcher.missing(ingredients, pantry, TODAY)).isEmpty();
    }

    @Test
    @DisplayName("without a day of comparison no product is read as bad")
    void withoutTodayNothingIsBad() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("Cheese"));
        List<Product> pantry = Collections.singletonList(product("Cheese", YESTERDAY));

        assertThat(IngredientMatcher.missing(ingredients, pantry, null)).isEmpty();
    }

    @Test
    @DisplayName("the same ingredient twice reaches the list once")
    void theSameIngredientReachesTheListOnce() {
        List<Ingredient> ingredients = Arrays.asList(
                ingredient("Eggs"), ingredient("eggs"), ingredient("EGG"));

        List<Ingredient> missing = IngredientMatcher.missing(
                ingredients, Collections.emptyList(), TODAY);

        assertThat(namesOf(missing)).containsExactly("Eggs");
    }

    @Test
    @DisplayName("an empty pantry leaves every ingredient missing")
    void anEmptyPantryLeavesEverythingMissing() {
        List<Ingredient> ingredients = Arrays.asList(ingredient("Salt"), ingredient("Oil"));

        assertThat(IngredientMatcher.missing(ingredients, Collections.emptyList(), TODAY))
                .hasSize(2);
    }

    @Test
    @DisplayName("a pantry that is null leaves every ingredient missing")
    void aPantryThatIsNullLeavesEverythingMissing() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("Salt"));

        assertThat(IngredientMatcher.missing(ingredients, null, TODAY)).hasSize(1);
    }

    @Test
    @DisplayName("ingredients that are null give an empty answer")
    void ingredientsThatAreNullGiveNothing() {
        assertThat(IngredientMatcher.missing(null, Collections.emptyList(), TODAY)).isEmpty();
    }

    @Test
    @DisplayName("a row without an ingredient and a name that is empty are left out")
    void rowsWithoutAUsefulNameAreLeftOut() {
        List<Ingredient> ingredients = Arrays.asList(
                null, ingredient(null), ingredient("   "), ingredient("Salt"));

        assertThat(namesOf(IngredientMatcher.missing(ingredients, null, TODAY)))
                .containsExactly("Salt");
    }

    @Test
    @DisplayName("a product without a name and a row without a product are left out")
    void productsWithoutAUsefulNameAreLeftOut() {
        List<Ingredient> ingredients = Collections.singletonList(ingredient("Salt"));
        List<Product> pantry = Arrays.asList(
                null, product(null, NEXT_WEEK), product("  ", NEXT_WEEK));

        assertThat(IngredientMatcher.missing(ingredients, pantry, TODAY)).hasSize(1);
    }

    @Test
    @DisplayName("a short name keeps its s, a long one loses it")
    void normaliseCutsOnlyALongPlural() {
        assertThat(IngredientMatcher.normalise("Eggs")).isEqualTo("egg");
        assertThat(IngredientMatcher.normalise("As")).isEqualTo("as");
        assertThat(IngredientMatcher.normalise("Flour")).isEqualTo("flour");
        assertThat(IngredientMatcher.normalise(null)).isEmpty();
    }
}
