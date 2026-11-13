package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodtracker.foodtrackerapi.common.rest.JsonHttpClient;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;

/**
 * Tests for {@link RecipeRestService}.
 *
 * @author Janina Mattes
 */
class RecipeRestServiceTest {

    /** A JSON answer of the search for recipes by ingredient. */
    private static final String SEARCH_ANSWER = "[{\"id\":11},{\"id\":12},{\"id\":13}]";

    /** A JSON answer with one full recipe. */
    private static final String RECIPE_ANSWER = "{\"id\":11,\"title\":\"Pizza\","
            + "\"summary\":\"A pizza with <b>1562 calories</b> for each serving.\","
            + "\"instructions\":\"<p>Mix <i>all</i> parts.</p>\",\"readyInMinutes\":45,"
            + "\"image\":\"http://example.org/pizza.png\",\"vegetarian\":true,"
            + "\"extendedIngredients\":[{\"id\":1,\"name\":\"wheat flour\",\"amount\":200.25,"
            + "\"unit\":\"grams\"}]}";

    private StubHttpClient httpClient;
    private InventoryRepository productRepository;
    private RecipeRestService service;

    /** A client that gives the answers that the test set, in order. */
    private static final class StubHttpClient implements JsonHttpClient {

        private final Deque<String> answers = new ArrayDeque<>();
        private final List<String> urls = new ArrayList<>();
        private Map<String, String> lastParams = new LinkedHashMap<>();

        void give(String... values) {
            answers.addAll(Arrays.asList(values));
        }

        @Override
        public String getJson(String url, Map<String, String> queryParams) {
            urls.add(url);
            lastParams = queryParams;
            return answers.isEmpty() ? "" : answers.poll();
        }
    }

    private static Product product(String name) {
        return new Product(42, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        httpClient = new StubHttpClient();
        productRepository = mock(InventoryRepository.class);
        when(productRepository.getExpiringProductsList(anyInt(), anyInt()))
                .thenReturn(Optional.of(new ArrayList<>()));
        service = new RecipeRestService(productRepository, httpClient);
    }

    @Nested
    @DisplayName("getMatchingRecipesByUserId")
    class MatchingRecipes {

        @Test
        @DisplayName("gives an empty list when no product expires")
        void givesEmptyListWithoutExpiringProducts() {
            assertThat(service.getMatchingRecipesByUserId(1, "key", 2, 3))
                    .get().asList().isEmpty();
            assertThat(httpClient.urls).isEmpty();
        }

        @Test
        @DisplayName("loads a recipe for each key that the search gives")
        void loadsRecipeForEachKey() {
            when(productRepository.getExpiringProductsList(1, 3)).thenReturn(
                    Optional.of(new ArrayList<>(Collections.singletonList(product("Milk")))));
            httpClient.give(SEARCH_ANSWER, RECIPE_ANSWER, RECIPE_ANSWER, RECIPE_ANSWER);

            List<Recipe> recipes = service.getMatchingRecipesByUserId(1, "key", 1, 3)
                    .orElseThrow(AssertionError::new);

            assertThat(recipes).hasSize(3);
            assertThat(httpClient.urls).hasSize(4);
            assertThat(httpClient.urls.get(0)).isEqualTo(RecipeRestService.SEARCH_URL);
            assertThat(httpClient.urls.get(1)).contains("/recipes/11/information");
        }

        @Test
        @DisplayName("makes one search for each round that the caller asked for")
        void makesOneSearchForEachRound() {
            when(productRepository.getExpiringProductsList(1, 3)).thenReturn(
                    Optional.of(new ArrayList<>(Collections.singletonList(product("Milk")))));

            service.getMatchingRecipesByUserId(1, "key", 2, 3);

            // Each round sends one search. The search gives no answer here, so
            // no request for a full recipe follows.
            assertThat(httpClient.urls).hasSize(2);
        }

        @Test
        @DisplayName("ignores a product without a name")
        void ignoresProductWithoutName() {
            Product withoutName = new Product(42, null, null, null, null, null);
            when(productRepository.getExpiringProductsList(1, 3)).thenReturn(
                    Optional.of(new ArrayList<>(Collections.singletonList(withoutName))));

            assertThat(service.getMatchingRecipesByUserId(1, "key", 2, 3))
                    .get().asList().isEmpty();
        }

        @Test
        @DisplayName("accepts a database that gives no answer")
        void acceptsDatabaseWithoutAnswer() {
            when(productRepository.getExpiringProductsList(1, 3)).thenReturn(Optional.empty());

            assertThat(service.getMatchingRecipesByUserId(1, "key", 2, 3))
                    .get().asList().isEmpty();
        }
    }

    @Nested
    @DisplayName("getListOfRecipeIds")
    class RecipeIds {

        @Test
        @DisplayName("gives the keys of the answer")
        void givesKeysOfAnswer() {
            httpClient.give(SEARCH_ANSWER);

            assertThat(service.getListOfRecipeIds("key", Arrays.asList("Milk", "Bread"), 3))
                    .get().asList().containsExactly(11, 12, 13);
        }

        @Test
        @DisplayName("sends the ingredients as one value with commas")
        void sendsIngredientsAsOneValue() {
            service.getListOfRecipeIds("key", Arrays.asList("Milk", "Bread"), 3);

            assertThat(httpClient.lastParams)
                    .containsEntry("apiKey", "key")
                    .containsEntry("ingredients", "Milk,Bread")
                    .containsEntry("number", "3");
        }

        @Test
        @DisplayName("gives all keys when the answer holds fewer than the caller asked for")
        void givesAllKeysWhenAnswerHoldsFewer() {
            // The method read a fixed number of keys before and stopped with a
            // JSONException when the answer was shorter.
            httpClient.give("[{\"id\":11}]");

            assertThat(service.getListOfRecipeIds("key", Collections.singletonList("Milk"), 3))
                    .get().asList().containsExactly(11);
        }

        @ParameterizedTest
        @DisplayName("gives an empty list for an answer without data")
        @NullAndEmptySource
        @ValueSource(strings = { "[]", "{\"error\":\"the limit is reached\"}", "not JSON" })
        void givesEmptyListForAnswerWithoutData(String answer) {
            assertThat(service.parseRecipeIds(answer, 3)).isEmpty();
        }

        @Test
        @DisplayName("gives all keys when the caller asks for no fixed number")
        void givesAllKeysWithoutNumber() {
            assertThat(service.parseRecipeIds(SEARCH_ANSWER, null)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getRecipe")
    class OneRecipe {

        @Test
        @DisplayName("reads all values of the answer")
        void readsAllValuesOfAnswer() {
            httpClient.give(RECIPE_ANSWER);

            Recipe recipe = service.getRecipe("key", 11).orElseThrow(AssertionError::new);

            assertThat(recipe.getRecipeId()).isEqualTo(11);
            assertThat(recipe.getTitle()).isEqualTo("Pizza");
            assertThat(recipe.getRecipeLabel()).isEqualTo("Vegetarian");
            assertThat(recipe.getCookingTime()).isEqualTo("45 min");
            assertThat(recipe.getImageUrl()).isEqualTo("http://example.org/pizza.png");
            assertThat(recipe.getNutritionValue()).isEqualTo("1562 kcal");
            assertThat(recipe.getIngredients()).hasSize(1);
        }

        @Test
        @DisplayName("removes the HTML tags of the instructions")
        void removesHtmlTagsOfInstructions() {
            httpClient.give(RECIPE_ANSWER);

            Recipe recipe = service.getRecipe("key", 11).orElseThrow(AssertionError::new);

            assertThat(recipe.getInstructions()).isEqualTo("Mix all parts.");
        }

        @Test
        @DisplayName("gives a mark and a level of difficulty to a new recipe")
        void givesMarkAndDifficulty() {
            httpClient.give(RECIPE_ANSWER);

            Recipe recipe = service.getRecipe("key", 11).orElseThrow(AssertionError::new);

            assertThat(recipe.getCummulativeRating()).isBetween(3.5F, 5.0F);
            assertThat(recipe.getAmountOfRatings()).isBetween(10, 100);
            assertThat(recipe.getDifficulty()).isIn("easy", "medium", "difficult");
        }

        @Test
        @DisplayName("writes the unit and the name of an ingredient with capital letters")
        void writesIngredientWithCapitalLetters() {
            httpClient.give(RECIPE_ANSWER);

            Recipe recipe = service.getRecipe("key", 11).orElseThrow(AssertionError::new);
            Ingredient ingredient = recipe.getIngredients().get(0);

            assertThat(ingredient.getIngredientName()).isEqualTo("Wheat Flour");
            assertThat(ingredient.getQuantity()).isEqualTo("200.3 Grams");
        }

        @Test
        @DisplayName("sends the key of the API and the key of the recipe")
        void sendsApiKeyAndRecipeKey() {
            service.getRecipe("key", 11);

            assertThat(httpClient.urls.get(0)).isEqualTo(
                    String.format(RecipeRestService.RECIPE_URL_PATTERN, 11));
            assertThat(httpClient.lastParams).containsEntry("apiKey", "key");
        }

        @Test
        @DisplayName("gives an empty result for a key that is null")
        void givesEmptyResultForNullKey() {
            assertThat(service.getRecipe("key", null)).isEmpty();
            assertThat(httpClient.urls).isEmpty();
        }

        @ParameterizedTest
        @DisplayName("gives an empty result for an answer without data")
        @NullAndEmptySource
        @ValueSource(strings = { "{}", "{\"error\":\"the key is not valid\"}", "not JSON" })
        void givesEmptyResultForAnswerWithoutData(String answer) {
            assertThat(service.parseRecipe(answer)).isEmpty();
        }

        @Test
        @DisplayName("gives an empty result for an answer without ingredients")
        void givesEmptyResultWithoutIngredients() {
            assertThat(service.parseRecipe("{\"id\":11,\"title\":\"Pizza\"}")).isEmpty();
        }
    }

    @Nested
    @DisplayName("readCalories")
    class Calories {

        @ParameterizedTest
        @DisplayName("reads the calories of the description")
        @CsvSource({
                "'It has <b>1562 calories</b>.', '1562 kcal'",
                "'It has <b>1562.25 calories</b>.', '1562 kcal'",
                "'It has <b>25.5 calories</b>.', '25 kcal'",
                "'It has <b>250 calories</b> for each serving.', '250 kcal'",
                // No part of the value has three digits, so the first part
                // stands for the calories.
                "'It has <b>1.2 calories</b>.', '1 kcal'"
        })
        void readsCaloriesOfDescription(String description, String expected) {
            assertThat(service.readCalories(description)).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("gives a placeholder when the description holds no calories")
        @ValueSource(strings = {
                "A pizza without a nutrition value.",
                "It has <b>many calories</b>.",
                "<b>1562 kilojoules</b>"
        })
        void givesPlaceholderWithoutCalories(String description) {
            assertThat(service.readCalories(description)).isEqualTo("- kcal");
        }

        @Test
        @DisplayName("gives a placeholder for a description that is null")
        void givesPlaceholderForNull() {
            assertThat(service.readCalories(null)).isEqualTo("- kcal");
        }
    }

    @Nested
    @DisplayName("readLabel")
    class Label {

        @ParameterizedTest
        @DisplayName("gives the name of the property that is true")
        @CsvSource({
                "vegetarian, Vegetarian",
                "vegan, Vegan",
                "glutenFree, 'Gluten Free'",
                "dairyFree, 'Dairy Free'",
                "veryHealthy, 'Very Healthy'",
                "veryPopular, 'Very Popular'"
        })
        void givesNameOfProperty(String property, String expected) throws Exception {
            JSONObject json = new JSONObject().put(property, true);

            assertThat(service.readLabel(json)).isEqualTo(expected);
        }

        @Test
        @DisplayName("gives a default name when no property is true")
        void givesDefaultNameWithoutProperty() {
            assertThat(service.readLabel(new JSONObject())).isEqualTo("New Recipe");
        }
    }

    @Nested
    @DisplayName("The random values")
    class RandomValues {

        @Test
        @DisplayName("pickRandomIngredients gives the one ingredient of a list of one")
        void picksTheOnlyIngredient() {
            assertThat(service.pickRandomIngredients(Collections.singletonList("Milk")))
                    .containsExactly("Milk");
        }

        @RepeatedTest(20)
        @DisplayName("pickRandomIngredients always gives at least one ingredient")
        void picksAtLeastOneIngredient() {
            List<String> ingredients = Arrays.asList("Milk", "Bread", "Cheese");

            assertThat(service.pickRandomIngredients(ingredients))
                    .isNotEmpty()
                    .hasSizeLessThanOrEqualTo(ingredients.size())
                    .isSubsetOf(ingredients);
        }

        @RepeatedTest(20)
        @DisplayName("randomDifficulty can give each level of difficulty")
        void difficultyIsOneOfTheLevels() {
            // The method could never give the last level before, because the
            // highest index was too small.
            assertThat(service.randomDifficulty()).isIn("easy", "medium", "difficult");
        }

        @RepeatedTest(20)
        @DisplayName("randomValue stays between the two values")
        void randomValueStaysInRange() {
            assertThat(service.randomValue(3.5F, 5.0F)).isBetween(3.5F, 5.0F);
        }

        @Test
        @DisplayName("The levels of difficulty are all available")
        void allDifficultiesAreAvailable() {
            List<String> seen = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                String difficulty = service.randomDifficulty();
                if (!seen.contains(difficulty)) {
                    seen.add(difficulty);
                }
            }

            assertThat(seen).containsExactlyInAnyOrder("easy", "medium", "difficult");
        }
    }

    @Nested
    @DisplayName("readIngredients")
    class Ingredients {

        @Test
        @DisplayName("gives an empty list for an array that is null")
        void givesEmptyListForNull() {
            assertThat(service.readIngredients(null)).isEmpty();
        }

        @Test
        @DisplayName("gives an empty list for an array that is empty")
        void givesEmptyListForEmptyArray() {
            assertThat(service.readIngredients(new org.json.JSONArray())).isEmpty();
        }

        @Test
        @DisplayName("accepts an ingredient whose amount is not a number")
        void acceptsIngredientWithTextAmount() throws Exception {
            org.json.JSONArray array = new org.json.JSONArray().put(new JSONObject()
                    .put("id", 1).put("name", "flour").put("amount", "a lot"));

            assertThat(service.readIngredients(array).get(0).getQuantity()).isEqualTo("0.0");
        }

        @Test
        @DisplayName("accepts an ingredient without an amount")
        void acceptsIngredientWithoutAmount() throws Exception {
            org.json.JSONArray array = new org.json.JSONArray()
                    .put(new JSONObject().put("id", 1).put("name", "flour"));

            List<Ingredient> ingredients = service.readIngredients(array);

            assertThat(ingredients).hasSize(1);
            assertThat(ingredients.get(0).getQuantity()).isEqualTo("0.0");
        }
    }
}
