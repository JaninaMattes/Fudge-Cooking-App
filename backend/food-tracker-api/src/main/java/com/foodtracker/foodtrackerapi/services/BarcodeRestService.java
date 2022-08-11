package com.foodtracker.foodtrackerapi.services;

import java.time.Duration;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.foodtracker.foodtrackerapi.models.BarcodeProduct;

/**
 * Get Product information based on the barcode code decoded via ZXing library in client.
 * 
 * Edamam API Documentation: https://developer.edamam.com/food-database-api-docs#/
 * WebClient Spring Boot: https://www.baeldung.com/webflux-webclient-parameters
 * 
 * @author Janina Mattes
 */
@Service
public class BarcodeRestService {
    
    private Logger logger = LogManager.getLogger(BarcodeRestService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    // ups query paramter: valid UPC, EAN, or PLU code. Not required when ingr is present
    public Optional<BarcodeProduct> getProduct(String appId, String appKey, String upcCode) {

        String responseJson = "";

        try {
            responseJson = WebClient.create("https://api.edamam.com/api/food-database/v2/parser")
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("app_id", appId)
                            .queryParam("app_key", appKey)
                            .queryParam("upc", upcCode)
                            .queryParam("nutrition-type", "cooking")
                            .build())
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

        } catch (Exception err) {
            this.logger.debug("Error occured when parsing Edamam API", err);
        }
        // parse product from json string
        BarcodeProduct result = parseJsonDataToProduct(responseJson);
        return Optional.of(result);
    }

    /**
     * Map JsonObject to BarcodeProduct
     * 
     * @param jsonResponse
     * @return
     */
    private BarcodeProduct parseJsonDataToProduct(String jsonResponse) {
        BarcodeProduct result = new BarcodeProduct();

        try {

            if (jsonResponse != null && !jsonResponse.isEmpty() && !jsonResponse.contains("error") 
                    && jsonResponse.length() > 4) {

                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONArray jsonArray = jsonObject.getJSONArray("hints");

                // first element 0 is "food"
                String foodId = jsonArray.getJSONObject(0).getJSONObject("food").optString("foodId");
                String label = jsonArray.getJSONObject(0).getJSONObject("food").optString("label");
                String nutrients = jsonArray.getJSONObject(0).getJSONObject("food").getJSONObject("nutrients")
                        .optString("ENERC_KCAL");
                String brand = jsonArray.getJSONObject(0).getJSONObject("food").optString("brand");
                String category = jsonArray.getJSONObject(0).getJSONObject("food").optString("category");
                String categoryLabel = jsonArray.getJSONObject(0).getJSONObject("food").optString("categoryLabel");

                this.logger.log(Level.DEBUG, "Parsed product from Edamam API: " + label);

                result.setFoodId(foodId);
                result.setLabel(label);
                result.setNutrients(nutrients);
                result.setBrand(brand);
                result.setCategory(category);
                result.setCategoryLabel(categoryLabel);

            } else {
                // create empty values
                String emptyString = "";
                result.setFoodId(emptyString);
                result.setLabel(emptyString);
                result.setNutrients(emptyString);
                result.setBrand(emptyString);
                result.setCategory(emptyString);
                result.setCategoryLabel(emptyString);
            }

        } catch (JSONException e) {
            this.logger.debug("Error occured parsing JsonString to JsonObject", e);
            e.printStackTrace();
        }

        return result;
    }
}