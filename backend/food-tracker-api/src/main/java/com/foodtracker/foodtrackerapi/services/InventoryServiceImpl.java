package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;

/**
 * <p>
 * The products that a user has at home.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    /** Value that the repositories give when a key is not available. */
    private static final Integer NO_ID = 0;

    /** Name of the image that a product without an image gets. */
    private static final String DEFAULT_IMAGE_NAME = "Product Image";

    private final InventoryRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Makes the service.
     *
     * @param productRepository repository of the products
     * @param userRepository    repository of the user data. The service reads
     *                          the notification settings from it.
     */
    public InventoryServiceImpl(InventoryRepository productRepository,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Puts a product on the inventory list of a user. The method also stores
     * the tags and the image of the product, because they are in other tables.
     */
    @Override
    public Integer addProductToInventoryList(Product product, int userId) {
        Integer inventorylistId = productRepository.getInventoryListId(userId);
        Integer productId = productRepository.createProductInInventoryList(product, inventorylistId);
        if (NO_ID.equals(productId)) {
            return NO_ID;
        }
        createTags(product.getProductTags(), productId);
        Image image = product.getProductImage();
        if (image == null) {
            image = new Image(NO_ID, DEFAULT_IMAGE_NAME, "", null);
        }
        productRepository.createProductImage(productId, image);
        return productId;
    }

    @Override
    public Integer updateProductOnInventoryListById(Product product, int userId) {
        Integer inventorylistId = productRepository.getInventoryListId(userId);
        if (NO_ID.equals(inventorylistId)) {
            return NO_ID;
        }
        Image image = product.getProductImage();
        if (image != null && !NO_ID.equals(image.getImageId())) {
            productRepository.updateProductImage(image);
        }
        return productRepository.updateProductInInventoryList(userId, product, inventorylistId);
    }

    @Override
    public Integer updateAllProductsOnInventoryListByID(Integer userId,
            ArrayList<Product> products) {
        if (products == null || products.isEmpty()) {
            return NO_ID;
        }
        Integer inventorylistId = productRepository.getInventoryListId(userId);
        int changedRows = 0;
        for (Product product : products) {
            if (!NO_ID.equals(product.getProductId())) {
                changedRows += productRepository.updateProductInInventoryList(userId, product,
                        inventorylistId);
            }
        }
        return changedRows;
    }

    @Override
    public Integer updateProductImage(Image image) {
        return productRepository.updateProductImage(image);
    }

    @Override
    public List<Product> getProductWithShortExpDate(Integer userId) {
        return productRepository.getProductWithShortExpDate(userId);
    }

    /**
     * Counts the products that expire soon. The client asks this question
     * regularly to show a notification. The method counts the products only
     * when the user gave permission for notifications.
     */
    @Override
    public Integer countExpiringProducts(Integer userId, int expDays) {
        Settings settings = userRepository.getUserSettingsById(userId);
        if (settings == null || !Boolean.TRUE.equals(settings.getAllowPushNotifications())) {
            return NO_ID;
        }
        return productRepository.countExpiringProducts(userId, expDays);
    }

    @Override
    public Optional<ArrayList<Product>> getAllProductWithExpDateUnder(Integer userId,
            Integer days) {
        return productRepository.getExpiringProductsList(userId, days);
    }

    @Override
    public Optional<Inventorylist> getInventoryList(int userId) {
        return productRepository.getInventoryList(userId);
    }

    /**
     * Deletes a product and everything that belongs to it. The tags and the
     * image must go first, because their tables point to the product.
     */
    @Override
    public Integer deleteProductOnInventoryListById(Integer userId, Integer productId) {
        Integer inventorylistId = productRepository.getInventoryListId(userId);
        if (NO_ID.equals(inventorylistId)) {
            return NO_ID;
        }
        ArrayList<Tag> tags = productRepository.getProductTags(productId).orElseGet(ArrayList::new);
        for (Tag tag : tags) {
            if (!NO_ID.equals(productRepository.deleteProductTagById(productId, tag.getTagId()))) {
                productRepository.deleteDietryTagById(tag.getTagId());
            }
        }
        productRepository.deleteProductImage(productId);
        return productRepository.deleteProductOnInventoryList(productId);
    }

    @Override
    public Integer removeProductOnInventoryListById(Integer userId, Integer productId) {
        if (userId == null || NO_ID.equals(userId)) {
            return NO_ID;
        }
        Integer inventorylistId = productRepository.getInventoryListId(userId);
        if (NO_ID.equals(inventorylistId)) {
            return NO_ID;
        }
        // The row stays in the database. Only the foreign key becomes empty.
        return productRepository.removeProductFromInventoryList(inventorylistId, productId);
    }

    /**
     * Stores the dietary tags of a product. A product without a tag gets one
     * tag with an empty name, because the user interface expects at least one
     * row.
     *
     * @param productTags tags of the product. The list can be empty.
     * @param productId   key of the product
     */
    private void createTags(ArrayList<Tag> productTags, Integer productId) {
        if (productTags == null || productTags.isEmpty()) {
            Integer tagId = productRepository.createDietryTag("");
            if (!NO_ID.equals(tagId)) {
                productRepository.createProductTag(productId, tagId);
            }
            return;
        }
        for (Tag tag : productTags) {
            Integer tagId = productRepository.createDietryTag(tag.getLabel());
            if (!NO_ID.equals(tagId)) {
                productRepository.createProductTag(productId, tagId);
            }
        }
    }
}
