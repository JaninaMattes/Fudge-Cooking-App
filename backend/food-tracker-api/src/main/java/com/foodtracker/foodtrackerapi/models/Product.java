package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * One food item of a user. The same class serves the inventory list and both
 * shopping lists, because the database holds all of them in the table
 * ET_PRODUCT. The foreign keys of the table decide on which list the product
 * is.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /** Primary key in the table ET_PRODUCT. */
    private Integer productId;

    /** Name of the product, for example "Milk". */
    private String productName;

    /** Date on which the product expires. */
    private Date expirationDate;

    /** Amount and unit, for example "1 Package". */
    private String quantity;

    /** Name of the manufacturer. */
    private String manufacturer;

    /** Energy value, for example "250 kcal". */
    private String nutritionValue;

    /** Image of the product. It is {@code null} if the product has no image. */
    private Image productImage;

    /** Dietary tags of the product. */
    private ArrayList<Tag> productTags = new ArrayList<>();

    /**
     * Makes a product without an image and without tags. The row mappers of the
     * database use this constructor, because one row of ET_PRODUCT holds these
     * values only.
     *
     * @param productId      primary key of the product
     * @param productName    name of the product
     * @param expirationDate date on which the product expires
     * @param quantity       amount and unit
     * @param manufacturer   name of the manufacturer
     * @param nutritionValue energy value
     */
    public Product(Integer productId, String productName, Date expirationDate, String quantity,
            String manufacturer, String nutritionValue) {
        this(productId, productName, expirationDate, quantity, manufacturer, nutritionValue,
                null, new ArrayList<>());
    }

    /**
     * Makes a product with tags but without an image.
     *
     * @param productId      primary key of the product
     * @param productName    name of the product
     * @param expirationDate date on which the product expires
     * @param quantity       amount and unit
     * @param manufacturer   name of the manufacturer
     * @param nutritionValue energy value
     * @param productTags    dietary tags of the product
     */
    public Product(Integer productId, String productName, Date expirationDate, String quantity,
            String manufacturer, String nutritionValue, ArrayList<Tag> productTags) {
        this(productId, productName, expirationDate, quantity, manufacturer, nutritionValue,
                null, productTags);
    }
}
