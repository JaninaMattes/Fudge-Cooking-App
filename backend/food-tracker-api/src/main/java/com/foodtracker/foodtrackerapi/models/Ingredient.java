package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * One ingredient of a recipe, for example "200.0 G" of "Flour". The external
 * recipe API gives the amount and the unit as separate values. The application
 * keeps them together in one text, because the user interface shows them
 * together.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    /** Primary key in the table ET_INGREDIENT. */
    private Integer ingredientId;

    /** Name of the ingredient, for example "Flour". */
    private String ingredientName;

    /** Amount and unit, for example "200.0 G". */
    private String quantity;
}
