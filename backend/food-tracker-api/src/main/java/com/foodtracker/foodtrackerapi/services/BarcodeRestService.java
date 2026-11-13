package com.foodtracker.foodtrackerapi.services;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.foodtracker.foodtrackerapi.common.rest.JsonHttpClient;
import com.foodtracker.foodtrackerapi.common.rest.JsonResponses;
import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.models.BarcodeProduct;

/**
 * <p>
 * Reads the data of a product from the Edamam food database. The client scans
 * the barcode with the ZXing library and sends the numerical code to this
 * service.
 * </p>
 *
 * <p>
 * See the
 * <a href="https://developer.edamam.com/food-database-api-docs">Edamam
 * documentation</a>.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
public class BarcodeRestService {

    /** Address of the Edamam food database. */
    static final String EDAMAM_URL = "https://api.edamam.com/api/food-database/v2/parser";

    /** Name of the array that holds the results of a search. */
    private static final String HINTS = "hints";

    private static final Logger LOG = LogManager.getLogger(BarcodeRestService.class);

    private final JsonHttpClient httpClient;

    /**
     * Makes the service.
     *
     * @param httpClient client that sends the request to the external API
     */
    public BarcodeRestService(JsonHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Reads the product that belongs to a barcode.
     *
     * @param appId   application ID of the Edamam API
     * @param appKey  key of the Edamam API
     * @param upcCode barcode of the product. A valid UPC, EAN or PLU code.
     * @return the product. All values are empty if the external API gives no
     *         result.
     */
    public Optional<BarcodeProduct> getProduct(String appId, String appKey, String upcCode) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("app_id", appId);
        queryParams.put("app_key", appKey);
        queryParams.put("upc", upcCode);
        queryParams.put("nutrition-type", "cooking");

        String responseJson = httpClient.getJson(EDAMAM_URL, queryParams);
        return Optional.of(parseProduct(responseJson));
    }

    /**
     * Reads a product out of the answer of the external API.
     *
     * @param jsonResponse answer of the API. It can be {@code null}.
     * @return the product. All values are empty if the answer holds no product.
     */
    BarcodeProduct parseProduct(String jsonResponse) {
        if (!JsonResponses.holdsData(jsonResponse)) {
            LOG.debug("The Edamam API gave no product.");
            return BarcodeProduct.empty();
        }
        return Attempt.orDefault("read the product of the Edamam API", BarcodeProduct.empty(),
                () -> {
                    JSONArray hints = new JSONObject(jsonResponse).getJSONArray(HINTS);
                    // The first element holds the product that fits the barcode best.
                    JSONObject food = hints.getJSONObject(0).getJSONObject("food");
                    BarcodeProduct product = new BarcodeProduct(
                            food.optString("foodId"),
                            food.optString("label"),
                            food.getJSONObject("nutrients").optString("ENERC_KCAL"),
                            food.optString("brand"),
                            food.optString("category"),
                            food.optString("categoryLabel"));
                    LOG.debug("Read the product '{}' from the Edamam API.", product.getLabel());
                    return product;
                });
    }
}
