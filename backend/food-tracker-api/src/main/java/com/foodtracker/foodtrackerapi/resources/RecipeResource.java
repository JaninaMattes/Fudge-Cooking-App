package com.foodtracker.foodtrackerapi.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.RecipeService;

/**
 * <p>
 * The endpoints for the recipes that the application stored for a user.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("api/recipe")
public class RecipeResource {

    private final RecipeService recipeService;

    /**
     * Makes the resource.
     *
     * @param recipeService service of the recipes
     */
    public RecipeResource(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Reads a recipe by its description.
     *
     * @param recipeMap body with the field type
     * @return the recipe, or an empty result if there is no such recipe
     */
    @GetMapping("/type")
    public Optional<Recipe> getRecipeByType(@RequestBody Map<String, Object> recipeMap) {
        return recipeService.getRecipeByType(RequestMapper.asString(recipeMap, "type"));
    }

    /**
     * Reads a recipe by its key.
     *
     * @param recipeMap body with the field recipeId
     * @return the recipe, or an empty result if there is no such recipe
     */
    @GetMapping("/id")
    public Optional<Recipe> getRecipeByID(@RequestBody Map<String, Integer> recipeMap) {
        return recipeService.getRecipeByID(recipeMap.get("recipeId"));
    }

    /**
     * Reads the recipe that has the best mark of all users.
     *
     * @return the recipe, or an empty result if there is no recipe
     */
    @GetMapping("/popular")
    public Optional<Recipe> getMostPopularRecipe() {
        return recipeService.getMostPopularRecipe();
    }

    /**
     * Adds a mark to a recipe.
     *
     * @param map body with the fields recipeId and cummulativeRating
     * @return the number of rows that changed
     */
    @PutMapping("/rate")
    public Integer rateRecipe(@RequestBody Map<String, Object> map) {
        return recipeService.rateRecipe(
                RequestMapper.asInteger(map, "recipeId"),
                RequestMapper.asInteger(map, "cummulativeRating"));
    }

    /**
     * Reads all recipes of a user.
     *
     * @param userId key of the user
     * @return the recipes
     */
    @GetMapping(path = "/all/{userId}", produces = "application/json")
    public ResponseEntity<Optional<List<Recipe>>> getAllRecipes(
            @PathVariable("userId") Integer userId) {
        Optional<List<Recipe>> recipes = recipeService.getAllRecipes(userId);
        return new ResponseEntity<>(recipes, HttpStatus.OK);
    }
}
