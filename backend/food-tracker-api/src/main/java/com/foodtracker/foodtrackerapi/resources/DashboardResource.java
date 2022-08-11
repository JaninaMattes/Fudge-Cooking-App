package com.foodtracker.foodtrackerapi.resources;

import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.InventoryService;
import com.foodtracker.foodtrackerapi.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    @Autowired
    RecipeService recipeService;

    @Autowired
    InventoryService productService;

    @GetMapping("/popular")
    public Optional<Recipe> getMostPopularRecipe(){
        return recipeService.getMostPopularRecipe();
    }

    @GetMapping("/type")
    public Optional<Recipe> getRecipeByType(@RequestBody Map<String, Object> recipeMap){
        String type = (String) recipeMap.get("TYPE");
        return recipeService.getRecipeByType(type);
    }

    @GetMapping(path="/shortexp/{userId}", produces = "application/json")
    public List<Product> getProductWithShortExpDate(@PathVariable("userId") Integer userId){
        return productService.getProductWithShortExpDate(userId);
    }

    @GetMapping(path="/numshortexp/{userId}", produces = "application/json")
    public Integer getNumOfShortExpProducts(@PathVariable("userId") Integer userId){
        return productService.getProductWithShortExpDate(userId).size();
    }

}
