package com.foodtracker.foodtrackerapi.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.RecipeRestService;
import com.foodtracker.foodtrackerapi.services.RecipeService;
import com.foodtracker.foodtrackerapi.services.RecipeSuggestionService;

/**
 * <p>
 * The endpoints that load recipes from the external Spoonacular API and give
 * the stored recipes back to the client.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/recipe/rest")
public class RecipeRestResource {

    /** Number of searches that the application makes when it loads recipes. */
    static final int POLL_SEARCH_COUNT = 20;

    /** Number of searches that the application makes for a quick suggestion. */
    static final int MATCH_SEARCH_COUNT = 2;

    /**
     * Length of the period in days. A product that expires in this period is an
     * ingredient of the search.
     */
    static final int EXPIRATION_WARNING_DAYS = 3;

    /** Number of recipes that the client shows in a list. */
    static final int RECIPE_LIST_SIZE = 10;

    /** Lowest mark of a recipe in the list of the best recipes. */
    static final float BEST_RATING_LIMIT = 4.3F;

    private final RecipeRestService recipeRestService;
    private final RecipeService recipeService;
    private final ApiProperties apiProperties;

    private final RecipeSuggestionService suggestionService;

    /**
     * Makes the resource.
     *
     * @param recipeRestService service that reads the external API
     * @param recipeService     service of the stored recipes
     * @param apiProperties     configuration that holds the key of the external
     *                          API
     * @param suggestionService keeps the dietary requirements of the user
     */
    public RecipeRestResource(RecipeRestService recipeRestService, RecipeService recipeService,
            ApiProperties apiProperties, RecipeSuggestionService suggestionService) {
        this.recipeRestService = recipeRestService;
        this.recipeService = recipeService;
        this.apiProperties = apiProperties;
        this.suggestionService = suggestionService;
    }

    /**
     * Loads new recipes from the external API and stores them. The client calls
     * this endpoint in the background.
     *
     * @param userId key of the user
     * @return the number of recipes that the application stored
     */
    @GetMapping(path = "/poll/{userId}", produces = "application/json")
    public ResponseEntity<Integer> loadRecipesFromAPI(@PathVariable("userId") Integer userId) {
        List<Recipe> recipes = recipeRestService.getMatchingRecipesByUserId(userId,
                apiProperties.getSpoonacularRecipeKey(), POLL_SEARCH_COUNT,
                EXPIRATION_WARNING_DAYS).orElseGet(ArrayList::new);
        Integer result = recipeService.loadRecipesFromAPI(recipes, userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Reads a random selection of the stored recipes of a user.
     *
     * @param userId key of the user
     * @return the recipes
     */
    @GetMapping(path = "/recipes/{userId}", produces = "application/json")
    public ResponseEntity<List<Recipe>> getRecipes(@PathVariable("userId") Integer userId) {
        List<Recipe> result = suggestionService.fitting(userId,
                recipeService.getNRandomRecipes(userId, RECIPE_LIST_SIZE));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Reads recipes from the external API that fit the products of a user that
     * expire soon. The method does not store the recipes.
     *
     * @param userId key of the user
     * @return the recipes
     */
    @GetMapping(path = "/match/{userId}", produces = "application/json")
    public ResponseEntity<Optional<List<Recipe>>> getMatchinRecipesByUserId(
            @PathVariable("userId") Integer userId) {
        Optional<List<Recipe>> recipes = recipeRestService
                .getMatchingRecipesByUserId(userId,
                        apiProperties.getSpoonacularRecipeKey(), MATCH_SEARCH_COUNT,
                        EXPIRATION_WARNING_DAYS)
                .map(found -> suggestionService.fitting(userId, found));
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }

    /**
     * Reads the stored recipes of a user that have a good mark.
     *
     * @param userId key of the user
     * @return the recipes
     */
    @GetMapping(path = "/recipes/rating/{userId}", produces = "application/json")
    public ResponseEntity<Optional<ArrayList<Recipe>>> getBestRatedRecipes(
            @PathVariable("userId") Integer userId) {
        Optional<ArrayList<Recipe>> result = recipeService
                .getAllRecipesByRating(BEST_RATING_LIMIT, userId)
                .map(found -> new ArrayList<Recipe>(
                        suggestionService.fitting(userId, found)));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
