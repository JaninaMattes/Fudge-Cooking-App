package com.foodtracker.foodtrackerapi.resources;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.InventoryService;
import com.foodtracker.foodtrackerapi.services.RecipeService;

/**
 * <p>
 * The endpoints of the start screen of the client. The screen shows the
 * products that expire soon and a recipe suggestion.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private final RecipeService recipeService;
    private final InventoryService productService;

    /**
     * Makes the resource.
     *
     * @param recipeService  service of the recipes
     * @param productService service of the inventory list
     */
    public DashboardResource(RecipeService recipeService, InventoryService productService) {
        this.recipeService = recipeService;
        this.productService = productService;
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
     * Reads a recipe by its description.
     *
     * @param recipeMap body with the field TYPE
     * @return the recipe, or an empty result if there is no such recipe
     */
    @GetMapping("/type")
    public Optional<Recipe> getRecipeByType(@RequestBody Map<String, Object> recipeMap) {
        return recipeService.getRecipeByType(RequestMapper.asString(recipeMap, "TYPE"));
    }

    /**
     * Reads the products of a user that expire soon.
     *
     * @param userId key of the user
     * @return the products
     */
    @GetMapping(path = "/shortexp/{userId}", produces = "application/json")
    public List<Product> getProductWithShortExpDate(@PathVariable("userId") Integer userId) {
        return productService.getProductWithShortExpDate(userId);
    }

    /**
     * Counts the products of a user that expire soon.
     *
     * @param userId key of the user
     * @return the number of products
     */
    @GetMapping(path = "/numshortexp/{userId}", produces = "application/json")
    public Integer getNumOfShortExpProducts(@PathVariable("userId") Integer userId) {
        return productService.getProductWithShortExpDate(userId).size();
    }
}
