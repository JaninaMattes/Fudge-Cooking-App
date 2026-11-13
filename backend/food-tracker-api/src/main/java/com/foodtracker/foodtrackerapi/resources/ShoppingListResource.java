package com.foodtracker.foodtrackerapi.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.services.ShoppingListService;

/**
 * <p>
 * The endpoints for the two shopping lists of a user.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("api/shoppinglist")
public class ShoppingListResource {

    private final ShoppingListService shoppingListService;

    /**
     * Makes the resource.
     *
     * @param shoppingListService service of the shopping lists
     */
    public ShoppingListResource(ShoppingListService shoppingListService) {
        this.shoppingListService = shoppingListService;
    }

    /**
     * Reads the current shopping list of a user.
     *
     * @param userId key of the user
     * @return the list with all products
     */
    @GetMapping(path = "/current/{userId}", produces = "application/json")
    public ResponseEntity<Optional<CurrentShoppingList>> getCurrentShoppingListByID(
            @PathVariable("userId") Integer userId) {
        Optional<CurrentShoppingList> shoppinglist = shoppingListService
                .getCurrentShoppingListByID(userId);
        return new ResponseEntity<>(shoppinglist, HttpStatus.OK);
    }

    /**
     * Reads the old shopping list of a user.
     *
     * @param userId key of the user
     * @return the list with all products
     */
    @GetMapping(path = "/old/{userId}", produces = "application/json")
    public ResponseEntity<Optional<OldShoppingList>> getOldShoppingListByID(
            @PathVariable("userId") Integer userId) {
        Optional<OldShoppingList> shoppinglist = shoppingListService
                .getOldShoppingListByID(userId);
        return new ResponseEntity<>(shoppinglist, HttpStatus.OK);
    }

    /**
     * Changes all products of the old shopping list.
     *
     * @param shoppingListMap body with the field userId and the array
     *                        shoppingList
     * @return the number of rows that changed
     */
    @PutMapping(path = "/old/update/all", produces = "application/json")
    public Integer updateAllItemsOldShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.updateAllItemsOldShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                readProducts(shoppingListMap));
    }

    /**
     * Changes all products of the current shopping list.
     *
     * @param shoppingListMap body with the field userId and the array
     *                        shoppingList
     * @return the number of rows that changed
     */
    @PutMapping(path = "/current/update/all", produces = "application/json")
    public Integer updateAllItemsCurrentShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.updateAllItemsCurrentShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                readProducts(shoppingListMap));
    }

    /**
     * Makes a product on the current shopping list. Only the current list
     * accepts a new product.
     *
     * @param shoppingListMap body with the product and the field userId
     * @return the key of the new product
     */
    @PostMapping(path = "/current/create", produces = "application/json")
    public Integer createNewProductInCurrentShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = RequestMapper.asInteger(shoppingListMap, "userId");
        Product product = RequestMapper.toProduct(shoppingListMap);
        return shoppingListService.createProductForCurrentShoppingList(userId, product);
    }

    /**
     * Moves a product from the old list to the current list.
     *
     * @param shoppingListMap body with the fields userId and productId
     * @return the number of rows that changed
     */
    @PostMapping(path = "/current/add", produces = "application/json")
    public Integer addProductInCurrentShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.addProductInCurrentShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                RequestMapper.asInteger(shoppingListMap, "productId"));
    }

    /**
     * Moves a product from the current list to the old list.
     *
     * @param shoppingListMap body with the fields userId and productId
     * @return the number of rows that changed
     */
    @PostMapping(path = "/old/add", produces = "application/json")
    public Integer addProductInOldShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.addProductInOldShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                RequestMapper.asInteger(shoppingListMap, "productId"));
    }

    /**
     * Changes one product of the current shopping list.
     *
     * @param shoppingListMap body with the product and the field userId
     * @return the number of rows that changed
     */
    @PutMapping("/current/update/single")
    public Integer updateProductInCurrentShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = RequestMapper.asInteger(shoppingListMap, "userId");
        Product product = RequestMapper.toProduct(shoppingListMap);
        return shoppingListService.updateProductInCurrentShoppingList(userId, product);
    }

    /**
     * Changes one product of the old shopping list.
     *
     * @param shoppingListMap body with the product and the field userId
     * @return the number of rows that changed
     */
    @PutMapping("/old/update/single")
    public Integer updateProductInOldShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = RequestMapper.asInteger(shoppingListMap, "userId");
        Product product = RequestMapper.toProduct(shoppingListMap);
        return shoppingListService.updateProductInOldShoppingList(userId, product);
    }

    /**
     * Takes a product off the current shopping list. The row stays in the
     * database.
     *
     * @param shoppingListMap body with the fields userId and productId
     * @return the number of rows that changed
     */
    @DeleteMapping("/current/delete/single")
    public Integer deleteProductInCurrentShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.removeProductInCurrentShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                RequestMapper.asInteger(shoppingListMap, "productId"));
    }

    /**
     * Takes a product off the old shopping list. The row stays in the database.
     *
     * @param shoppingListMap body with the fields userId and productId
     * @return the number of rows that changed
     */
    @DeleteMapping("/old/delete/single")
    public Integer deleteProductInOldShoppingList(
            @RequestBody Map<String, Object> shoppingListMap) {
        return shoppingListService.removeProductInOldShoppingList(
                RequestMapper.asInteger(shoppingListMap, "userId"),
                RequestMapper.asInteger(shoppingListMap, "productId"));
    }

    private ArrayList<Product> readProducts(Map<String, Object> shoppingListMap) {
        List<Map<String, Object>> products = RequestMapper.asMapList(shoppingListMap,
                "shoppingList");
        return RequestMapper.toProducts(products);
    }
}
