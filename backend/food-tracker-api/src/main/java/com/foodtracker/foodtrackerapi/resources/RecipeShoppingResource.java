package com.foodtracker.foodtrackerapi.resources;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.services.RecipeShoppingService;

/**
 * The endpoint that fills the shopping list out of a chosen recipe.
 *
 * <p>It holds one endpoint, so that the client needs one request when the
 * user chooses a recipe. See FR-3.6 of {@code docs/PRODUCT.md}.
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/shoppinglist/current")
public class RecipeShoppingResource {

    private final RecipeShoppingService recipeShoppingService;

    /**
     * Makes the endpoint.
     *
     * @param recipeShoppingService service that compares recipe and pantry
     */
    public RecipeShoppingResource(RecipeShoppingService recipeShoppingService) {
        this.recipeShoppingService = recipeShoppingService;
    }

    /**
     * Writes the ingredients that the pantry lacks onto the current list.
     *
     * @param body body with the fields userId and recipeId
     * @return how many products reached the list
     */
    @PostMapping(path = "/add/missing", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Integer> addMissingProducts(@RequestBody Map<String, Object> body) {
        Integer added = recipeShoppingService.addMissingProductsForRecipe(
                RequestMapper.asInteger(body, "userId"),
                RequestMapper.asInteger(body, "recipeId"));
        return new ResponseEntity<>(added, HttpStatus.OK);
    }
}
