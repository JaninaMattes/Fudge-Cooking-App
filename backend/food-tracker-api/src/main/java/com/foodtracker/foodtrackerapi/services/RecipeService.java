package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * <p>
 * The recipes that the application stored for a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface RecipeService {

    /**
     * Reads a recipe by its description.
     *
     * @param type description of the recipe
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeByType(String type);

    /**
     * Reads a recipe by its key.
     *
     * @param id key of the recipe
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeByID(Integer id);

    /**
     * Reads one recipe of a user that has the given average mark.
     *
     * @param rating average mark
     * @param userId key of the user
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeByRating(double rating, Integer userId);

    /**
     * Reads all recipes of a user.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if the user has no recipe.
     */
    Optional<List<Recipe>> getAllRecipes(Integer userId);

    /**
     * Reads the recipes of a user that have a better mark than the given mark.
     *
     * @param rating mark to compare with
     * @param userId key of the user
     * @return the recipes. The list is empty if there is no such recipe.
     */
    Optional<ArrayList<Recipe>> getAllRecipesByRating(float rating, Integer userId);

    /**
     * Reads the recipes that fit the products of a user.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if there is no such recipe.
     */
    List<Recipe> getAllRecipesByMatchingUserInventory(Integer userId);

    /**
     * Reads the recipe that has the best mark of all users.
     *
     * @return the recipe, or an empty result if there is no recipe
     */
    Optional<Recipe> getMostPopularRecipe();

    /**
     * Deletes a recipe of a user with its ingredients.
     *
     * @param id     key of the recipe
     * @param userId key of the user
     * @return the number of rows that the operation deleted
     */
    Integer deleteRecipeById(Integer id, Integer userId);

    /**
     * Adds a mark to a recipe.
     *
     * @param id     key of the recipe
     * @param rating mark of the user
     * @return the number of rows that changed
     */
    Integer rateRecipe(Integer id, Integer rating);

    /**
     * Stores the recipes that the external API gave. The method first deletes
     * the recipes of the last call.
     *
     * @param recipes recipes to store. The list can be {@code null}.
     * @param userId  key of the user
     * @return the number of recipes that the method stored
     */
    Integer loadRecipesFromAPI(List<Recipe> recipes, Integer userId);

    /**
     * Gives a random selection of the recipes of a user.
     *
     * @param userId key of the user
     * @param n      highest number of recipes to give
     * @return the recipes. The list holds fewer recipes if the user has fewer.
     */
    List<Recipe> getNRandomRecipes(Integer userId, Integer n);
}
