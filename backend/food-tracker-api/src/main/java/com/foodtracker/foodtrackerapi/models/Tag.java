package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * A dietary tag, for example "Vegan". A tag can belong to a user (a food
 * preference) or to a product (a property of the food).
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    /** Primary key in the table ET_DIETRY_TAG. */
    private Integer tagId;

    /** Name of the tag that the user interface shows. */
    private String label;
}
