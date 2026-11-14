package com.foodtracker.foodtrackerapi.services;

import java.util.List;

import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * Keeps the dietary requirements of a user out of every list of recipes.
 *
 * <p>Requirement FR-3.1 of {@code docs/PRODUCT.md}: a user who does not eat
 * meat never sees a recipe with meat, on no screen and in no notification.
 * Every endpoint that gives recipes to a user therefore passes its answer
 * through this service.
 *
 * @author Janina Mattes
 */
public interface RecipeSuggestionService {

    /**
     * Gives the recipes that the requirements of the user allow.
     *
     * @param userId  key of the user
     * @param recipes the recipes that a service found, may be {@code null}
     * @return the recipes that the user may see
     */
    List<Recipe> fitting(Integer userId, List<Recipe> recipes);
}
