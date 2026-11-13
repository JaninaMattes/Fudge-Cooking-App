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
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.services.InventoryService;

/**
 * <p>
 * The endpoints for the products that a user has at home.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/products")
public class InventoryResource {

    /**
     * Length of the period for which a product counts as "expires soon". The
     * dashboard and the notification of the client use this value.
     */
    static final int EXPIRATION_WARNING_DAYS = 3;

    private final InventoryService productService;

    /**
     * Makes the resource.
     *
     * @param productService service of the inventory list
     */
    public InventoryResource(InventoryService productService) {
        this.productService = productService;
    }

    /**
     * Puts a product on the inventory list.
     *
     * @param productMap body with the product and the field userId
     * @return the key of the new product
     */
    @PostMapping("/add")
    public ResponseEntity<Integer> addProductToInventaryList(
            @RequestBody Map<String, Object> productMap) {
        Integer userId = RequestMapper.asInteger(productMap, "userId");
        Product product = RequestMapper.toProduct(productMap);
        Integer productId = productService.addProductToInventoryList(product, userId);
        return new ResponseEntity<>(productId, HttpStatus.OK);
    }

    /**
     * Changes a product of the inventory list.
     *
     * @param productMap body with the product and the field userId
     * @return the number of rows that changed
     */
    @PutMapping("/update")
    public Integer updateProductOnIneventaryListByID(
            @RequestBody Map<String, Object> productMap) {
        Integer userId = RequestMapper.asInteger(productMap, "userId");
        Product product = RequestMapper.toProduct(productMap);
        return productService.updateProductOnInventoryListById(product, userId);
    }

    /**
     * Changes the image of a product.
     *
     * @param imageMap body with the fields imageId, imageName, imageUrl and
     *                 imageBytes
     * @return the number of rows that changed
     */
    @PutMapping("/update/image")
    public Integer updateProductImageOnIneventaryListByID(
            @RequestBody Map<String, Object> imageMap) {
        Image image = RequestMapper.toImage(imageMap);
        return productService.updateProductImage(image);
    }

    /**
     * Takes a product off the inventory list. The row stays in the database, so
     * the product is still available for the shopping lists.
     *
     * @param productMap body with the fields userId and productId
     * @return the number of rows that changed
     */
    @DeleteMapping("/delete")
    public Integer deleteProductOnInventaryList(@RequestBody Map<String, Object> productMap) {
        Integer productId = RequestMapper.asInteger(productMap, "productId");
        Integer userId = RequestMapper.asInteger(productMap, "userId");
        return productService.removeProductOnInventoryListById(userId, productId);
    }

    /**
     * Counts the products that expire soon. The client asks this question
     * regularly to show a notification.
     *
     * @param userId key of the user
     * @return the number of products
     */
    @GetMapping(path = "/count/expiring/{userId}", produces = "application/json")
    public ResponseEntity<Integer> countExpiringProducts(@PathVariable("userId") Integer userId) {
        Integer result = productService.countExpiringProducts(userId, EXPIRATION_WARNING_DAYS);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Reads the inventory list of a user.
     *
     * @param userId key of the user
     * @return the inventory list with all products
     */
    @GetMapping(path = "/all/{userId}", produces = "application/json")
    public ResponseEntity<Optional<Inventorylist>> getInventoryList(
            @PathVariable("userId") Integer userId) {
        Optional<Inventorylist> inventory = productService.getInventoryList(userId);
        return new ResponseEntity<>(inventory, HttpStatus.OK);
    }

    /**
     * Reads the products of a user that expire soon.
     *
     * @param userId key of the user
     * @return the products
     */
    @GetMapping(path = "/all/expiring/{userId}", produces = "application/json")
    public ResponseEntity<Optional<ArrayList<Product>>> getAllExpiringProducts(
            @PathVariable("userId") Integer userId) {
        Optional<ArrayList<Product>> expiringProducts = productService
                .getAllProductWithExpDateUnder(userId, EXPIRATION_WARNING_DAYS);
        return new ResponseEntity<>(expiringProducts, HttpStatus.OK);
    }

    /**
     * Changes all products of the inventory list.
     *
     * @param productMap body with the field userId and the array inventoryList
     * @return the number of rows that changed
     */
    @PutMapping("/all/update")
    public Integer updateAllProductsOnIneventaryListByID(
            @RequestBody Map<String, Object> productMap) {
        Integer userId = RequestMapper.asInteger(productMap, "userId");
        List<Map<String, Object>> inventoryList = RequestMapper.asMapList(productMap,
                "inventoryList");
        return productService.updateAllProductsOnInventoryListByID(userId,
                RequestMapper.toProducts(inventoryList));
    }
}
