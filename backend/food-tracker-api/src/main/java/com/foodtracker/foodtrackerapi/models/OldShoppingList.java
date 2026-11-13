package com.foodtracker.foodtrackerapi.models;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * <p>
 * The products that a user bought before. The list works as a template: the
 * user can move a product from here back to the current shopping list and does
 * not have to type the data again.
 * </p>
 *
 * @author Janina Mattes
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OldShoppingList extends Shoppinglist {

    /**
     * Makes an old shopping list with products.
     *
     * @param shoppingListId primary key of the list
     * @param shoppingList   products that are on the list
     */
    public OldShoppingList(Integer shoppingListId, List<Product> shoppingList) {
        super(shoppingListId, shoppingList);
    }

    /**
     * Makes an empty old shopping list.
     *
     * @param shoppingListId primary key of the list
     */
    public OldShoppingList(Integer shoppingListId) {
        super(shoppingListId);
    }
}
