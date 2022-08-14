package com.foodtracker.foodtrackerapi.resources;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.Constants;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.RecipeRestService;
import com.foodtracker.foodtrackerapi.services.RecipeService;
<<<<<<< HEAD

=======
>>>>>>> c8c9ba2 (backend: Cleanup repositories)
/**
 * RecipeController class to retrieve matching recipes from external Spoonacular API.
 * 
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/recipe/rest")
public class RecipeRestResource {

    @Autowired
    RecipeRestService recipeRestService;

    @Autowired
    RecipeService recipeService;

    @GetMapping(path = "/poll/{userId}", produces = "application/json")
    public ResponseEntity<Integer> loadRecipesFromAPI(@PathVariable("userId") Integer userId) {
        Integer result = 0;
        Optional<List<Recipe>> recipes = recipeRestService.getMatchingRecipesByUserId(userId,
                Constants.SPOONACULAR_RECIPE_API_KEY, 20, 3);
        if (!recipes.isEmpty()) {
            result = recipeService.loadRecipesFromAPI(recipes.get(), userId);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/recipes/{userId}", produces = "application/json")
    public ResponseEntity<List<Recipe>> getRecipes(@PathVariable("userId") Integer userId) {
        List<Recipe> result = recipeService.getNRandomRecipes(userId, 10); // select 10 recipes
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/match/{userId}", produces = "application/json")
    public ResponseEntity<Optional<List<Recipe>>> getMatchinRecipesByUserId(@PathVariable("userId") Integer userId) {
        Optional<List<Recipe>> recipes = recipeRestService.getMatchingRecipesByUserId(userId,
                Constants.SPOONACULAR_RECIPE_API_KEY, 2, 3); // get min 2 different types of recipes with ingredients <=
                                                             // 3 exp days
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }
    
    @GetMapping(path = "/recipes/rating/{userId}", produces = "application/json")
    public ResponseEntity<Optional<ArrayList<Recipe>>> getBestRatedRecipes(@PathVariable("userId") Integer userId) {
        Optional<ArrayList<Recipe>> result = recipeService.getAllRecipesByRating(4.3F, userId); // select 10 recipes + filter by rating
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}