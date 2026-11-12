package com.foodtracker.foodtrackerapi.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tests for {@link ApiProperties}.
 *
 * @author Janina Mattes
 */
class ApiPropertiesTest {

    @Test
    @DisplayName("A new configuration holds no credential")
    void newConfigurationHoldsNoCredential() {
        ApiProperties properties = new ApiProperties();

        assertThat(properties.getSecretKey()).isEmpty();
        assertThat(properties.getSpoonacularRecipeKey()).isEmpty();
        assertThat(properties.getEdamamAppId()).isEmpty();
        assertThat(properties.getEdamamBarcodeKey()).isEmpty();
        assertThat(properties.getEdamamRecipeKey()).isEmpty();
    }

    @Test
    @DisplayName("A token is valid for two hours when nothing else is configured")
    void tokenIsValidForTwoHours() {
        assertThat(new ApiProperties().getTokenValidityMillis()).isEqualTo(2 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("The access methods read and write all values")
    void accessMethodsWork() {
        ApiProperties properties = new ApiProperties();

        properties.setSecretKey("secret");
        properties.setTokenValidityMillis(1000L);
        properties.setSpoonacularRecipeKey("spoonacular");
        properties.setEdamamAppId("app-id");
        properties.setEdamamBarcodeKey("barcode");
        properties.setEdamamRecipeKey("recipe");

        assertThat(properties.getSecretKey()).isEqualTo("secret");
        assertThat(properties.getTokenValidityMillis()).isEqualTo(1000L);
        assertThat(properties.getSpoonacularRecipeKey()).isEqualTo("spoonacular");
        assertThat(properties.getEdamamAppId()).isEqualTo("app-id");
        assertThat(properties.getEdamamBarcodeKey()).isEqualTo("barcode");
        assertThat(properties.getEdamamRecipeKey()).isEqualTo("recipe");
    }

    @Test
    @DisplayName("Spring reads the values from the prefix foodtracker.api")
    void springReadsValuesFromPrefix() {
        ConfigurationProperties annotation = ApiProperties.class
                .getAnnotation(ConfigurationProperties.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.prefix()).isEqualTo("foodtracker.api");
    }
}
