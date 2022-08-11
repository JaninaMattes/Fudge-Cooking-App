package com.foodtracker.foodtrackerapi.models;

import java.util.List;

/**
 * <p>
 * The CurrentShoppingList is a POJO to represent the items in a user's current shoppinglist.
 * Simultaneously to the database it follows the gen-spec pattern and is the specification of a Shoppinglist.
 * </p>
 * 
 * @author Janina Mattes
 */
public class CurrentShoppingList extends Shoppinglist{

    /**
     * @param shoppingListId
     * @param shoppingList
     */
    public CurrentShoppingList(Integer shoppingListId, List<Product> shoppingList) {
        super(shoppingListId, shoppingList);
    }

    /**
     * @param shoppingListId
     */
    public CurrentShoppingList(Integer shoppingListId) {
        super(shoppingListId);
    }

    
}
