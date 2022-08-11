package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import org.springframework.beans.factory.annotation.Autowired;

import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;
import com.foodtracker.foodtrackerapi.repositories.RecipeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecipeServiceImpl implements RecipeService{

    private static Logger logger = LogManager.getLogger(RecipeServiceImpl.class);


    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    InventoryRepository productRepository;

    @Override
    public Optional<ArrayList<Recipe>> getAllRecipesByRating(float rating, Integer userId) {
        ArrayList<Recipe> result = null;
        try{
            ArrayList<Recipe> recipes = getNRandomRecipes(userId, 10);
            // filter by rating
            Predicate<Recipe> byRating = recipe -> recipe.getCummulativeRating() > rating;
            List<Recipe> filtered = recipes.stream().filter(byRating).collect(Collectors.toList());
            result = new ArrayList<>(filtered);

        }catch(Exception e){
            e.printStackTrace();
        }
        return Optional.of(result);
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

    @Override
    public Integer loadRecipesFromAPI(List<Recipe> recipes, Integer userId) {
        Integer result = 0;
        try{
            // remove all old recipes in table
            removeAllPreviousRecipes(userId);
            // create new recipes + their ingredients
            for (Recipe recipe : recipes) {
                Integer recipeId = recipeRepository.createRecipe(recipe, userId);
                if(recipeId != 0){
                    for (Ingredient ingredient : recipe.getIngredients()) {
                        Integer ingredientId = recipeRepository.createIngredient(ingredient.getIngredientName(),
                                ingredient.getQuantity());
                        recipeRepository.createRecipeIngredient(recipeId, ingredientId);
                    }
                }
                result++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    public ArrayList<Recipe> getNRandomRecipes(Integer userId, Integer amount) {
        ArrayList<Recipe> randomRecipes = new ArrayList<>(amount);
        try {
            Optional<List<Recipe>> allRecipes = recipeRepository.getAllRecipes(userId);
            if (!allRecipes.isEmpty()) {
                List<Recipe> recipes = allRecipes.get();
                Collections.shuffle(recipes);
                for (Integer i = 0; i < amount; i++) {
                    randomRecipes.add(recipes.get(i));
                }
            }else{
                logger.debug("No recipes found! For user with id: " + userId);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return filterDuplicates(randomRecipes);
    }

    private ArrayList<Recipe> filterDuplicates(ArrayList<Recipe> recipes){
        ArrayList<Recipe> result = recipes;
        try{
            List<Recipe> filteredRecipes = recipes.stream().distinct().collect(Collectors.toList());
            result = new ArrayList(filteredRecipes);
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteRecipeById(Integer recipeId, Integer userId) {
        recipeRepository.deleteRecipeIngredient(recipeId);
        recipeRepository.deleteIngredient(recipeId);
        return recipeRepository.deleteRecipe(recipeId, userId);
    }

    private Integer removeAllPreviousRecipes(Integer userId) {
        Integer result = 0;
        Optional<List<Recipe>> allRecipes = recipeRepository.getAllRecipes(userId);
        if (!allRecipes.isEmpty()) {
            for (Recipe recipe : allRecipes.get()) {
                result += deleteRecipeById(recipe.getRecipeId(), userId);
            }
        }
        return result;
    }

    private Integer getRandomNumber(Integer min, Integer max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
