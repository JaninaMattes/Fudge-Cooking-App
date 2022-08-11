package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    List<Recipe> getAllRecipesByRatingAndMatchWithInventory(int userId);

    Integer createIngredient(String ingredientName, String quantity);

    Integer createRecipe(Recipe recipe, Integer userId);

    Integer createRecipeIngredient(Integer recipeId, Integer ingredienId);
    
    Integer rateRecipe(int id, int rating);

    Optional<List<Recipe>> getAllRecipes(Integer userId); 

    List<Recipe> getAllRecipesByRatingAndTypeAndIngredients(int userId);

    List<Ingredient> getAllIngredientsPerRecipe(Integer recipeId);

    Optional<Recipe> getRecipeByRating(double rating, Integer userId);

    List<Recipe> getAllRecipesByRating(int rating, Integer userId);

    Optional<Recipe> getRecipeById(int id);

    Optional<Recipe> getRecipeByType(String type);

    List<Recipe> getMostPopularRecipesByAmount(Integer amount, Integer userId); 

    Optional<Recipe> getMostPopularRecipe();

    Integer deleteRecipe(Integer id, Integer userId);

    Integer deleteIngredient(Integer recipeId);

    Integer deleteRecipeIngredient(Integer recipeId);
}
