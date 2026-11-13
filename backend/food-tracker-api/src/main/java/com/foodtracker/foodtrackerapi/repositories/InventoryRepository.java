package com.foodtracker.foodtrackerapi.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * <p>
 * The database operations on the inventory list of a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface InventoryRepository {

    /**
     * Puts an image on a product.
     *
     * @param productId key of the product
     * @param image     image to store
     * @return the key of the new image, or 0 if the operation failed
     */
    Integer createProductImage(Integer productId, Image image);

    /**
     * Links a dietary tag to a product.
     *
     * @param productId   key of the product
     * @param dietryTagId key of the tag
     * @return the number of rows that the operation wrote
     */
    Integer createProductTag(Integer productId, Integer dietryTagId);

    /**
     * Makes a dietary tag.
     *
     * @param label name of the tag
     * @return the key of the new tag, or 0 if the operation failed
     */
    Integer createDietryTag(String label);

    /**
     * Puts a product on the inventory list.
     *
     * @param product         product to store
     * @param inventorylistId key of the inventory list
     * @return the key of the new product, or 0 if the operation failed
     */
    Integer createProductInInventoryList(Product product, int inventorylistId);

    /**
     * Changes a product of the inventory list.
     *
     * @param userId          key of the user
     * @param product         product with the new values
     * @param inventorylistId key of the inventory list
     * @return the number of rows that changed
     */
    Integer updateProductInInventoryList(Integer userId, Product product, Integer inventorylistId);

    /**
     * Changes the image of a product.
     *
     * @param image image with the new values
     * @return the number of rows that changed
     */
    Integer updateProductImage(Image image);

    /**
     * Reads the inventory list of a user with all products.
     *
     * @param userId key of the user
     * @return the inventory list. The list of products is empty if the user has
     *         no product.
     */
    Optional<Inventorylist> getInventoryList(Integer userId);

    /**
     * Reads the products that expire in the next three days.
     *
     * @param userId key of the user
     * @return the products. The list is empty if there is no such product.
     */
    List<Product> getProductWithShortExpDate(Integer userId);

    /**
     * Reads the image of a product.
     *
     * @param productId key of the product
     * @return the image, or an empty result if the product has no image
     */
    Optional<Image> getProductImage(Integer productId);

    /**
     * Reads the dietary tags of a product.
     *
     * @param productId key of the product
     * @return the tags. The list is empty if the product has no tag.
     */
    Optional<ArrayList<Tag>> getProductTags(Integer productId);

    /**
     * Reads all dietary tags that the products of a user have.
     *
     * @param userId key of the user
     * @return the tags. The list is empty if there is no tag.
     */
    List<Tag> getAllProductTags(Integer userId);

    /**
     * Reads the key of the inventory list of a user.
     *
     * @param userId key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getInventoryListId(Integer userId);

    /**
     * Reads the products that expire in the next number of days.
     *
     * @param userId key of the user
     * @param days   length of the period in days
     * @return the products. The list is empty if there is no such product.
     */
    Optional<ArrayList<Product>> getExpiringProductsList(Integer userId, Integer days);

    /**
     * Counts the products that expire in the next number of days.
     *
     * @param userId  key of the user
     * @param expDays length of the period in days
     * @return the number of products
     */
    Integer countExpiringProducts(Integer userId, Integer expDays);

    /**
     * Reads the key of the current shopping list of a user.
     *
     * @param userId key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getCurrentShoppinglistId(Integer userId);

    /**
     * Reads the key of the old shopping list of a user.
     *
     * @param userId key of the user
     * @return the key of the list, or 0 if the user has no list
     */
    Integer getOldShoppinglistId(Integer userId);

    /**
     * Reads the keys of the dietary tags of a product.
     *
     * @param productId key of the product
     * @return the keys of the tags. The list is empty if the product has no tag.
     */
    Optional<List<Integer>> getAllTagsByProductId(Integer productId);

    /**
     * Takes a product off the inventory list but keeps the row in the database.
     *
     * @param inventoryId key of the inventory list
     * @param productId   key of the product
     * @return the number of rows that changed
     */
    Integer removeProductFromInventoryList(Integer inventoryId, Integer productId);

    /**
     * Deletes a product from the database.
     *
     * @param productId key of the product
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductOnInventoryList(Integer productId);

    /**
     * Deletes a dietary tag.
     *
     * @param tagId key of the tag
     * @return the number of rows that the operation deleted
     */
    Integer deleteDietryTagById(Integer tagId);

    /**
     * Deletes the link between a product and a dietary tag.
     *
     * @param productId key of the product
     * @param tagId     key of the tag
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductTagById(Integer productId, Integer tagId);

    /**
     * Deletes the image of a product.
     *
     * @param productId key of the product
     * @return the number of rows that the operation deleted
     */
    Integer deleteProductImage(Integer productId);
}
