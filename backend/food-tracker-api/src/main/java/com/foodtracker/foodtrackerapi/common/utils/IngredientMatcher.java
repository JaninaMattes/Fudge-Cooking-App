package com.foodtracker.foodtrackerapi.common.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Product;

/**
 * Says which ingredients of a recipe the pantry of a user does not hold.
 *
 * <p>This is the one place that compares the name of an ingredient with the
 * name of a product. It holds no state and reads no database, so each rule
 * below can be tested on its own:
 *
 * <ul>
 *   <li>The comparison ignores upper case, lower case and spaces around the
 *       name, because one name comes from a recipe and the other from a
 *       barcode.</li>
 *   <li>It ignores one "s" at the end, so that "Onions" of a recipe finds
 *       the "Onion" of the pantry.</li>
 *   <li>A product whose date has passed does not count. A user who holds
 *       rotten cheese still needs to buy cheese.</li>
 *   <li>An ingredient that stands twice in a recipe reaches the list once.
 *       </li>
 * </ul>
 *
 * @author Janina Mattes
 */
public final class IngredientMatcher {

    /**
     * The shortest name from which an "s" at the end is read as a plural.
     * Below it the risk of cutting a real name is higher than the gain.
     */
    private static final int SHORTEST_PLURAL = 4;

    private IngredientMatcher() {
        // A class of rules, never an object.
    }

    /**
     * Gives the ingredients of a recipe that the pantry does not hold.
     *
     * @param ingredients the ingredients of the recipe, may be {@code null}
     * @param pantry      the products of the user, may be {@code null}
     * @param today       the day against which a date is read as passed
     * @return the missing ingredients, each one once, in the order of the
     *         recipe
     */
    public static List<Ingredient> missing(List<Ingredient> ingredients,
            List<Product> pantry, Date today) {
        List<Ingredient> missing = new ArrayList<>();
        if (ingredients == null) {
            return missing;
        }
        Set<String> available = namesInPantry(pantry, today);
        Set<String> taken = new HashSet<>();
        for (Ingredient ingredient : ingredients) {
            String name = nameOf(ingredient);
            if (!name.isEmpty() && !available.contains(name) && taken.add(name)) {
                missing.add(ingredient);
            }
        }
        return missing;
    }

    /**
     * Gives the names of the products that the user can still use.
     *
     * @param pantry the products of the user, may be {@code null}
     * @param today  the day against which a date is read as passed
     * @return the names, in the form that {@link #normalise(String)} gives
     */
    private static Set<String> namesInPantry(List<Product> pantry, Date today) {
        Set<String> names = new HashSet<>();
        if (pantry == null) {
            return names;
        }
        for (Product product : pantry) {
            if (product != null && !hasPassed(product.getExpirationDate(), today)) {
                String name = normalise(product.getProductName());
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * Says whether a date lies before the day of the comparison.
     *
     * @param date  the date of the product, may be {@code null}
     * @param today the day against which it is read
     * @return {@code true} when the product is bad
     */
    private static boolean hasPassed(Date date, Date today) {
        return date != null && today != null && date.before(today);
    }

    /**
     * Gives the name of an ingredient in the form of the comparison.
     *
     * @param ingredient the ingredient, may be {@code null}
     * @return the name, or an empty text when there is none
     */
    private static String nameOf(Ingredient ingredient) {
        return ingredient == null ? "" : normalise(ingredient.getIngredientName());
    }

    /**
     * Brings a name into the form of the comparison.
     *
     * @param name the name of a product or of an ingredient
     * @return the name without spaces, in lower case and without one "s" at
     *         the end
     */
    public static String normalise(String name) {
        if (name == null) {
            return "";
        }
        String plain = name.trim().toLowerCase(Locale.ROOT);
        if (plain.length() >= SHORTEST_PLURAL && plain.endsWith("s")) {
            return plain.substring(0, plain.length() - 1);
        }
        return plain;
    }
}
