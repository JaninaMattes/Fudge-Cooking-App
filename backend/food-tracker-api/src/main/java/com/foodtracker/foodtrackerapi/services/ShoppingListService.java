package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;

public interface ShoppingListService {

    public Integer createProductForCurrentShoppingList(Integer userId, Product product);

    public Integer addProductInCurrentShoppingList(Integer userId, Integer productId);

    public Integer addProductInOldShoppingList(Integer userId, Integer productId);

    public Optional<CurrentShoppingList> getCurrentShoppingListByID(Integer userId);

    public Optional<OldShoppingList> getOldShoppingListByID(Integer userId);

    public Integer updateAllItemsOldShoppingList(Integer userId, ArrayList<Product> oldShoppingList);

    public Integer updateAllItemsCurrentShoppingList(Integer userId, ArrayList<Product> currentShoppingList);

    public Integer updateProductInCurrentShoppingList(Integer userId, Product newProduct);

    public Integer updateProductInOldShoppingList(Integer userId, Product newProduct);

    public Integer removeProductInCurrentShoppingList(Integer userId, Integer productId);

    public Integer removeProductInOldShoppingList(Integer userId, Integer productId);

    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId);

    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId);

}
