package com.foodtracker.foodtrackerapi.repositories;

import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Product;

/**
 * <p>
 * The database operations on the two shopping lists of a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface ShoppingListRepository {

    /**
     * Makes a product on the current shopping list.
     *
     * @param userId     key of the user
     * @param newProduct product to store
     * @return the key of the new product, or 0 if the operation failed
     */
    Integer createProductInCurrentShoppingList(Integer userId, Product newProduct);

    /**
     * Makes a dietary tag.
     *
     * @param label name of the tag
     * @return the key of the new tag, or 0 if the operation failed
     */
    Integer createDietryTag(String label);

    /**
     * Links a dietary tag to a product.
     *
     * @param productId key of the product
     * @param tagId     key of the tag
     * @return the number of rows that the operation wrote
     */
    Integer createProductTag(Integer productId, Integer tagId);

    /**
     * Puts an image on a product.
     *
     * @param productId key of the product
     * @param imageName name of the image
     * @param imageUrl  address of the image
     * @return the key of the new image, or 0 if the operation failed
     */
    Integer createProductImage(Integer productId, String imageName, String imageUrl);

    /**
     * Reads the products of the current shopping list.
     *
     * @param userID key of the user
     * @return the products. The list is empty if the list has no product.
     */
    Optional<List<Product>> getAllProductsInCurrentShoppingList(Integer userID);

    /**
     * Reads the products of the old shopping list.
     *
     * @param userID key of the user
     * @return the products. The list is empty if the list has no product.
     */
    Optional<List<Product>> getAllProductsInOldShoppingList(Integer userID);

    /**
     * Moves a product from the current list to the old list.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer addProductInOldShoppingList(Integer userId, Integer productId);

    /**
     * Moves a product from the old list to the current list.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer addProductInCurShoppingList(Integer userId, Integer productId);

    /**
     * Changes a product of the current shopping list.
     *
     * @param userId     key of the user
     * @param newProduct product with the new values
     * @return the number of rows that changed
     */
    Integer updateProductInCurrentShoppingList(Integer userId, Product newProduct);

    /**
     * Changes a product of the old shopping list.
     *
     * @param userId     key of the user
     * @param newProduct product with the new values
     * @return the number of rows that changed
     */
    Integer updateProductInOldShoppingList(Integer userId, Product newProduct);

    /**
     * Reads the key of the current shopping list of a user.
     *
     * @param user key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getCurrentShoppingListId(Integer user);

    /**
     * Reads the key of the inventory list of a user.
     *
     * @param userId key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getInventoryListId(Integer userId);

    /**
     * Reads the key of the old shopping list of a user.
     *
     * @param user key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getOldShoppingListId(Integer user);

    /**
     * Takes a product off the old shopping list but keeps the row in the
     * database.
     *
     * @param productId         key of the product
     * @param oldShoppingListId key of the old shopping list
     * @return the number of rows that changed
     */
    Integer removeProductFromOldShoppingList(Integer productId, Integer oldShoppingListId);

    /**
     * Takes a product off the current shopping list but keeps the row in the
     * database.
     *
     * @param productId         key of the product
     * @param curShoppingListId key of the current shopping list
     * @return the number of rows that changed
     */
    Integer removeProductFromCurShoppingList(Integer productId, Integer curShoppingListId);

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
