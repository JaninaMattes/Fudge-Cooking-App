package com.foodtracker.foodtrackerapi.models;

import java.util.List;

/**
 * <p>
 * The OldShoppingList is a POJO to represent the previously used items.
 * Simultaneously to the database it follows the gen-spec pattern and is the
 * specification of a Shoppinglist.
 * </p>
 * 
 * @author Janina Mattes
 */
public class OldShoppingList extends Shoppinglist{
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    /**
     * @param shoppingListId
     * @param shoppingList
     */
    public OldShoppingList(Integer shoppingListId, List<Product> shoppingList) {
        super(shoppingListId, shoppingList);
    }


    /**
     * @param shoppingListId
     */
    public OldShoppingList(Integer shoppingListId) {
        super(shoppingListId);
    }
    
}