package com.foodtracker.foodtrackerapi.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.repositories.ShoppingListRepository;

/**
 * <p>
 * The two shopping lists of a user. The user creates a product on the current
 * list, buys it and then moves it to the old list. The old list works as a
 * template for the next purchase.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class ShoppingListServiceImpl implements ShoppingListService {

    /** Value that the repositories give when a key is not available. */
    private static final Integer NO_ID = 0;

    /** Name of the image that a new product of a shopping list gets. */
    private static final String DEFAULT_IMAGE_NAME = "Produktbild";

    private final ShoppingListRepository shoppingListRepository;

    /**
     * Makes the service.
     *
     * @param shoppingListRepository repository of the shopping lists
     */
    public ShoppingListServiceImpl(ShoppingListRepository shoppingListRepository) {
        this.shoppingListRepository = shoppingListRepository;
    }

    /**
     * Makes a product on the current shopping list. Only the current list
     * accepts a new product. The old list accepts products that the current
     * list already holds.
     */
    @Override
    public Integer createProductForCurrentShoppingList(Integer userId, Product product) {
        Integer productId = shoppingListRepository.createProductInCurrentShoppingList(userId,
                product);
        if (NO_ID.equals(productId)) {
            return NO_ID;
        }
        Integer tagId = shoppingListRepository.createDietryTag("");
        shoppingListRepository.createProductTag(productId, tagId);
        shoppingListRepository.createProductImage(productId, DEFAULT_IMAGE_NAME, "");
        return productId;
    }

    @Override
    public Integer addProductInOldShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.addProductInOldShoppingList(userId, productId);
    }

    @Override
    public Integer addProductInCurrentShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.addProductInCurShoppingList(userId, productId);
    }

    @Override
    public Optional<CurrentShoppingList> getCurrentShoppingListByID(Integer userId) {
        CurrentShoppingList shoppingList = new CurrentShoppingList(userId);
        shoppingListRepository.getAllProductsInCurrentShoppingList(userId)
                .ifPresent(shoppingList::setShoppingList);
        return Optional.of(shoppingList);
    }

    @Override
    public Optional<OldShoppingList> getOldShoppingListByID(Integer userId) {
        OldShoppingList shoppingList = new OldShoppingList(userId);
        shoppingListRepository.getAllProductsInOldShoppingList(userId)
                .ifPresent(shoppingList::setShoppingList);
        return Optional.of(shoppingList);
    }

    @Override
    public Integer updateAllItemsOldShoppingList(Integer userId, ArrayList<Product> oldShoppingList) {
        return updateAll(userId, oldShoppingList, false);
    }

    @Override
    public Integer updateAllItemsCurrentShoppingList(Integer userId,
            ArrayList<Product> currentShoppingList) {
        return updateAll(userId, currentShoppingList, true);
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
        Integer shoppingListId = shoppingListRepository.getCurrentShoppingListId(userId);
        if (NO_ID.equals(shoppingListId)) {
            return NO_ID;
        }
        return shoppingListRepository.removeProductFromCurShoppingList(productId, shoppingListId);
    }

    @Override
    public Integer removeProductInOldShoppingList(Integer userId, Integer productId) {
        Integer shoppingListId = shoppingListRepository.getOldShoppingListId(userId);
        if (NO_ID.equals(shoppingListId)) {
            return NO_ID;
        }
        return shoppingListRepository.removeProductFromOldShoppingList(productId, shoppingListId);
    }

    @Override
    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.deleteProductInCurrentShoppingList(userId, productId);
    }

    @Override
    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId) {
        return shoppingListRepository.deleteProductInOldShoppingList(userId, productId);
    }

    /**
     * Changes all products of one shopping list.
     *
     * @param userId      key of the user
     * @param products    products with the new values. The list can be
     *                    {@code null}.
     * @param currentList {@code true} for the current list, {@code false} for
     *                    the old list
     * @return the number of rows that changed
     */
    private Integer updateAll(Integer userId, List<Product> products, boolean currentList) {
        if (products == null || products.isEmpty()) {
            return NO_ID;
        }
        int changedRows = 0;
        for (Product product : products) {
            changedRows += currentList
                    ? shoppingListRepository.updateProductInCurrentShoppingList(userId, product)
                    : shoppingListRepository.updateProductInOldShoppingList(userId, product);
        }
        return changedRows;
    }
}
