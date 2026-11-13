package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.repositories.RecipeRepository;

/**
 * <p>
 * The recipes of a user. The service reads the recipes that the application
 * stored before. The class {@link RecipeRestService} loads new recipes from the
 * external API.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class RecipeServiceImpl implements RecipeService {

    private static final Logger LOG = LogManager.getLogger(RecipeServiceImpl.class);

    /** Number of recipes that the application shows in a list. */
    private static final int DEFAULT_RECIPE_AMOUNT = 10;

    /** Value that the repositories give when a key is not available. */
    private static final Integer NO_ID = 0;

    private final RecipeRepository recipeRepository;

    /**
     * Makes the service.
     *
     * @param recipeRepository repository of the recipes
     */
    public RecipeServiceImpl(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    public Optional<ArrayList<Recipe>> getAllRecipesByRating(float rating, Integer userId) {
        List<Recipe> recipes = getNRandomRecipes(userId, DEFAULT_RECIPE_AMOUNT);
        ArrayList<Recipe> betterThanRating = recipes.stream()
                .filter(recipe -> recipe.getCummulativeRating() != null)
                .filter(recipe -> recipe.getCummulativeRating() > rating)
                .collect(Collectors.toCollection(ArrayList::new));
        return Optional.of(betterThanRating);
    }

    @Override
    public List<Recipe> getAllRecipesByMatchingUserInventory(Integer userId) {
        return recipeRepository.getAllRecipesByRatingAndTypeAndIngredients(userId);
    }

    @Override
    public Optional<Recipe> getRecipeByType(String type) {
        return recipeRepository.getRecipeByType(type);
    }

    @Override
    public Optional<Recipe> getRecipeByID(Integer id) {
        return recipeRepository.getRecipeById(id);
    }

    @Override
    public Optional<Recipe> getRecipeByRating(double rating, Integer userId) {
        return recipeRepository.getRecipeByRating(rating, userId);
    }

    @Override
    public Optional<List<Recipe>> getAllRecipes(Integer userId) {
        return recipeRepository.getAllRecipes(userId);
    }

    @Override
    public Optional<Recipe> getMostPopularRecipe() {
        return recipeRepository.getMostPopularRecipe();
    }

    @Override
    public Integer rateRecipe(Integer id, Integer rating) {
        return recipeRepository.rateRecipe(id, rating);
    }

    /**
     * Stores the recipes that the external API gave. The method first deletes
     * the recipes of the last call, because the API gives recipes for the
     * products that expire now.
     */
    @Override
    public Integer loadRecipesFromAPI(List<Recipe> recipes, Integer userId) {
        if (recipes == null || recipes.isEmpty()) {
            return 0;
        }
        removeAllPreviousRecipes(userId);
        int storedRecipes = 0;
        for (Recipe recipe : recipes) {
            Integer recipeId = recipeRepository.createRecipe(recipe, userId);
            if (!NO_ID.equals(recipeId)) {
                createIngredients(recipe, recipeId);
                storedRecipes++;
            }
        }
        return storedRecipes;
    }

    /**
     * Gives a random selection of the recipes of a user. The dashboard shows a
     * different selection on each call.
     *
     * @param userId key of the user
     * @param amount highest number of recipes to give
     * @return the recipes. The list holds fewer recipes if the user has fewer,
     *         and it is empty if the user has none.
     */
    @Override
    public ArrayList<Recipe> getNRandomRecipes(Integer userId, Integer amount) {
        List<Recipe> allRecipes = recipeRepository.getAllRecipes(userId)
                .orElseGet(ArrayList::new);
        if (allRecipes.isEmpty()) {
            LOG.debug("No recipes found for the user with the key {}.", userId);
            return new ArrayList<>();
        }
        List<Recipe> shuffled = new ArrayList<>(allRecipes);
        Collections.shuffle(shuffled);
        int wanted = amount != null && amount > 0 ? amount : 0;
        return shuffled.stream()
                .distinct()
                .limit(wanted)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Deletes a recipe and everything that belongs to it. The links and the
     * ingredients must go first, because their tables point to the recipe.
     */
    @Override
    public Integer deleteRecipeById(Integer recipeId, Integer userId) {
        recipeRepository.deleteRecipeIngredient(recipeId);
        recipeRepository.deleteIngredient(recipeId);
        return recipeRepository.deleteRecipe(recipeId, userId);
    }

    private void createIngredients(Recipe recipe, Integer recipeId) {
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients == null) {
            return;
        }
        for (Ingredient ingredient : ingredients) {
            Integer ingredientId = recipeRepository.createIngredient(
                    ingredient.getIngredientName(), ingredient.getQuantity());
            recipeRepository.createRecipeIngredient(recipeId, ingredientId);
        }
    }

    private Integer removeAllPreviousRecipes(Integer userId) {
        List<Recipe> allRecipes = recipeRepository.getAllRecipes(userId).orElseGet(ArrayList::new);
        int deletedRows = 0;
        for (Recipe recipe : allRecipes) {
            deletedRows += deleteRecipeById(recipe.getRecipeId(), userId);
        }
        return deletedRows;
    }
}
