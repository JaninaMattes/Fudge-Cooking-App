package com.foodtracker.foodtrackerapi.resources;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.*;
import com.foodtracker.foodtrackerapi.services.ShoppingListService;

<<<<<<< HEAD
<<<<<<< HEAD
=======
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
>>>>>>> 90d7a59 (src: Initial commit)
=======
>>>>>>> c8c9ba2 (backend: Cleanup repositories)
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("api/shoppinglist")
public class ShoppingListResource {

<<<<<<< HEAD
<<<<<<< HEAD
=======
    private static Logger logger = LogManager.getLogger(ShoppingListResource.class);

>>>>>>> 90d7a59 (src: Initial commit)
=======
>>>>>>> c8c9ba2 (backend: Cleanup repositories)
    @Autowired
    ShoppingListService shoppingListService;

    @GetMapping(path = "/current/{userId}", produces = "application/json")
    public ResponseEntity<Optional<CurrentShoppingList>> getCurrentShoppingListByID(@PathVariable("userId") Integer userId) {
        Optional<CurrentShoppingList> shoppinglist = shoppingListService.getCurrentShoppingListByID(userId);
        return new ResponseEntity<>(shoppinglist, HttpStatus.OK);
    }

    @GetMapping(path = "/old/{userId}", produces="application/json")
    public ResponseEntity<Optional<OldShoppingList>> getOldShoppingListByID(@PathVariable("userId") Integer userId) {
        Optional<OldShoppingList> shoppinglist = shoppingListService.getOldShoppingListByID(userId);
        return new ResponseEntity<>(shoppinglist, HttpStatus.OK);
    }

    @PutMapping(path="/old/update/all", produces="application/json")
    public Integer updateAllItemsOldShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        ArrayList<Map<String, Object>> shoppingList = (ArrayList<Map<String, Object>>) shoppingListMap.get("shoppingList");
        return shoppingListService.updateAllItemsOldShoppingList(userId, mapToProductList(shoppingList));
    }

    @PutMapping(path="/current/update/all", produces="application/json")
    public Integer updateAllItemsCurrentShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        ArrayList<Map<String, Object>> shoppingList = (ArrayList<Map<String, Object>>) shoppingListMap.get("shoppingList");
        return shoppingListService.updateAllItemsOldShoppingList(userId, mapToProductList(shoppingList));
    }

    @PostMapping(path="/current/create", produces = "application/json")
    public Integer createNewProductInCurrentShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Product product = mapToProduct(shoppingListMap);
        // add new product
        return shoppingListService.createProductForCurrentShoppingList(userId, product);
    }

    @PostMapping(path="/current/add", produces = "application/json")
    public Integer addProductInCurrentShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Integer productId = (Integer) shoppingListMap.get("productId");
        // assign product by id to old shoppinglist, do not add new product
        return shoppingListService.addProductInCurrentShoppingList(userId, productId);
    }

    @PostMapping(path="/old/add", produces = "application/json")
    public Integer addProductInOldShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Integer productId = (Integer) shoppingListMap.get("productId");
        // assign product by id to old shoppinglist, do not add new product
        return shoppingListService.addProductInOldShoppingList(userId, productId);
    }

    @PutMapping("/current/update/single")
    public Integer updateProductInCurrentShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Product product = mapToProduct(shoppingListMap);
        return shoppingListService.updateProductInCurrentShoppingList(userId, product);
    }

    @PutMapping("/old/update/single")
    public Integer updateProductInOldShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Product product = mapToProduct(shoppingListMap);
        return shoppingListService.updateProductInOldShoppingList(userId, product);
    }

    @DeleteMapping("/current/delete/single") 
    public Integer deleteProductInCurrentShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        Integer userId = (Integer) shoppingListMap.get("userId");
        Integer productId = (Integer) shoppingListMap.get("productId");
        return shoppingListService.removeProductInCurrentShoppingList(userId, productId);
    }

    @DeleteMapping("/old/delete/single") 
    public Integer deleteProductInOldShoppingList(@RequestBody Map<String, Object> shoppingListMap) {
        int userId = (int) shoppingListMap.get("userId");
        int productId = (int) shoppingListMap.get("productId");
        return shoppingListService.removeProductInOldShoppingList(userId, productId);
    }

    private ArrayList<Product> mapToProductList(ArrayList<Map<String, Object>> shoppingListMap) {
        ArrayList<Product> products = new ArrayList<>();
        for (Map<String, Object> product : shoppingListMap) {
            products.add(mapToProduct(product));
        }
        return products;
    }

    private Product mapToProduct(Map<String, Object> productMap) { 
        Integer productId = (Integer) productMap.get("productId");
        String name = (String) productMap.get("productName");
        String dateStr = (String) productMap.get("expirationDate");
        Date expirationDate = convertStringToDate(dateStr);
        String quantity = (String) productMap.get("quantity");
        String manufacturer = (String) productMap.get("manufacturer");
        String nutritionValue = (String) productMap.get("nutritionValue");
        ArrayList<Map<String, Object>> tags = (ArrayList<Map<String, Object>>) productMap.get("productTags"); // Nested array
        ArrayList<Tag> productTag = mapToTagsList(tags);
        Map<String, Object> imageMap = (Map<String, Object>) productMap.get("productImage");
        Image image = mapToImage(imageMap);
        return new Product(productId, name, expirationDate, quantity, manufacturer, nutritionValue, image, productTag);
    }
    
    private ArrayList<Tag> mapToTagsList(ArrayList<Map<String, Object>> tags) {
        ArrayList<Tag> tagsList = new ArrayList<>();
        for (Map<String, Object> tag : tags) {
            Integer tagId = (Integer) tag.get("tagId");
            String label = (String) tag.get("label");
            tagsList.add(new Tag(tagId, label));
        }
        return tagsList;
    }

    private Image mapToImage(Map<String, Object> image){
        Integer imageId = (Integer) image.get("imageId");
        String imageName = (String) image.get("imageName");
        String imageUrl = (String) image.get("imageUrl");
        return new Image(imageId, imageName, imageUrl, null);
    }

    private boolean isDateStrValid(String dateStr) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private Date convertStringToDate(String dateStr) {
        Date date = null;
        if (!isDateStrValid(dateStr)) {
            dateStr = "2022-02-02";
        }
        try {
            date = Date.valueOf(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}
