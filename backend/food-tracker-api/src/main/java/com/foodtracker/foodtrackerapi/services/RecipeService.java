package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.models.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface RecipeService {

    public Optional<Recipe> getRecipeByType(String type);

    public Optional<Recipe> getRecipeByID(Integer id);

    public Optional<Recipe> getRecipeByRating(double rating, Integer userId);

    public Optional<List<Recipe>> getAllRecipes(Integer userId);

    public Optional<ArrayList<Recipe>> getAllRecipesByRating(float rating, Integer userId);

    public List<Recipe> getAllRecipesByMatchingUserInventory(Integer userId);

    public Optional<Recipe> getMostPopularRecipe();

    public Integer deleteRecipeById(Integer id, Integer userId);

    public Integer rateRecipe(Integer id, Integer rating);

    public Integer loadRecipesFromAPI(List<Recipe> recipes, Integer userId);

    public List<Recipe> getNRandomRecipes(Integer userId, Integer n);

}
