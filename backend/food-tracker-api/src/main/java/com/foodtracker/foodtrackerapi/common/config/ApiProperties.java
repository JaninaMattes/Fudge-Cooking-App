package com.foodtracker.foodtrackerapi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 * All credentials and tuning values of the API. Spring reads the values from
 * {@code application.properties}, which itself reads them from environment
 * variables. Credentials are therefore not in the source code.
 * </p>
 *
 * <p>
 * Example of the environment for a local start:
 * </p>
 *
 * <pre>
 * export FT_API_SECRET_KEY=change-me
 * export FT_SPOONACULAR_KEY=...
 * export FT_EDAMAM_APP_ID=...
 * export FT_EDAMAM_BARCODE_KEY=...
 * </pre>
 *
 * @author Janina Mattes
 */
@Component
@ConfigurationProperties(prefix = "foodtracker.api")
public class ApiProperties {

    /** Signature key of the JWT tokens. */
    private String secretKey = "";

    /** Lifetime of a JWT token in milliseconds. The default is two hours. */
    private long tokenValidityMillis = 2 * 60 * 60 * 1000L;

    /** Key of the Spoonacular recipe API. */
    private String spoonacularRecipeKey = "";

    /** Application ID of the Edamam food database API. */
    private String edamamAppId = "";

    /** Key of the Edamam barcode API. */
    private String edamamBarcodeKey = "";

    /** Key of the Edamam recipe API. */
    private String edamamRecipeKey = "";

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getTokenValidityMillis() {
        return tokenValidityMillis;
    }

    public void setTokenValidityMillis(long tokenValidityMillis) {
        this.tokenValidityMillis = tokenValidityMillis;
    }

    public String getSpoonacularRecipeKey() {
        return spoonacularRecipeKey;
    }

    public void setSpoonacularRecipeKey(String spoonacularRecipeKey) {
        this.spoonacularRecipeKey = spoonacularRecipeKey;
    }

    public String getEdamamAppId() {
        return edamamAppId;
    }

    public void setEdamamAppId(String edamamAppId) {
        this.edamamAppId = edamamAppId;
    }

    public String getEdamamBarcodeKey() {
        return edamamBarcodeKey;
    }

    public void setEdamamBarcodeKey(String edamamBarcodeKey) {
        this.edamamBarcodeKey = edamamBarcodeKey;
    }

    public String getEdamamRecipeKey() {
        return edamamRecipeKey;
    }

    public void setEdamamRecipeKey(String edamamRecipeKey) {
        this.edamamRecipeKey = edamamRecipeKey;
    }
}
