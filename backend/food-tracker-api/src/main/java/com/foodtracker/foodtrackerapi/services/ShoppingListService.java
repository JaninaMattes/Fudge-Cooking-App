package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;

/**
 * <p>
 * The two shopping lists of a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface ShoppingListService {

    /**
     * Makes a product on the current shopping list.
     *
     * @param userId  key of the user
     * @param product product to store
     * @return the key of the new product, or 0 if the operation failed
     */
    Integer createProductForCurrentShoppingList(Integer userId, Product product);

    /**
     * Moves a product from the old list to the current list.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer addProductInCurrentShoppingList(Integer userId, Integer productId);

    /**
     * Moves a product from the current list to the old list.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer addProductInOldShoppingList(Integer userId, Integer productId);

    /**
     * Reads the current shopping list of a user.
     *
     * @param userId key of the user
     * @return the list. The list of products is empty if the user has none.
     */
    Optional<CurrentShoppingList> getCurrentShoppingListByID(Integer userId);

    /**
     * Reads the old shopping list of a user.
     *
     * @param userId key of the user
     * @return the list. The list of products is empty if the user has none.
     */
    Optional<OldShoppingList> getOldShoppingListByID(Integer userId);

    /**
     * Changes all products of the old shopping list.
     *
     * @param userId          key of the user
     * @param oldShoppingList products with the new values
     * @return the number of rows that changed
     */
    Integer updateAllItemsOldShoppingList(Integer userId, ArrayList<Product> oldShoppingList);

    /**
     * Changes all products of the current shopping list.
     *
     * @param userId              key of the user
     * @param currentShoppingList products with the new values
     * @return the number of rows that changed
     */
    Integer updateAllItemsCurrentShoppingList(Integer userId,
            ArrayList<Product> currentShoppingList);

    /**
     * Changes one product of the current shopping list.
     *
     * @param userId     key of the user
     * @param newProduct product with the new values
     * @return the number of rows that changed
     */
    Integer updateProductInCurrentShoppingList(Integer userId, Product newProduct);

    /**
     * Changes one product of the old shopping list.
     *
     * @param userId     key of the user
     * @param newProduct product with the new values
     * @return the number of rows that changed
     */
    Integer updateProductInOldShoppingList(Integer userId, Product newProduct);

    /**
     * Takes a product off the current shopping list but keeps it in the
     * database.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer removeProductInCurrentShoppingList(Integer userId, Integer productId);

    /**
     * Takes a product off the old shopping list but keeps it in the database.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer removeProductInOldShoppingList(Integer userId, Integer productId);

    /**
     * Deletes a product of the current shopping list from the database.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId);

    /**
     * Deletes a product of the old shopping list from the database.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductInOldShoppingList(Integer userId, Integer productId);
}
