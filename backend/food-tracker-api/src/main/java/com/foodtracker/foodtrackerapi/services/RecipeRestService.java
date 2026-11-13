package com.foodtracker.foodtrackerapi.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.foodtracker.foodtrackerapi.common.rest.JsonHttpClient;
import com.foodtracker.foodtrackerapi.common.rest.JsonResponses;
import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.common.utils.WordUtils;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;

/**
 * <p>
 * Loads recipes from the external Spoonacular API. The service takes the
 * products of the user that expire soon and searches for recipes that use these
 * products. The user can then cook the food before it is bad.
 * </p>
 *
 * <p>
 * Each search needs two requests. The first request gives the keys of the
 * recipes that fit the ingredients. The second request gives the full recipe
 * for one key.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
public class RecipeRestService {

    /** Address of the search for recipes by ingredient. */
    static final String SEARCH_URL = "https://api.spoonacular.com/recipes/findByIngredients";

    /** Pattern of the address that gives one full recipe. */
    static final String RECIPE_URL_PATTERN = "https://api.spoonacular.com/recipes/%d/information";

    private static final Logger LOG = LogManager.getLogger(RecipeRestService.class);

    /** Number of recipes that one search gives. */
    private static final int RECIPES_PER_SEARCH = 3;

    /** Lowest mark that the service gives to a new recipe. */
    private static final float MIN_RATING = 3.5F;

    /** Highest mark that the service gives to a new recipe. */
    private static final float MAX_RATING = 5.0F;

    /** Lowest number of marks that the service gives to a new recipe. */
    private static final float MIN_RATING_COUNT = 10.0F;

    /** Highest number of marks that the service gives to a new recipe. */
    private static final float MAX_RATING_COUNT = 100.0F;

    /** Text that the service shows when the calories are not available. */
    private static final String UNKNOWN_CALORIES = "- kcal";

    /** Levels of difficulty that the service gives to a new recipe. */
    private static final List<String> DIFFICULTIES = Collections.unmodifiableList(
            Arrays.asList("easy", "medium", "difficult"));

    /**
     * Finds the calories in the description text of a recipe, for example
     * {@code <b>1562 calories</b>}.
     */
    private static final Pattern CALORIES_PATTERN = Pattern.compile("<b>(.+?) calories</b>",
            Pattern.DOTALL);

    private final InventoryRepository productRepository;
    private final JsonHttpClient httpClient;

    /**
     * Makes the service.
     *
     * @param productRepository repository that gives the expiring products
     * @param httpClient        client that sends the requests to the external
     *                          API
     */
    public RecipeRestService(InventoryRepository productRepository, JsonHttpClient httpClient) {
        this.productRepository = productRepository;
        this.httpClient = httpClient;
    }

    /**
     * Loads the recipes that fit the products of a user that expire soon.
     *
     * @param userId                  key of the user
     * @param spoonacularRecipeApiKey key of the Spoonacular API
     * @param recipeAmount            number of searches to make. Each search
     *                                uses a different selection of ingredients.
     * @param expDays                 length of the period in days. A product
     *                                that expires in this period is an
     *                                ingredient of the search.
     * @return the recipes. The list is empty if no product expires in this
     *         period.
     */
    public Optional<List<Recipe>> getMatchingRecipesByUserId(int userId,
            String spoonacularRecipeApiKey, int recipeAmount, int expDays) {

        List<String> expiringIngredients = readExpiringIngredients(userId, expDays);
        if (expiringIngredients.isEmpty()) {
            LOG.debug("No product of the user with the key {} expires soon.", userId);
            return Optional.of(new ArrayList<>());
        }

        List<Recipe> recipes = new ArrayList<>();
        for (int search = 0; search < recipeAmount; search++) {
            List<String> ingredients = pickRandomIngredients(expiringIngredients);
            List<Integer> recipeIds = getListOfRecipeIds(spoonacularRecipeApiKey, ingredients,
                    RECIPES_PER_SEARCH).orElseGet(ArrayList::new);
            for (Integer recipeId : recipeIds) {
                getRecipe(spoonacularRecipeApiKey, recipeId).ifPresent(recipes::add);
            }
        }
        return Optional.of(recipes);
    }

    /**
     * Reads the keys of the recipes that use the given ingredients.
     *
     * @param apiKey       key of the Spoonacular API
     * @param ingredients  names of the ingredients
     * @param recipeAmount highest number of keys to read
     * @return the keys. The list is empty if there is no such recipe.
     */
    public Optional<ArrayList<Integer>> getListOfRecipeIds(String apiKey, List<String> ingredients,
            int recipeAmount) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("apiKey", apiKey);
        queryParams.put("ingredients", String.join(",", ingredients));
        queryParams.put("number", String.valueOf(recipeAmount));

        String responseJson = httpClient.getJson(SEARCH_URL, queryParams);
        return Optional.of(parseRecipeIds(responseJson, recipeAmount));
    }

    /**
     * Reads one full recipe.
     *
     * @param apiKey   key of the Spoonacular API
     * @param recipeId key of the recipe in the Spoonacular API
     * @return the recipe, or an empty result if the API gives no recipe
     */
    public Optional<Recipe> getRecipe(String apiKey, Integer recipeId) {
        if (recipeId == null) {
            return Optional.empty();
        }
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("apiKey", apiKey);

        String url = String.format(RECIPE_URL_PATTERN, recipeId);
        String responseJson = httpClient.getJson(url, queryParams);
        return parseRecipe(responseJson);
    }

    /**
     * Reads the keys of the recipes out of the answer of the external API.
     *
     * @param jsonResponse answer of the API. It can be {@code null}.
     * @param recipeAmount highest number of keys to read
     * @return the keys. The list is empty if the answer holds no key.
     */
    ArrayList<Integer> parseRecipeIds(String jsonResponse, Integer recipeAmount) {
        if (!JsonResponses.holdsData(jsonResponse)) {
            LOG.debug("The search for recipes gave no answer.");
            return new ArrayList<>();
        }
        return Attempt.orDefault("read the keys of the recipes", new ArrayList<>(), () -> {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            // The API can give fewer recipes than the application asked for.
            int available = Math.min(jsonArray.length(),
                    recipeAmount != null ? recipeAmount : jsonArray.length());
            ArrayList<Integer> recipeIds = new ArrayList<>(available);
            for (int i = 0; i < available; i++) {
                recipeIds.add(jsonArray.getJSONObject(i).optInt("id"));
            }
            return recipeIds;
        });
    }

    /**
     * Reads one recipe out of the answer of the external API.
     *
     * @param jsonResponse answer of the API. It can be {@code null}.
     * @return the recipe, or an empty result if the answer holds no recipe
     */
    Optional<Recipe> parseRecipe(String jsonResponse) {
        if (!JsonResponses.holdsData(jsonResponse)) {
            LOG.debug("The request for a recipe gave no answer.");
            return Optional.empty();
        }
        return Optional.ofNullable(Attempt.orDefault("read a recipe", null, () -> {
            JSONObject json = new JSONObject(jsonResponse);
            String description = json.optString("summary");
            Recipe recipe = new Recipe();
            recipe.setRecipeId(json.optInt("id"));
            recipe.setTitle(json.optString("title"));
            recipe.setRecipeLabel(readLabel(json));
            recipe.setRecipeDescription(description);
            recipe.setCookingTime(json.optLong("readyInMinutes") + " min");
            recipe.setImageUrl(json.optString("image"));
            recipe.setInstructions(WordUtils.removeHtmlTags(json.optString("instructions")));
            recipe.setNutritionValue(readCalories(description));
            recipe.setIngredients(readIngredients(json.getJSONArray("extendedIngredients")));
            // The external API gives no mark and no level of difficulty.
            recipe.setCummulativeRating(randomValue(MIN_RATING, MAX_RATING));
            recipe.setAmountOfRatings((int) randomValue(MIN_RATING_COUNT, MAX_RATING_COUNT));
            recipe.setDifficulty(randomDifficulty());
            return recipe;
        }));
    }

    /**
     * Reads the calories out of the description text of a recipe. The text holds
     * them in a form like {@code <b>1562 calories</b>}.
     *
     * @param description description text. It can be {@code null}.
     * @return the calories, for example "1562 kcal", or "- kcal" if the text
     *         holds no calories
     */
    String readCalories(String description) {
        if (description == null) {
            return UNKNOWN_CALORIES;
        }
        Matcher matcher = CALORIES_PATTERN.matcher(description);
        if (!matcher.find()) {
            return UNKNOWN_CALORIES;
        }
        String digitsOnly = matcher.group(1).replaceAll("[^\\d.]", "");
        if (digitsOnly.isEmpty()) {
            return UNKNOWN_CALORIES;
        }
        if (!digitsOnly.contains(".")) {
            return digitsOnly + " kcal";
        }
        // The value can hold more than one number, for example "1562.25".
        // A value of the calories has three digits or more.
        String[] parts = digitsOnly.split("\\.");
        for (String part : parts) {
            if (part.length() >= 3) {
                return part + " kcal";
            }
        }
        return parts[0] + " kcal";
    }

    /**
     * Reads the first property of a recipe that is true. The user interface
     * shows one property only.
     *
     * @param json answer of the API for one recipe
     * @return the name of the property, or "New Recipe" if no property is true
     */
    String readLabel(JSONObject json) {
        if (json.optBoolean("vegetarian")) {
            return "Vegetarian";
        }
        if (json.optBoolean("vegan")) {
            return "Vegan";
        }
        if (json.optBoolean("glutenFree")) {
            return "Gluten Free";
        }
        if (json.optBoolean("dairyFree")) {
            return "Dairy Free";
        }
        if (json.optBoolean("veryHealthy")) {
            return "Very Healthy";
        }
        if (json.optBoolean("veryPopular")) {
            return "Very Popular";
        }
        return "New Recipe";
    }

    /**
     * Reads the ingredients out of the answer of the external API.
     *
     * @param jsonArray array of ingredients
     * @return the ingredients. The list is empty if the array holds none.
     */
    ArrayList<Ingredient> readIngredients(JSONArray jsonArray) {
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        if (jsonArray == null) {
            return ingredients;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            final int index = i;
            Attempt.run("read an ingredient", () -> {
                JSONObject json = jsonArray.getJSONObject(index);
                String unit = WordUtils.capitalizeString(json.optString("unit"));
                String name = WordUtils.capitalizeString(json.optString("name"));
                String quantity = round(json.optDouble("amount")) + " " + unit;
                ingredients.add(new Ingredient(json.optInt("id"), name, quantity.trim()));
            });
        }
        return ingredients;
    }

    /**
     * Gives the names of the products of a user that expire soon.
     *
     * @param userId  key of the user
     * @param expDays length of the period in days
     * @return the names. The list is empty if no product expires.
     */
    private List<String> readExpiringIngredients(int userId, int expDays) {
        List<Product> expiringProducts = productRepository
                .getExpiringProductsList(userId, expDays).orElseGet(ArrayList::new);
        List<String> names = new ArrayList<>();
        for (Product product : expiringProducts) {
            if (product.getProductName() != null) {
                names.add(product.getProductName());
            }
        }
        return names;
    }

    /**
     * Picks a random selection of the ingredients. Each search then uses a
     * different combination, and the user receives different recipes.
     *
     * @param ingredients names of all ingredients. The list is not empty.
     * @return the selection. It holds at least one ingredient.
     */
    List<String> pickRandomIngredients(List<String> ingredients) {
        if (ingredients.size() == 1) {
            return new ArrayList<>(ingredients);
        }
        // At least one ingredient. A search without an ingredient gives nothing.
        int amount = ThreadLocalRandom.current().nextInt(1, ingredients.size() + 1);
        List<String> selection = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            selection.add(ingredients.get(ThreadLocalRandom.current().nextInt(ingredients.size())));
        }
        return selection;
    }

    /**
     * Gives a random level of difficulty.
     *
     * @return one of easy, medium or difficult
     */
    String randomDifficulty() {
        return DIFFICULTIES.get(ThreadLocalRandom.current().nextInt(DIFFICULTIES.size()));
    }

    /**
     * Gives a random number between two values, with one decimal place.
     *
     * @param min lowest value
     * @param max highest value
     * @return the random number
     */
    float randomValue(float min, float max) {
        double random = min + ThreadLocalRandom.current().nextDouble() * (max - min);
        return round(random);
    }

    private static float round(double value) {
        if (!Double.isFinite(value)) {
            return 0.0F;
        }
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).floatValue();
    }
}
