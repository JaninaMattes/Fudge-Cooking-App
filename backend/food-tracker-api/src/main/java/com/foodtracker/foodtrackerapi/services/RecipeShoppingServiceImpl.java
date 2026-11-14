package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.DateUtils;
import com.foodtracker.foodtrackerapi.common.utils.IngredientMatcher;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * Writes the missing ingredients of a chosen recipe onto the shopping list.
 *
 * <p>The service reads the recipe and the pantry, lets
 * {@link IngredientMatcher} decide what is missing, and gives each missing
 * ingredient to the shopping list as a product. It holds no rule of its own,
 * so the rules stay in one place each.
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class RecipeShoppingServiceImpl implements RecipeShoppingService {

    /** Value that the services give when no key is available. */
    private static final Integer NO_ID = 0;

    /**
     * Days that a planned purchase keeps. The database needs a date for each
     * product, and a product that nobody has bought holds none. The engine
     * of FR-1.4 replaces this value with an estimate per type.
     */
    private static final int PLANNED_SHELF_LIFE_DAYS = 7;

    /** Amount for an ingredient that the recipe names without one. */
    private static final String DEFAULT_QUANTITY = "1 Piece";

    private static final Logger LOG =
            LoggerFactory.getLogger(RecipeShoppingServiceImpl.class);

    private final RecipeService recipeService;

    private final InventoryService inventoryService;

    private final ShoppingListService shoppingListService;

    /**
     * Makes the service.
     *
     * @param recipeService       reads the recipe and its ingredients
     * @param inventoryService    reads the pantry of the user
     * @param shoppingListService writes a product onto the current list
     */
    public RecipeShoppingServiceImpl(RecipeService recipeService,
            InventoryService inventoryService, ShoppingListService shoppingListService) {
        this.recipeService = recipeService;
        this.inventoryService = inventoryService;
        this.shoppingListService = shoppingListService;
    }

    @Override
    public Integer addMissingProductsForRecipe(Integer userId, Integer recipeId) {
        Optional<Recipe> recipe = recipeService.getRecipeByID(recipeId);
        if (recipe.isEmpty()) {
            LOG.warn("The recipe {} is not available, so the list stays as it is", recipeId);
            return NO_ID;
        }

        List<Ingredient> missing = IngredientMatcher.missing(
                recipe.get().getIngredients(), pantryOf(userId), new Date());

        int added = 0;
        for (Ingredient ingredient : missing) {
            Integer productId = shoppingListService.createProductForCurrentShoppingList(
                    userId, asProduct(ingredient));
            if (!NO_ID.equals(productId)) {
                added++;
            }
        }

        LOG.info("{} of {} missing ingredients of the recipe {} reached the list of user {}",
                added, missing.size(), recipeId, userId);
        return added;
    }

    /**
     * Gives the products that the pantry of a user holds.
     *
     * @param userId key of the user
     * @return the products, or an empty list when the user holds no pantry
     */
    private List<Product> pantryOf(Integer userId) {
        return inventoryService.getInventoryList(userId)
                .map(Inventorylist::getInventoryList)
                .orElseGet(ArrayList::new);
    }

    /**
     * Turns an ingredient of a recipe into a product of a shopping list.
     *
     * @param ingredient the missing ingredient
     * @return the product for the list
     */
    private static Product asProduct(Ingredient ingredient) {
        Product product = new Product();
        product.setProductName(ingredient.getIngredientName());
        product.setQuantity(amountOf(ingredient));
        product.setExpirationDate(DateUtils.addDays(new Date(), PLANNED_SHELF_LIFE_DAYS));
        return product;
    }

    /**
     * Gives the amount of an ingredient, or a default when it names none.
     *
     * @param ingredient the missing ingredient
     * @return the amount for the product of the list
     */
    private static String amountOf(Ingredient ingredient) {
        String amount = ingredient.getQuantity();
        return amount == null || amount.isBlank() ? DEFAULT_QUANTITY : amount;
    }
}
