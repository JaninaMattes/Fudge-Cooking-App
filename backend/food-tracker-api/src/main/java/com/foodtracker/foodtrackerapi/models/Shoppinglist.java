package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * The common part of the two shopping lists of a user. The database holds the
 * same gen-spec pattern: one table ET_SHOPPINGLIST and two specializations.
 * </p>
 *
 * @author Janina Mattes
 * @see CurrentShoppingList
 * @see OldShoppingList
 */
@Data
@NoArgsConstructor
abstract class Shoppinglist {

    /** Primary key in the table ET_SHOPPINGLIST. */
    private Integer shoppingListId;

    /** Products that are on the list. */
    private List<Product> shoppingList = new ArrayList<>();

    /**
     * Makes an empty shopping list.
     *
     * @param shoppingListId primary key of the list
     */
    protected Shoppinglist(Integer shoppingListId) {
        this.shoppingListId = shoppingListId;
    }

    /**
     * Makes a shopping list with products.
     *
     * @param shoppingListId primary key of the list
     * @param shoppingList   products that are on the list
     */
    protected Shoppinglist(Integer shoppingListId, List<Product> shoppingList) {
        this.shoppingListId = shoppingListId;
        this.shoppingList = shoppingList;
    }

    /**
     * Puts a product on the list.
     *
     * @param product product to put on the list
     */
    public void addProduct(Product product) {
        this.shoppingList.add(product);
    }

    /**
     * Takes a product off the list.
     *
     * @param product product to take off the list
     * @return {@code true} if the product was on the list
     */
    public boolean removeProduct(Product product) {
        return this.shoppingList.remove(product);
    }
}
