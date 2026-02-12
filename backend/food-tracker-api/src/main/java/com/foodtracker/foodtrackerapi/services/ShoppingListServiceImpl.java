package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;

import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.repositories.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShoppingListServiceImpl implements ShoppingListService {

    @Autowired
    ShoppingListRepository shoppingListRepository;

    @Override
    public Integer createProductForCurrentShoppingList(Integer userId, Product product) {
        // New products can only be created for current shoppinglist
        Integer productId = 0;
        try {
            productId = shoppingListRepository.createProductInCurrentShoppingList(userId, product);
            if (productId != 0) {
                Integer tagId = shoppingListRepository.createDietryTag("");
                shoppingListRepository.createProductTag(productId, tagId);
                shoppingListRepository.createProductImage(productId, "Produktbild", "");
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
        return productId;
    }

    @Override
    public Integer addProductInOldShoppingList(Integer userId, Integer productId) {
        // Products can only be added, NOT created!
        // Add a product from current shoppinglist to old shoppinglist
        Integer result = 0;
        try {
            result = shoppingListRepository.addProductInOldShoppingList(userId, productId);
        } catch (

        Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer addProductInCurrentShoppingList(Integer userId, Integer productId) {
        // Add a product from old shoppinglist to current shoppinglist
        Integer result = 0;
        try {
            result = shoppingListRepository.addProductInCurShoppingList(userId, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Optional<CurrentShoppingList> getCurrentShoppingListByID(Integer userId) {
        CurrentShoppingList curShoppingList = new CurrentShoppingList(userId);
        try {
            Optional<List<Product>> shoppingList = shoppingListRepository.getAllProductsInCurrentShoppingList(userId);
            if (!shoppingList.isEmpty() && shoppingList.get() != null) {
                curShoppingList.setShoppingList(shoppingList.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.of(curShoppingList);
    }

    @Override
    public Optional<OldShoppingList> getOldShoppingListByID(Integer userId) {
        OldShoppingList oldShoppingList = new OldShoppingList(userId);
        try {
            Optional<List<Product>> shoppingList = shoppingListRepository.getAllProductsInOldShoppingList(userId);
            if (!shoppingList.isEmpty() && shoppingList.get() != null) {
                oldShoppingList.setShoppingList(shoppingList.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.of(oldShoppingList);
    }

    @Override
    public Integer updateAllItemsOldShoppingList(Integer userId, ArrayList<Product> oldShoppingList) {
        Integer result = 0;
        try {
            if (oldShoppingList != null && oldShoppingList.size() > 0) {
                for (Product product : oldShoppingList) {
                    result += shoppingListRepository.updateProductInOldShoppingList(userId, product);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateAllItemsCurrentShoppingList(Integer userId, ArrayList<Product> currentShoppingList) {
        Integer result = 0;
        try {
            if (currentShoppingList != null && currentShoppingList.size() > 0) {
                for (Product product : currentShoppingList) {
                    result += shoppingListRepository.updateProductInCurrentShoppingList(userId, product);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateProductInCurrentShoppingList(Integer userId, Product product) {
        return shoppingListRepository.updateProductInCurrentShoppingList(userId, product);
    }

    @Override
    public Integer updateProductInOldShoppingList(Integer userId, Product product) {
        return shoppingListRepository.updateProductInOldShoppingList(userId, product);
    }

    @Override
    public Integer removeProductInCurrentShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            Integer curShoppingListId = shoppingListRepository.getCurrentShoppingListId(userId);
            if (curShoppingListId != 0) { // no 0 key values in DB
                result = shoppingListRepository.removeProductFromCurShoppingList(productId, curShoppingListId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer removeProductInOldShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            Integer oldShoppingListId = shoppingListRepository.getOldShoppingListId(userId);
            if (oldShoppingListId != 0) { // no 0 key values in DB
                result = shoppingListRepository.removeProductFromOldShoppingList(productId, oldShoppingListId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.deleteProductInCurrentShoppingList(userId, productId);
    }

    @Override
    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.deleteProductInOldShoppingList(userId, productId);
    }

}
