package com.foodtracker.foodtrackerapi.resources;

import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.RecipeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/recipe")
public class RecipeResource {

    @Autowired
    RecipeService recipeService;

    @GetMapping("/type")
    public Optional<Recipe> getRecipeByType(@RequestBody Map<String, Object> recipeMap){
        String type = (String) recipeMap.get("type");
        return recipeService.getRecipeByType(type);
    }

    @GetMapping("/id")
    public Optional<Recipe> getRecipeByID(@RequestBody Map<String, Integer> recipeMap){
        Integer recipe_id = (Integer) recipeMap.get("recipeId");
        return recipeService.getRecipeByID(recipe_id);
    }

    @GetMapping("/popular")
    public Optional<Recipe> getMostPopularRecipe(){
        return recipeService.getMostPopularRecipe();
    }

    @PutMapping("/rate")
    public Integer rateRecipe(@RequestBody Map<String, Object> map) {
        Integer recipeId = (Integer) map.get("recipeId");
        Integer rating = (Integer) map.get("cummulativeRating");
        return recipeService.rateRecipe(recipeId, rating);
    }

    @GetMapping(path="/all/{userId}", produces="application/json")
    public ResponseEntity<Optional<List<Recipe>>> getAllRecipes(@PathVariable("userId") Integer userId){
        Optional<List<Recipe>> recipes = recipeService.getAllRecipes(userId);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }  

}
