package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.models.Product;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {

    public Integer createProductInCurrentShoppingList(Integer userId, Product newProduct);

    public Integer createDietryTag(String label);

    public Integer createProductTag(Integer productId, Integer tagId);

    public Integer createProductImage(Integer productId, String imageName, String imageUrl);

    public Optional<List<Product>> getAllProductsInCurrentShoppingList(Integer userID);

    public Optional<List<Product>> getAllProductsInOldShoppingList(Integer userID);

    public Integer addProductInOldShoppingList(Integer userId, Integer productId);

    public Integer addProductInCurShoppingList(Integer userId, Integer productId);

    public Integer updateProductInCurrentShoppingList(Integer userId, Product newProduct);

    public Integer updateProductInOldShoppingList(Integer userId, Product newProduct);

    public Integer getCurrentShoppingListId(Integer user);

    public Integer getInventoryListId(Integer userId);

    public Integer getOldShoppingListId(Integer user);

    public Integer removeProductFromOldShoppingList(Integer productId, Integer oldShoppingListId);

    public Integer removeProductFromCurShoppingList(Integer productId, Integer curShoppingListId);

    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId);

    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId);
}
