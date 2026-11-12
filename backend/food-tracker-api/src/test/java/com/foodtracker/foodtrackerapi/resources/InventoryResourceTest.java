package com.foodtracker.foodtrackerapi.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.services.InventoryService;

/**
 * Tests for {@link InventoryResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryResourceTest {

    /** A product as the Android client sends it. */
    private static final String PRODUCT_BODY = "{\"userId\":1,\"productId\":42,"
            + "\"productName\":\"Milk\",\"expirationDate\":\"2022-02-02\","
            + "\"quantity\":\"1 Package\",\"manufacturer\":\"Dairy\","
            + "\"nutritionValue\":\"250 kcal\","
            + "\"productImage\":{\"imageId\":7,\"imageName\":\"Milk\",\"imageUrl\":\"url\"},"
            + "\"productTags\":[{\"tagId\":2,\"label\":\"Vegan\"}]}";

    @Mock
    private InventoryService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new InventoryResource(productService)).build();
    }

    @Test
    @DisplayName("The add endpoint reads the product and the user of the body")
    void addEndpointReadsProductAndUser() throws Exception {
        when(productService.addProductToInventoryList(any(Product.class), anyInt()))
                .thenReturn(42);

        mockMvc.perform(post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42));

        ArgumentCaptor<Product> product = ArgumentCaptor.forClass(Product.class);
        verify(productService).addProductToInventoryList(product.capture(), anyInt());
        assertThat(product.getValue().getProductName()).isEqualTo("Milk");
        assertThat(product.getValue().getExpirationDate()).isEqualTo(Date.valueOf("2022-02-02"));
        assertThat(product.getValue().getProductTags()).hasSize(1);
        assertThat(product.getValue().getProductImage().getImageId()).isEqualTo(7);
    }

    @Test
    @DisplayName("The add endpoint accepts a product without an image and without tags")
    void addEndpointAcceptsProductWithoutImageAndTags() throws Exception {
        mockMvc.perform(post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productName\":\"Bread\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Product> product = ArgumentCaptor.forClass(Product.class);
        verify(productService).addProductToInventoryList(product.capture(), anyInt());
        assertThat(product.getValue().getProductTags()).isEmpty();
        assertThat(product.getValue().getExpirationDate())
                .isEqualTo(Date.valueOf(RequestMapper.DEFAULT_EXPIRATION_DATE));
    }

    @Test
    @DisplayName("The update endpoint gives the number of rows that changed")
    void updateEndpointGivesChangedRows() throws Exception {
        when(productService.updateProductOnInventoryListById(any(Product.class), anyInt()))
                .thenReturn(1);

        mockMvc.perform(put("/api/products/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("The image endpoint reads the image of the body")
    void imageEndpointReadsImage() throws Exception {
        when(productService.updateProductImage(any(Image.class))).thenReturn(1);

        mockMvc.perform(put("/api/products/update/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageId\":7,\"imageName\":\"Milk\",\"imageUrl\":\"url\","
                        + "\"imageBytes\":\"RmVlZE1l\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Image> image = ArgumentCaptor.forClass(Image.class);
        verify(productService).updateProductImage(image.capture());
        assertThat(image.getValue().getBase64ImageString()).isEqualTo("RmVlZE1l");
    }

    @Test
    @DisplayName("The delete endpoint only takes the product off the list")
    void deleteEndpointTakesProductOffList() throws Exception {
        when(productService.removeProductOnInventoryListById(1, 42)).thenReturn(1);

        mockMvc.perform(delete("/api/products/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("The count endpoint uses the period of three days")
    void countEndpointUsesThreeDays() throws Exception {
        when(productService.countExpiringProducts(1, InventoryResource.EXPIRATION_WARNING_DAYS))
                .thenReturn(2);

        mockMvc.perform(get("/api/products/count/expiring/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));

        assertThat(InventoryResource.EXPIRATION_WARNING_DAYS).isEqualTo(3);
    }

    @Test
    @DisplayName("The inventory endpoint gives the list of the user")
    void inventoryEndpointGivesList() throws Exception {
        Inventorylist list = new Inventorylist();
        list.setInventoryId(5);
        list.addProductToInventoryList(new Product(42, "Milk", Date.valueOf("2022-02-02"),
                "1 Package", "Dairy", "250 kcal"));
        when(productService.getInventoryList(1)).thenReturn(Optional.of(list));

        mockMvc.perform(get("/api/products/all/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(5))
                .andExpect(jsonPath("$.inventoryList[0].productName").value("Milk"));
    }

    @Test
    @DisplayName("The endpoint for expiring products gives the products")
    void expiringEndpointGivesProducts() throws Exception {
        ArrayList<Product> products = new ArrayList<>(Collections.singletonList(
                new Product(42, "Milk", Date.valueOf("2022-02-02"), "1 Package", "Dairy",
                        "250 kcal")));
        when(productService.getAllProductWithExpDateUnder(1,
                InventoryResource.EXPIRATION_WARNING_DAYS)).thenReturn(Optional.of(products));

        mockMvc.perform(get("/api/products/all/expiring/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Milk"));
    }

    @Test
    @DisplayName("The endpoint for all products reads the nested array of the body")
    void updateAllEndpointReadsNestedArray() throws Exception {
        when(productService.updateAllProductsOnInventoryListByID(anyInt(), any())).thenReturn(2);

        mockMvc.perform(put("/api/products/all/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"inventoryList\":["
                        + "{\"productId\":42,\"productName\":\"Milk\"},"
                        + "{\"productId\":43,\"productName\":\"Bread\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ArrayList<Product>> products = ArgumentCaptor.forClass(ArrayList.class);
        verify(productService).updateAllProductsOnInventoryListByID(anyInt(), products.capture());
        assertThat(products.getValue()).extracting(Product::getProductName)
                .containsExactly("Milk", "Bread");
    }

    @Test
    @DisplayName("The endpoint for all products accepts a body without an array")
    void updateAllEndpointAcceptsBodyWithoutArray() throws Exception {
        mockMvc.perform(put("/api/products/all/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1}"))
                .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ArrayList<Product>> products = ArgumentCaptor.forClass(ArrayList.class);
        verify(productService).updateAllProductsOnInventoryListByID(anyInt(), products.capture());
        assertThat(products.getValue()).isEmpty();
    }

    @Test
    @DisplayName("The dashboard counts the products that expire soon")
    void dashboardCountsExpiringProducts() throws Exception {
        List<Product> products = Collections.singletonList(
                new Product(42, "Milk", null, null, null, null));
        when(productService.getProductWithShortExpDate(1)).thenReturn(products);

        MockMvc dashboard = MockMvcBuilders
                .standaloneSetup(new DashboardResource(null, productService)).build();

        dashboard.perform(get("/api/dashboard/numshortexp/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}
