package com.foodtracker.foodtrackerapi.resources;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class InventoryResource {

    @Autowired
    InventoryService productService;

    @PostMapping("/add")
    public ResponseEntity<Integer> addProductToInventaryList(@RequestBody Map<String, Object> productMap){
        Integer userId = (Integer) productMap.get("userId");
        Product product = mapToProduct(productMap);
        Integer productId = productService.addProductToInventoryList(product, userId);
        return new ResponseEntity<>(productId, HttpStatus.OK);
    }

    @PutMapping("/update")
    public Integer updateProductOnIneventaryListByID(@RequestBody Map<String, Object> productMap) {
        Integer userId = (Integer) productMap.get("userId");
        Product product = mapToProduct(productMap);
        return productService.updateProductOnInventoryListById(product, userId);
    }

    @PutMapping("/update/image")
    public Integer updateProductImageOnIneventaryListByID(@RequestBody Map<String, Object> imageMap) {
        Image image = mapToImage(imageMap);
        return productService.updateProductImage(image);
    }

    @DeleteMapping("/delete") 
    public Integer deleteProductOnInventaryList(@RequestBody Map<String, Object> productMap){
        Integer productId = (Integer) productMap.get("productId");
        Integer userId = (Integer) productMap.get("userId");
        return productService.removeProductOnInventoryListById(userId, productId);
    }

    // polling
    @GetMapping(path = "/count/expiring/{userId}", produces = "application/json")
    public ResponseEntity<Integer> countExpiringProducts(@PathVariable("userId") Integer userId) {
        Integer result = productService.countExpiringProducts(userId, 3);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path="/all/{userId}", produces = "application/json")
    public ResponseEntity<Optional<Inventorylist>> getInventoryList(@PathVariable("userId") Integer userId){
        Optional<Inventorylist> inventory = productService.getInventoryList(userId);
        return new ResponseEntity<>(inventory, HttpStatus.OK);
    }

    @GetMapping(path = "/all/expiring/{userId}", produces = "application/json")
    public ResponseEntity<Optional<ArrayList<Product>>> getAllExpiringProducts(@PathVariable("userId") Integer userId) {
        Optional<ArrayList<Product>> expProducts = productService.getAllProductWithExpDateUnder(userId, 3);
        return new ResponseEntity<>(expProducts, HttpStatus.OK);
    }


    @PutMapping("/all/update")
    public Integer updateAllProductsOnIneventaryListByID(@RequestBody Map<String, Object> productMap) {
        Integer userId = (Integer) productMap.get("userId");
        ArrayList<Map<String, Object>> inventoryList = (ArrayList<Map<String, Object>>) productMap.get("inventoryList");
        return productService.updateAllProductsOnInventoryListByID(userId, mapToInventoryList(inventoryList));
    }

    private Product mapToProduct(Map<String, Object> productMap) { 
        Integer productId = (Integer) productMap.get("productId");
        String name = (String) productMap.get("productName");
        String dateStr = (String) productMap.get("expirationDate");
        Date expirationDate = convertStringToDate(dateStr);
        String quantity = (String) productMap.get("quantity");
        String manufacturer = (String) productMap.get("manufacturer");
        String nutritionValue = (String) productMap.get("nutritionValue");
        ArrayList<Map<String, Object>> tags = (ArrayList<Map<String, Object>>) productMap.get("productTags"); // Nested                                                                                             // array
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

    private ArrayList<Product> mapToInventoryList(ArrayList<Map<String, Object>> inventoryList) {
        ArrayList<Product> products = new ArrayList<>();
        for (Map<String, Object> product : inventoryList) {
            products.add(mapToProduct(product));
        }
        return products;
    }

    /**
     * <p>
     * Maps the incomming map<string, obj> to an image object.
     * </p>
     * 
     * @param image
     * @return Image
     * 
     * @author Janina Mattes
     */
    private Image mapToImage(Map<String, Object> image) {
        // map incomming map<string, obj> to an image
        Integer imageId = (Integer) image.get("imageId");
        String imageName = (String) image.get("imageName");
        String imageUrl = (String) image.get("imageUrl");
        String base64String = (String) image.get("imageBytes");
        // base64 encoding
        return new Image(imageId, imageName, imageUrl, base64String);
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
