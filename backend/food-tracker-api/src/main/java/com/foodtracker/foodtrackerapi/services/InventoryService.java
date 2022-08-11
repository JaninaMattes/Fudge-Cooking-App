package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface InventoryService {

    public Integer updateProductOnInventoryListById(Product product, int userId);

    public Integer updateProductImage(Image image);

    public Integer addProductToInventoryList(Product product, int userId); 

    public Integer updateAllProductsOnInventoryListByID(Integer userId, ArrayList<Product> products);
    
    public Optional<ArrayList<Product>> getAllProductWithExpDateUnder(Integer userId, Integer days);

    public List<Product> getProductWithShortExpDate(Integer userId);

    public Optional<Inventorylist> getInventoryList(int userId);

    public Integer countExpiringProducts(Integer userId, int i);

    public Integer deleteProductOnInventoryListById(Integer userId, Integer id);

    public Integer removeProductOnInventoryListById(Integer userId, Integer productId);

}
