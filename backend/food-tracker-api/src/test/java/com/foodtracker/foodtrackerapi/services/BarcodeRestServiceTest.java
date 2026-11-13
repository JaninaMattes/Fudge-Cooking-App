package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodtracker.foodtrackerapi.common.rest.JsonHttpClient;
import com.foodtracker.foodtrackerapi.models.BarcodeProduct;

/**
 * Tests for {@link BarcodeRestService}.
 *
 * @author Janina Mattes
 */
class BarcodeRestServiceTest {

    /** A JSON answer of the Edamam API with one product. */
    private static final String EDAMAM_ANSWER = "{\"text\":\"milk\",\"hints\":[{\"food\":{"
            + "\"foodId\":\"food_1\",\"label\":\"Milk\",\"brand\":\"Dairy Ltd\","
            + "\"category\":\"Generic foods\",\"categoryLabel\":\"food\","
            + "\"nutrients\":{\"ENERC_KCAL\":250.0}}}]}";

    private StubHttpClient httpClient;
    private BarcodeRestService service;

    /** A client that gives the answer that the test set. */
    private static final class StubHttpClient implements JsonHttpClient {

        private String answer = "";
        private String lastUrl;
        private Map<String, String> lastParams = new LinkedHashMap<>();

        @Override
        public String getJson(String url, Map<String, String> queryParams) {
            this.lastUrl = url;
            this.lastParams = queryParams;
            return answer;
        }
    }

    @BeforeEach
    void setUp() {
        httpClient = new StubHttpClient();
        service = new BarcodeRestService(httpClient);
    }

    @Test
    @DisplayName("The service reads the product of the answer")
    void readsProductOfAnswer() {
        httpClient.answer = EDAMAM_ANSWER;

        BarcodeProduct product = service.getProduct("app-id", "app-key", "123456")
                .orElseThrow(AssertionError::new);

        assertThat(product.getFoodId()).isEqualTo("food_1");
        assertThat(product.getLabel()).isEqualTo("Milk");
        assertThat(product.getBrand()).isEqualTo("Dairy Ltd");
        assertThat(product.getCategory()).isEqualTo("Generic foods");
        assertThat(product.getCategoryLabel()).isEqualTo("food");
        assertThat(product.getNutrients()).isEqualTo("250.0");
    }

    @Test
    @DisplayName("The service sends the credentials and the barcode to the external API")
    void sendsCredentialsAndBarcode() {
        service.getProduct("app-id", "app-key", "123456");

        assertThat(httpClient.lastUrl).isEqualTo(BarcodeRestService.EDAMAM_URL);
        assertThat(httpClient.lastParams)
                .containsEntry("app_id", "app-id")
                .containsEntry("app_key", "app-key")
                .containsEntry("upc", "123456")
                .containsEntry("nutrition-type", "cooking");
    }

    @ParameterizedTest
    @DisplayName("The service gives a product with empty values for an answer without data")
    @NullAndEmptySource
    @ValueSource(strings = { "{}", "{\"error\":\"the key is not valid\"}" })
    void givesEmptyProductForAnswerWithoutData(String answer) {
        httpClient.answer = answer;

        BarcodeProduct product = service.getProduct("app-id", "app-key", "123456")
                .orElseThrow(AssertionError::new);

        assertThat(product).isEqualTo(BarcodeProduct.empty());
    }

    @Test
    @DisplayName("The service gives a product with empty values for an answer without a product")
    void givesEmptyProductForAnswerWithoutProduct() {
        httpClient.answer = "{\"text\":\"milk\",\"hints\":[]}";

        BarcodeProduct product = service.parseProduct(httpClient.answer);

        assertThat(product).isEqualTo(BarcodeProduct.empty());
    }

    @Test
    @DisplayName("The service gives a product with empty values for an answer that is not JSON")
    void givesEmptyProductForTextThatIsNotJson() {
        assertThat(service.parseProduct("this is not JSON"))
                .isEqualTo(BarcodeProduct.empty());
    }

    @Test
    @DisplayName("The service gives a product with empty values when the energy value is missing")
    void givesEmptyProductWithoutEnergyValue() {
        String answer = "{\"hints\":[{\"food\":{\"foodId\":\"food_1\",\"label\":\"Milk\"}}]}";

        assertThat(service.parseProduct(answer)).isEqualTo(BarcodeProduct.empty());
    }
}
