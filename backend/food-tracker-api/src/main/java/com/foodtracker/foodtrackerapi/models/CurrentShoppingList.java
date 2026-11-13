package com.foodtracker.foodtrackerapi.models;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * The products that a user wants to buy. The user can create a product here, or
 * move a product back from the old shopping list.
 * </p>
 *
 * @author Janina Mattes
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CurrentShoppingList extends Shoppinglist {

    /**
     * Makes a current shopping list with products.
     *
     * @param shoppingListId primary key of the list
     * @param shoppingList   products that are on the list
     */
    public CurrentShoppingList(Integer shoppingListId, List<Product> shoppingList) {
        super(shoppingListId, shoppingList);
    }

    /**
     * Makes an empty current shopping list.
     *
     * @param shoppingListId primary key of the list
     */
    public CurrentShoppingList(Integer shoppingListId) {
        super(shoppingListId);
    }
}
