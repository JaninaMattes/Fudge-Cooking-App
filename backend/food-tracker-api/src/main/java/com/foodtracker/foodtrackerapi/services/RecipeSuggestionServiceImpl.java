package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.foodtracker.foodtrackerapi.common.diet.DietRules;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * Leaves out every recipe that a user cannot or does not want to eat.
 *
 * <p>The service reads the wishes of the user and lets {@link DietRules}
 * decide. It holds no rule of its own, and it writes a log entry for each
 * recipe that it leaves out, so that a question of a user can be answered.
 *
 * <p>It asks the rules twice, because the two kinds of wish are not equal:
 *
 * <ul>
 *   <li>A wish marked as <strong>medical</strong> — an allergy or an illness
 *       — is a veto. Every such wish counts, also one that the file of the
 *       rules does not name: a user who writes "Celery" never sees celery
 *       again.</li>
 *   <li>A <strong>diet</strong> such as Vegan is a rule of the file. A wish
 *       that the file does not name is a taste and hides nothing.</li>
 * </ul>
 *
 * <p><strong>Where this stands today.</strong> A wish carries a weight since
 * 14 November 2026. This service still treats every diet that the rules know
 * as a veto, so nothing of the behaviour changed with the weights. The
 * weighted choice — two vegan recipes and one that uses the steak which
 * expires in two days — belongs to FR-R1 and is open. A medical wish keeps
 * its absolute priority then: it is never traded against an expiring product.
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
     * @param userService reads the wishes of the user
     * @param dietRules   the rules of the requirements
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
        List<Tag> wishes = wishesOf(userId);
        List<String> medical = labelsOf(wishes, true);
        List<String> diets = labelsOf(wishes, false);
        if (medical.isEmpty() && diets.isEmpty()) {
            return recipes;
        }

        List<Recipe> fitting = new ArrayList<>();
        for (Recipe recipe : recipes) {
            List<Ingredient> ingredients = recipe == null ? null : recipe.getIngredients();
            List<String> conflicts = new ArrayList<>(
                    dietRules.medicalConflicts(medical, ingredients));
            conflicts.addAll(dietRules.conflicts(diets, ingredients));
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
     * Gives the wishes of a user.
     *
     * @param userId key of the user
     * @return the tags of the user, or an empty list when there are none
     */
    private List<Tag> wishesOf(Integer userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            LOG.warn("The user {} is not available, so no requirement is known", userId);
            return List.of();
        }
        List<Tag> wishes = user.getDietaryPreferences();
        return wishes == null ? List.of() : wishes;
    }

    /**
     * Gives the labels of one kind of wish.
     *
     * @param wishes  the tags of the user
     * @param medical {@code true} for the allergies and the illnesses,
     *                {@code false} for the diets
     * @return the labels that the application must honour
     */
    private List<String> labelsOf(List<Tag> wishes, boolean medical) {
        List<String> labels = new ArrayList<>();
        for (Tag tag : wishes) {
            if (tag != null && tag.getLabel() != null && tag.isMedical() == medical) {
                // A medical wish counts even when the file knows no rule for
                // it: the label itself then names the forbidden ingredient.
                if (medical || dietRules.isHard(tag.getLabel())) {
                    labels.add(tag.getLabel());
                }
            }
        }
        return labels;
    }
}
