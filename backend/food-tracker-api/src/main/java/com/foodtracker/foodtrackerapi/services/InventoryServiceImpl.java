package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    InventoryRepository productRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Add product to inventory list by userId.
     */
    @Override
    public Integer addProductToInventoryList(Product product, int userId) {
        Integer productId = 0;
        try {
            // add new product to inventory list
            Integer inventorylistId = this.productRepository.getInventoryListId(userId);
            productId = this.productRepository.createProductInInventoryList(product, inventorylistId);
            if (productId != 0) {
                ArrayList<Tag> productTags = product.getProductTags();
                if (productTags != null && !productTags.isEmpty()) {
                    this.createDietaryTag(productTags, productId);
                } else {
                    Integer dietryTagId = this.productRepository.createDietryTag("");
                    if (dietryTagId != 0) {
                        this.productRepository.createProductTag(productId, dietryTagId);
                    }
                }
                Image img = product.getProductImage();
                if (img == null) {
                    img = new Image(0, "Product Image", "", null);
                }
                this.productRepository.createProductImage(productId, img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productId;
    }

    @Override
    public Integer updateProductOnInventoryListById(Product product, int userId) {
        Integer result = 0;
        try {
            Integer inventoryListId = this.productRepository.getInventoryListId(userId);
            if (inventoryListId != 0) {
                Image image = product.getProductImage();
                if (image != null && image.getImageId() != 0) { // if product has an image
                    this.productRepository.updateProductImage(image);
                }
                result = this.productRepository.updateProductInInventoryList(userId, product, inventoryListId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateAllProductsOnInventoryListByID(Integer userId, ArrayList<Product> products) {
        // updata all products on the inventory list
        Integer result = 0;
        try {
            Integer invlistid = this.productRepository.getInventoryListId(userId);
            for (Product product : products) {
                if (product.getProductId() != 0) {
                    result += this.productRepository.updateProductInInventoryList(userId, product, invlistid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateProductImage(Image image) {
        Integer result = 0;
        try {
            result = this.productRepository.updateProductImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Product> getProductWithShortExpDate(Integer userId) {
        return this.productRepository.getProductWithShortExpDate(userId);
    }

    /**
     * Function for polling from client when products expire.
     * 
     * @author Janina Mattes
     */
    @Override
    public Integer countExpiringProducts(Integer userId, int expDays) {
        Integer count = 0;
        try {
            // Only allow push notifications if settings true
            Settings settings = this.userRepository.getUserSettingsById(userId);
            if (settings != null && settings.getAllowPushNotifications()) {
                count = this.productRepository.countExpiringProducts(userId, expDays);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Optional<ArrayList<Product>> getAllProductWithExpDateUnder(Integer userId, Integer days) {
        return this.productRepository.getExpiringProductsList(userId, days);
    }

    @Override
    public Optional<Inventorylist> getInventoryList(int userId) {
        Optional<Inventorylist> inventoryList = this.productRepository.getInventoryList(userId);
        return inventoryList;
    }

    @Override
    public Integer deleteProductOnInventoryListById(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            Integer inventorylistId = this.productRepository.getInventoryListId(userId);

            // we do not use 0 as ID/PK in DB
            if (inventorylistId != 0) {
                // first delete dietry tag + product tag
                Optional<ArrayList<Tag>> tags = this.productRepository.getProductTags(productId);

                if (!tags.isEmpty() && tags.get().size() > 0) {
                    for (Tag tag : tags.get()) {
                        result = this.productRepository.deleteProductTagById(productId, tag.getTagId());

                        if (result != 0) { // if deletion of compount tabel was successfull
                            result = this.productRepository.deleteDietryTagById(tag.getTagId());
                        }
                    }
                    if (result != 0) { // only if tag was successfully deleted, delete image
                        // delete image
                        result = this.productRepository.deleteProductImage(productId);
                        result = this.productRepository.deleteProductOnInventoryList(userId, productId);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer removeProductOnInventoryListById(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            if (userId != 0) {
                Integer inventorylistId = this.productRepository.getInventoryListId(userId);
                // we do not use 0 as ID/PK in DB
                if (inventorylistId != 0) {
                    // set to NULL
                    result = this.productRepository.removeProductFromInventoryList(inventorylistId, productId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void createDietaryTag(ArrayList<Tag> productTags, Integer productId) {
        for (Tag tag : productTags) {
            Integer dietryTagId = this.productRepository.createDietryTag(tag.getLabel());
            if (dietryTagId != 0) {
                this.productRepository.createProductTag(productId, dietryTagId);
            }
        }

    }
}
