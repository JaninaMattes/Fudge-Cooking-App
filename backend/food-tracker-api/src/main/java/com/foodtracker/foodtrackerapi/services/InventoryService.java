package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;

/**
 * <p>
 * The products that a user has at home.
 * </p>
 *
 * @author Janina Mattes
 */
public interface InventoryService {

    /**
     * Changes a product of the inventory list.
     *
     * @param product product with the new values
     * @param userId  key of the user
     * @return the number of rows that changed
     */
    Integer updateProductOnInventoryListById(Product product, int userId);

    /**
     * Changes the image of a product.
     *
     * @param image image with the new values
     * @return the number of rows that changed
     */
    Integer updateProductImage(Image image);

    /**
     * Puts a product on the inventory list, with its image and its tags.
     *
     * @param product product to store
     * @param userId  key of the user
     * @return the key of the new product, or 0 if the operation failed
     */
    Integer addProductToInventoryList(Product product, int userId);

    /**
     * Changes all products of the inventory list.
     *
     * @param userId   key of the user
     * @param products products with the new values. The list can be
     *                 {@code null}.
     * @return the number of rows that changed
     */
    Integer updateAllProductsOnInventoryListByID(Integer userId, ArrayList<Product> products);

    /**
     * Reads the products that expire in the next number of days.
     *
     * @param userId key of the user
     * @param days   length of the period in days
     * @return the products. The list is empty if there is no such product.
     */
    Optional<ArrayList<Product>> getAllProductWithExpDateUnder(Integer userId, Integer days);

    /**
     * Reads the products that expire in the next three days.
     *
     * @param userId key of the user
     * @return the products. The list is empty if there is no such product.
     */
    List<Product> getProductWithShortExpDate(Integer userId);

    /**
     * Reads the inventory list of a user with all products.
     *
     * @param userId key of the user
     * @return the inventory list
     */
    Optional<Inventorylist> getInventoryList(int userId);

    /**
     * Counts the products that expire soon. The method counts them only when
     * the user gave permission for notifications.
     *
     * @param userId  key of the user
     * @param expDays length of the period in days
     * @return the number of products, or 0 if the user gave no permission
     */
    Integer countExpiringProducts(Integer userId, int expDays);

    /**
     * Deletes a product with its image and its tags.
     *
     * @param userId key of the user
     * @param id     key of the product
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductOnInventoryListById(Integer userId, Integer id);

    /**
     * Takes a product off the inventory list but keeps it in the database.
     *
     * @param userId    key of the user
     * @param productId key of the product
     * @return the number of rows that changed
     */
    Integer removeProductOnInventoryListById(Integer userId, Integer productId);
}
