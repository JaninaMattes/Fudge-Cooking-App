package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * One recipe with its ingredients. The application loads a recipe from the
 * external Spoonacular API and then stores it for the user.
 * </p>
 *
 * <p>
 * The rating has two values. {@code cummulativeRating} is the sum of all marks
 * and {@code amountOfRatings} is the number of marks. The average mark is the
 * sum divided by the number. The database makes this calculation, because the
 * SQL statements sort the recipes by the average mark.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    /** Primary key in the table ET_RECIPE. */
    private Integer recipeId;

    /** Name of the recipe. */
    private String title;

    /** Short property of the recipe, for example "Vegan". */
    private String recipeLabel;

    /** Text that describes the recipe. */
    private String recipeDescription;

    /** Sum of all marks that the users gave. */
    private Float cummulativeRating;

    /** Number of marks that the users gave. */
    private Integer amountOfRatings;

    /** Time to cook the recipe, for example "45 min". */
    private String cookingTime;

    /** Level of difficulty: easy, medium or difficult. */
    private String difficulty;

    /** Steps to cook the recipe, without HTML tags. */
    private String instructions;

    /** Energy value, for example "560 kcal". */
    private String nutritionValue;

    /** Address of the image of the recipe. */
    private String imageUrl;

    /** Ingredients that the recipe needs. */
    private ArrayList<Ingredient> ingredients = new ArrayList<>();

    /**
     * Makes a recipe without the nutrition value, the image and the
     * ingredients.
     *
     * @param recipeId          primary key of the recipe
     * @param title             name of the recipe
     * @param recipeLabel       short property of the recipe
     * @param recipeDescription text that describes the recipe
     * @param cummulativeRating sum of all marks
     * @param amountOfRatings   number of marks
     * @param cookingTime       time to cook the recipe
     * @param difficulty        level of difficulty
     * @param instructions      steps to cook the recipe
     */
    public Recipe(Integer recipeId, String title, String recipeLabel, String recipeDescription,
            Float cummulativeRating, Integer amountOfRatings, String cookingTime, String difficulty,
            String instructions) {
        this(recipeId, title, recipeLabel, recipeDescription, cummulativeRating, amountOfRatings,
                cookingTime, difficulty, instructions, null, null, new ArrayList<>());
    }
}
