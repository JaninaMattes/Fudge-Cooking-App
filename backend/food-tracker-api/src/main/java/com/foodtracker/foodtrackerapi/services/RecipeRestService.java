package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.common.utils.WordUtils;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;
import com.foodtracker.foodtrackerapi.repositories.RecipeRepository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecipeRestService {

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    InventoryRepository productRepository;
    
    private static Logger logger = LogManager.getLogger(RecipeRestService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Load recipes based on expiring ingredient combinations
     * from external Rest API.
     * 
     * @param userId
     * @param spoonacularRecipeApiKey
     * @param recipeAmount
     * @param expDays
     * @return
     * 
     * @author Janina Mattes
     */
    public Optional<List<Recipe>> getMatchingRecipesByUserId(int userId, String spoonacularRecipeApiKey,
            int recipeAmount, int expDays) {

        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        Optional<ArrayList<Product>> expProducts = productRepository.getExpiringProductsList(userId, expDays);

        if (!expProducts.isEmpty() && expProducts.get().size() > 0) { 
            ArrayList<String> expIngredients = createIngredientsList(expProducts);

            int counter = 0;
            // Make calls
            while(counter < recipeAmount){
                // get random amounts of ingredients
                Random rand = new Random();
                int randomAmount = rand.nextInt(expIngredients.size());
                ArrayList<String> randomIngredients = pickRandomIngredients(expIngredients, randomAmount);
                
                // 1. Http request - Get matchin recipes by their recipeIds
                Optional<ArrayList<Integer>> recipeIds = getListOfRecipeIds(spoonacularRecipeApiKey,
                        randomIngredients, 3); // get only three recipes per ingredient selection

                if (!recipeIds.isEmpty()) {
                    for (int recipeId : recipeIds.get()) {
                        // 2. Http request - Get recipes with descriptions + ingredients
                        Optional<Recipe> recipe = getRecipe(spoonacularRecipeApiKey, recipeId);
                        if (!recipe.isEmpty() && recipe.get() != null) {
                            recipes.add(recipe.get());
                        } else {
                            logger.info("Recipe is empty.");
                        }
                    }
                }
                // If recipes have been requested increment
                counter++;
            }
        }else{
            logger.debug("No expiring products found!");
        }

        return Optional.of(recipes);
    }
    
    private ArrayList<String> createIngredientsList(Optional<ArrayList<Product>> expProducts){
        ArrayList<String> expIngredients = new ArrayList<String>();
        if(!expProducts.get().isEmpty() && expProducts.get().size() > 0){
            for (Product product : expProducts.get()) {
                String productName = product.getProductName();
                expIngredients.add(productName);
            }
        }
        return expIngredients;
    }
    
    private ArrayList<String> pickRandomIngredients(ArrayList<String> expIngredients, int amount) {
        // pick two random values from ingredientslist
        ArrayList<String> randomIngredients = new ArrayList<String>();
        Random rand = new Random();
        int size = expIngredients.size();
        if (size > 1) {
            for (int i = 0; i < amount; i++) {
                String randomIngredient = expIngredients.get(rand.nextInt(size));
                randomIngredients.add(randomIngredient);
            }
        } else if (size == 1) {
            randomIngredients.add(size - 1, expIngredients.get(size - 1));
        } else {
            logger.debug("No expiring products found.");
        }

        return randomIngredients;
    }

    public Optional<ArrayList<Integer>> getListOfRecipeIds(String apiKey, ArrayList<String> expProducts,
            int recipeAmount) {

        String responseJson = "";

        try {

            String ingredients = convertToIngredientsParam(expProducts);
            responseJson = WebClient.create("https://api.spoonacular.com/recipes/findByIngredients")
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("apiKey", apiKey)
                            .queryParam("ingredients", ingredients)
                            .queryParam("number", recipeAmount)
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

        } catch (Exception err) {
            logger.debug("Error occured when parsing Spoonacular API for ingredients.", err);
            err.printStackTrace();
        }
    
        // parse product from json string
        ArrayList<Integer> recipeIds = parseJsonDataToRecipeIds(responseJson, recipeAmount);
        return Optional.of(recipeIds);
    }

    public Optional<Recipe> getRecipe(String apiKey, Integer recipeId) {

        String responseJson = "";
        try {
            responseJson = WebClient.create("https://api.spoonacular.com")
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/recipes/{id}/information")
                            .queryParam("apiKey", apiKey)                            
                            .build(recipeId))
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

        } catch (Exception err) {
            logger.debug("Error occured when parsing Spoonacular API for recipes.", err);
            err.printStackTrace();
        }
        // parse product from json string
        Recipe recipe = parseJsonDataToRecipe(responseJson);
        return Optional.of(recipe);
    }

    private String convertToIngredientsParam(List<String> expProducts) {
        // join all values
        return String.join(",", expProducts);
    }

    private Recipe parseJsonDataToRecipe(String jsonResponse) {
        Recipe recipe = new Recipe();

        try {

            if (jsonResponse != null && !jsonResponse.isEmpty() && !jsonResponse.contains("error") 
                && jsonResponse.length() > 4) {

                JSONObject jsonObject = new JSONObject(jsonResponse);
                // Get values for recipe
                Integer recipeId = jsonObject.optInt("id");
                String recipeTitle = jsonObject.optString("title");
                String recipeDescription = jsonObject.optString("summary");
                String recipeLabel = getLabel(jsonObject);
                String instructions = jsonObject.optString("instructions");
                String cookingTime = jsonObject.optLong("readyInMinutes") + " min";
                String image = jsonObject.optString("image");
                float rating = randNumbConverter(3.5F, 5.0F);
                int amountOfRating = (int) randNumbConverter(10.0F, 100.0F);
                String nutritionValue = useRegexForCalories(recipeDescription); // find values in text like <b>1562 calories</b>
                
                // remove any HTML tags from text
                String cleanedInstructions = WordUtils.removeHtmlTags(instructions);

                // Get ingredients
                JSONArray jsonIngredientArray = jsonObject.getJSONArray("extendedIngredients");
                ArrayList<Ingredient> ingredients = convertJsonArrayToList(jsonIngredientArray);
                
                recipe.setRecipeId(recipeId);
                recipe.setTitle(recipeTitle);
                recipe.setRecipeLabel(recipeLabel);
                recipe.setRecipeDescription(recipeDescription);
                recipe.setCummulativeRating(rating); 
                recipe.setAmountOfRatings(amountOfRating);
                recipe.setCookingTime(cookingTime);
                recipe.setDifficulty(getDifficulty());
                recipe.setInstructions(cleanedInstructions);
                recipe.setNutritionValue(nutritionValue);
                recipe.setImageUrl(image);
                recipe.setIngredients(ingredients);

            } 
        } catch (JSONException e) {
            logger.debug("Error occured parsing JsonString to JsonObject", e);
            e.printStackTrace();
        }

        return recipe;
    }

    /**
     * Retrieve the amount of calories out of the description text provided by the API
     * using Regex algorithm.
     * @param input
     * @return
     * 
     * @author Janina Mattes
     */
    private String useRegexForCalories(String input){
        String result = "";      
        String kcal = " kcal";  
        try {
            // pattern matching
            final Pattern TAG_REGEX = Pattern.compile("<b>(.+?) calories</b>", Pattern.DOTALL);
            final Matcher matcher = TAG_REGEX.matcher(input);
            // get all matches
            matcher.find();
            String mResult = matcher.group(1);

            if(!mResult.isEmpty()){
                // pattern matching
                String extractedStr = mResult.replaceAll("[^\\d.]", "");

                if(extractedStr.contains(".")){
                    // values are dirty can contain "."
                    String[] tokens = extractedStr.split("\\."); 
                    if (tokens.length > 1) {
                        // calories are mostly > 3 values
                        for (int i = 0; i < tokens.length; i++) {
                            if (tokens[i].length() >= 3) {
                                result = tokens[i] + kcal;
                                break;
                            }
                        }
                    } else {
                        // just one token found
                        result = tokens[0] + kcal;
                    }
                }
                else{
                    result = extractedStr + kcal;
                }
            }else{
                // no token found
                result = "- kcal";
            }
            
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    private String getLabel(JSONObject jsonObject) {
        String result = "";

        try {
        
            Boolean vegetarian = jsonObject.optBoolean("vegetarian");
            Boolean vegan = jsonObject.optBoolean("vegan");
            Boolean glutenFree = jsonObject.optBoolean("glutenFree");
            Boolean dairyFree = jsonObject.optBoolean("dairyFree");
            Boolean veryHealthy = jsonObject.optBoolean("veryHealthy");
            Boolean veryPopular = jsonObject.optBoolean("veryPopular");

            if (vegetarian) {
                result = "Vegetarian";
            } else if (vegan) {
                result = "Vegan";
            } else if (glutenFree) {
                result = "Gluten Free";
            } else if (dairyFree) {
                result = "Dairy Free";
            } else if (veryHealthy) {
                result = "Very Healthy";
            } else if (veryPopular) {
                result = "Very Popular";
            } else {
                result = "New Recipe";
            }

        } catch (JSONException e) {
            logger.debug("Error occured parsing JsonString to JsonObject", e);
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Ingredient> convertJsonArrayToList(JSONArray jsonArray) {
        ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();

        try {

            for (int i = 0; i < jsonArray.length(); i++) {
                Ingredient ingredient = new Ingredient();
                JSONObject obj = jsonArray.getJSONObject(i);
                Integer ingredientId = obj.optInt("id");
                String ingredientName = obj.optString("name");
                Double amount = obj.optDouble("amount");
                String unit = obj.optString("unit");

                // capitalize each word
                String capUnit = WordUtils.capitalizeString(unit);
                String capName = WordUtils.capitalizeString(ingredientName);
                String quantity = roundNumbers(amount) + " " + capUnit;

                ingredient.setIngredientId(ingredientId);
                ingredient.setIngredientName(capName);
                ingredient.setQuantity(quantity);
                ingredients.add(ingredient);
            }
        } catch (JSONException e) {
            logger.debug("Error occured parsing JsonString to JsonObject", e);
            e.printStackTrace();
        }
        return ingredients;
    }

    private ArrayList<Integer> parseJsonDataToRecipeIds(String jsonResponse, Integer recipeAmount) {
        ArrayList<Integer> recipeIds = new ArrayList<Integer>();

        try {
            if (jsonResponse != null && !jsonResponse.isEmpty() && !jsonResponse.contains("error") 
                    && jsonResponse.length() > 4) {

                JSONArray jsonArray = new JSONArray(jsonResponse);

                for (int i = 0; i < recipeAmount; i++) {
                    Integer recipeId = jsonArray.getJSONObject(i).optInt("id");
                    recipeIds.add(recipeId);
                }
            } else {
                logger.debug("Recipe json response string is empty.", jsonResponse);
            }

        } catch (JSONException e) {
            logger.debug("Error occured parsing JsonString to JsonObject", e);
            e.printStackTrace();
        }

        return recipeIds;
    }

    private float roundNumbers(double val){
        float result = 0.0F;
        try{
            BigDecimal rounded = BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP);
            result = rounded.floatValue();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }

    private float randNumbConverter(float min, float max){
        // future get missing values from different external source
        float result = 0.0F;
        try{
            Random r = new Random();
            Double random = min + r.nextDouble() * (max - min);
            BigDecimal rating = BigDecimal.valueOf(random).setScale(1, RoundingMode.HALF_UP);
            result = rating.floatValue();
        } catch (Exception e) {
            logger.debug("Error occured creating random values.", e);
            e.printStackTrace();
        }
       return result;
    }

    private String getDifficulty(){
        // future get missing values from different external source
        String difficulty = "easy";
        try{
            ArrayList<String> difficulties = new ArrayList<String>(Arrays.asList("easy", "medium", "difficult"));
            Integer index = getRandomNumberUsingInts(0, difficulties.size() -1);
            difficulty = difficulties.get(index);
        }catch (Exception e){
            e.printStackTrace();
        }
        return difficulty;
    }
}
