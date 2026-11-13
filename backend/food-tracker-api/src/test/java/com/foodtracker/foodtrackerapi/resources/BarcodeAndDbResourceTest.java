package com.foodtracker.foodtrackerapi.resources;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.BarcodeProduct;
import com.foodtracker.foodtrackerapi.services.BarcodeRestService;
import com.foodtracker.foodtrackerapi.services.DBService;

/**
 * Tests for {@link BarcodeRestResource} and {@link DBResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BarcodeAndDbResourceTest {

    @Mock
    private BarcodeRestService barcodeService;

    @Mock
    private DBService dbService;

    private MockMvc barcodeMvc;
    private MockMvc dbMvc;

    @BeforeEach
    void setUp() {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setEdamamAppId("app-id");
        apiProperties.setEdamamBarcodeKey("barcode-key");
        barcodeMvc = MockMvcBuilders.standaloneSetup(
                new BarcodeRestResource(barcodeService, apiProperties)).build();
        dbMvc = MockMvcBuilders.standaloneSetup(new DBResource(dbService)).build();
    }

    @Test
    @DisplayName("The barcode endpoint gives the product of the external API")
    void barcodeEndpointGivesProduct() throws Exception {
        BarcodeProduct product = new BarcodeProduct("food_1", "Milk", "250", "Dairy",
                "Generic foods", "food");
        when(barcodeService.getProduct("app-id", "barcode-key", "123456"))
                .thenReturn(Optional.of(product));

        barcodeMvc.perform(get("/api/barcode/rest/123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Milk"))
                .andExpect(jsonPath("$.nutrients").value("250"));
    }

    @Test
    @DisplayName("The barcode endpoint reads the credentials from the configuration")
    void barcodeEndpointReadsCredentialsFromConfiguration() throws Exception {
        when(barcodeService.getProduct("app-id", "barcode-key", "123456"))
                .thenReturn(Optional.of(BarcodeProduct.empty()));

        barcodeMvc.perform(get("/api/barcode/rest/123456"))
                .andExpect(status().isOk());

        verify(barcodeService).getProduct("app-id", "barcode-key", "123456");
    }

    @Test
    @DisplayName("The database endpoint gives the number of corrected sequences")
    void databaseEndpointGivesNumberOfSequences() throws Exception {
        when(dbService.updateDBSequences()).thenReturn(12);

        dbMvc.perform(get("/api/db/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(12));
    }
}
