package com.foodtracker.foodtrackerapi.services;

/**
 * Brings a chosen recipe and the shopping list of a user together.
 *
 * <p>The user chooses a recipe. Everything that the recipe needs and the
 * pantry does not hold belongs on the shopping list, without the user
 * writing it there. That is requirement FR-3.6 of
 * {@code docs/PRODUCT.md}.
 *
 * @author Janina Mattes
 */
public interface RecipeShoppingService {

    /**
     * Writes the ingredients that the pantry of the user lacks onto the
     * current shopping list.
     *
     * @param userId   key of the user
     * @param recipeId key of the chosen recipe
     * @return how many products reached the list
     */
    Integer addMissingProductsForRecipe(Integer userId, Integer recipeId);
}
