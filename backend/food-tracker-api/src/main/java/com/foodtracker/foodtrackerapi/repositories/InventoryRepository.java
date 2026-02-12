package com.foodtracker.foodtrackerapi.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

public interface InventoryRepository {

    Integer createProductImage(Integer productId, Image image);

    Integer createProductTag(Integer productId, Integer dietryTagId);

    Integer createDietryTag(String label);

    Integer createProductInInventoryList(Product product, int inventorylistId);

    Integer updateProductInInventoryList(Integer userId, Product product, Integer inventorylistId);

    Integer updateProductImage(Image image);

    Optional<Inventorylist> getInventoryList(Integer userId);

    List<Product> getProductWithShortExpDate(Integer userId);

    Optional<Image> getProductImage(Integer productId);

    Optional<ArrayList<Tag>> getProductTags(Integer productId);

    List<Tag> getAllProductTags(Integer userId);

    Integer getInventoryListId(Integer userId);

    Optional<ArrayList<Product>> getExpiringProductsList(Integer userId, Integer days);

    Integer countExpiringProducts(Integer userId, Integer expDays);
    
    Integer getCurrentShoppinglistId(Integer userId);

    Integer getOldShoppinglistId(Integer userId);

    Optional<List<Integer>> getAllTagsByProductId(Integer productId);
    
    Integer removeProductFromInventoryList(Integer inventoryId, Integer productId);

    Integer deleteProductOnInventoryList(Integer productId, Integer inventoryId);
 
    Integer deleteDietryTagById(Integer tagId);

    Integer deleteProductTagById(Integer productId, Integer tagId);

    Integer deleteProductImage(Integer userId);

}