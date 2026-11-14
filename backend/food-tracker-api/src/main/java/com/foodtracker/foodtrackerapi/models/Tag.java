package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * A tag, for example "Vegan". One vocabulary holds every tag of the
 * application, and {@link #kind} says what a tag is: a diet with rules, a
 * medical requirement, a value of the nutrition, a cuisine, a meal or a
 * property of a recipe.
 * </p>
 *
 * <p>
 * A tag can belong to a product, to a recipe, or to a user. When it belongs
 * to a user, two more fields carry the wish of that user:
 * {@link #weightPercent} says how important the wish is, and {@link #medical}
 * says that the wish is an allergy or an illness, which is a veto and never a
 * weight. A tag of the vocabulary leaves both at their default.
 * </p>
 *
 * <p>
 * <strong>Open for the future:</strong> the two fields of the wish stand on
 * the tag, because the row of the table {@code ET_USER_TAG} is the wish. When
 * the recommender of FR-R1 arrives, a model of its own, for example
 * {@code DietaryPreference}, is the better place for them.
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

    /**
     * What the tag is: {@code diet}, {@code medical}, {@code nutrition},
     * {@code cuisine}, {@code meal} or {@code attribute}.
     */
    private String kind;

    /**
     * How important the wish of the user is, from 0 to 100. The wishes of one
     * user add up to 100. A tag that belongs to a product or to a recipe
     * leaves the value at zero.
     */
    private int weightPercent;

    /**
     * Whether the wish is an allergy or an illness. Such a wish is a veto: it
     * carries no weight and is never traded against an expiring product.
     */
    private boolean medical;

    /**
     * Makes a tag of the vocabulary, without the wish of a user.
     *
     * @param tagId primary key of the tag
     * @param label name that the user interface shows
     */
    public Tag(Integer tagId, String label) {
        this(tagId, label, null, 0, false);
    }
}
