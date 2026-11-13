package com.foodtracker.foodtrackerapi.repositories;

import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * <p>
 * The database operations on the recipes of a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface RecipeRepository {

    /**
     * Reads the recipes that fit the products of a user.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if there is no such recipe.
     */
    List<Recipe> getAllRecipesByRatingAndMatchWithInventory(int userId);

    /**
     * Makes an ingredient.
     *
     * @param ingredientName name of the ingredient
     * @param quantity       amount and unit
     * @return the key of the new ingredient, or 0 if the operation failed
     */
    Integer createIngredient(String ingredientName, String quantity);

    /**
     * Makes a recipe for a user.
     *
     * @param recipe recipe to store
     * @param userId key of the user
     * @return the key of the new recipe, or 0 if the operation failed
     */
    Integer createRecipe(Recipe recipe, Integer userId);

    /**
     * Links an ingredient to a recipe.
     *
     * @param recipeId     key of the recipe
     * @param ingredienId key of the ingredient
     * @return the number of rows that the operation wrote
     */
    Integer createRecipeIngredient(Integer recipeId, Integer ingredienId);

    /**
     * Adds a mark to a recipe. The method increases the sum of the marks and the
     * number of the marks.
     *
     * @param id     key of the recipe
     * @param rating mark of the user
     * @return the number of rows that changed
     */
    Integer rateRecipe(int id, int rating);

    /**
     * Reads all recipes of a user with their ingredients.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if the user has no recipe.
     */
    Optional<List<Recipe>> getAllRecipes(Integer userId);

    /**
     * Reads the recipes that fit the type and the products of a user.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if there is no such recipe.
     */
    List<Recipe> getAllRecipesByRatingAndTypeAndIngredients(int userId);

    /**
     * Reads the ingredients of a recipe.
     *
     * @param recipeId key of the recipe
     * @return the ingredients. The list is empty if the recipe has none.
     */
    List<Ingredient> getAllIngredientsPerRecipe(Integer recipeId);

    /**
     * Reads one recipe of a user that has the given average mark.
     *
     * @param rating average mark
     * @param userId key of the user
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeByRating(double rating, Integer userId);

    /**
     * Reads all recipes of a user that have the given average mark.
     *
     * @param rating average mark
     * @param userId key of the user
     * @return the recipes. The list is empty if there is no such recipe.
     */
    List<Recipe> getAllRecipesByRating(int rating, Integer userId);

    /**
     * Reads a recipe by its key.
     *
     * @param id key of the recipe
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeById(int id);

    /**
     * Reads a recipe by its description.
     *
     * @param type description of the recipe
     * @return the recipe, or an empty result if there is no such recipe
     */
    Optional<Recipe> getRecipeByType(String type);

    /**
     * Reads the recipes of a user that have the best mark.
     *
     * @param amount number of recipes to read
     * @param userId key of the user
     * @return the recipes. The list is empty if the user has no recipe.
     */
    List<Recipe> getMostPopularRecipesByAmount(Integer amount, Integer userId);

    /**
     * Reads the recipe that has the best mark of all users.
     *
     * @return the recipe, or an empty result if there is no recipe
     */
    Optional<Recipe> getMostPopularRecipe();

    /**
     * Deletes a recipe of a user.
     *
     * @param id     key of the recipe
     * @param userId key of the user
     * @return the number of rows that the operation deleted
     */
    Integer deleteRecipe(Integer id, Integer userId);

    /**
     * Deletes the ingredients of a recipe.
     *
     * @param recipeId key of the recipe
     * @return the number of rows that the operation deleted
     */
    Integer deleteIngredient(Integer recipeId);

    /**
     * Deletes the links between a recipe and its ingredients.
     *
     * @param recipeId key of the recipe
     * @return the number of rows that the operation deleted
     */
    Integer deleteRecipeIngredient(Integer recipeId);
}
