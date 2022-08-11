package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.models.Product;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {

    Integer createProductInCurrentShoppingList(Integer userId, Product newProduct);

    Integer createDietryTag(String label);

    Integer createProductTag(Integer productId, Integer tagId);

    Integer createProductImage(Integer productId, String imageName, String imageUrl);

    Optional<List<Product>> getAllProductsInCurrentShoppingList(Integer userID);

    Optional<List<Product>> getAllProductsInOldShoppingList(Integer userID);

    Integer addProductInOldShoppingList(Integer userId, Integer productId);

    Integer addProductInCurShoppingList(Integer userId, Integer productId);

    Integer updateProductInCurrentShoppingList(Integer userId, Product newProduct);

    Integer updateProductInOldShoppingList(Integer userId, Product newProduct);

    Integer getCurrentShoppingListId(Integer user);

    Integer getInventoryListId(Integer userId);

    Integer getOldShoppingListId(Integer user);

    Integer removeProductFromOldShoppingList(Integer productId, Integer oldShoppingListId);

    Integer removeProductFromCurShoppingList(Integer productId, Integer curShoppingListId);

    Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId);

    Integer deleteProductInOldShoppingList(Integer userId, Integer productId);
}
