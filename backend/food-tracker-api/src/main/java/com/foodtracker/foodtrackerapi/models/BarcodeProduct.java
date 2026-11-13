package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * The product that the Edamam food database gives for a scanned barcode. The
 * class holds the values of the external API. It is not stored in the database.
 * </p>
 *
 * <p>
 * For more information see the
 * <a href="https://developer.edamam.com/food-database-api-docs">Edamam
 * documentation</a>.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarcodeProduct {

    /** Identifier of the food in the Edamam database. */
    private String foodId;

    /** Name of the food. */
    private String label;

    /** Energy value in kilocalories. */
    private String nutrients;

    /** Name of the manufacturer. */
    private String brand;

    /** Category of the food in the Edamam database. */
    private String category;

    /** Name of the category that the user interface shows. */
    private String categoryLabel;

    /**
     * Makes a product in which all values are empty. The barcode service uses
     * it when the external API gives no result.
     *
     * @return a product with empty values
     */
    public static BarcodeProduct empty() {
        return new BarcodeProduct("", "", "", "", "", "");
    }
}
