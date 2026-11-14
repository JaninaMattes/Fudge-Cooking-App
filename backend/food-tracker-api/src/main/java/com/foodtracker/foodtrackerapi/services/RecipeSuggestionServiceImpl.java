package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.foodtracker.foodtrackerapi.common.diet.DietRules;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * Leaves out every recipe that breaks a dietary requirement of the user.
 *
 * <p>The service reads the requirements of the user and lets
 * {@link DietRules} decide. It holds no rule of its own, and it writes a log
 * entry for each recipe that it leaves out, so that a question of a user can
 * be answered.
 *
 * @author Janina Mattes
 */
@Service
public class RecipeSuggestionServiceImpl implements RecipeSuggestionService {

    private static final Logger LOG =
            LoggerFactory.getLogger(RecipeSuggestionServiceImpl.class);

    private final UserService userService;

    private final DietRules dietRules;

    /**
     * Makes the service.
     *
     * @param userService reads the requirements of the user
     * @param dietRules   the rules of the dietary requirements
     */
    public RecipeSuggestionServiceImpl(UserService userService, DietRules dietRules) {
        this.userService = userService;
        this.dietRules = dietRules;
    }

    @Override
    public List<Recipe> fitting(Integer userId, List<Recipe> recipes) {
        if (recipes == null) {
            return new ArrayList<>();
        }
        List<String> requirements = requirementsOf(userId);
        if (requirements.isEmpty()) {
            return recipes;
        }

        List<Recipe> fitting = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<String> conflicts = dietRules.conflicts(requirements,
                    recipe == null ? null : recipe.getIngredients());
            if (conflicts.isEmpty()) {
                fitting.add(recipe);
            } else {
                LOG.info("The recipe {} does not reach user {}, because of {}",
                        recipe.getTitle(), userId, conflicts);
            }
        }
        return fitting;
    }

    /**
     * Gives the labels of the dietary requirements of a user.
     *
     * @param userId key of the user
     * @return the labels, or an empty list when the user holds none
     */
    private List<String> requirementsOf(Integer userId) {
        List<String> labels = new ArrayList<>();
        User user = userService.getUserById(userId);
        if (user == null) {
            LOG.warn("The user {} is not available, so no requirement is known", userId);
            return labels;
        }
        List<Tag> preferences = user.getDietaryPreferences();
        if (preferences == null) {
            return labels;
        }
        for (Tag tag : preferences) {
            if (tag != null && tag.getLabel() != null && dietRules.isHard(tag.getLabel())) {
                labels.add(tag.getLabel());
            }
        }
        return labels;
    }
}
