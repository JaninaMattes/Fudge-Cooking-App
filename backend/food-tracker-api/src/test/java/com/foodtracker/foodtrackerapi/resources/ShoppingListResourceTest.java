package com.foodtracker.foodtrackerapi.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
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

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.services.ShoppingListService;

/**
 * Tests for {@link ShoppingListResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShoppingListResourceTest {

    private static final String LIST_BODY = "{\"userId\":1,\"shoppingList\":["
            + "{\"productId\":42,\"productName\":\"Milk\"},"
            + "{\"productId\":43,\"productName\":\"Bread\"}]}";

    @Mock
    private ShoppingListService shoppingListService;

    private MockMvc mockMvc;

    private static Product product(Integer id, String name) {
        return new Product(id, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ShoppingListResource(shoppingListService)).build();
    }

    @Test
    @DisplayName("The endpoint of the current list gives the products")
    void currentListEndpointGivesProducts() throws Exception {
        CurrentShoppingList list = new CurrentShoppingList(1,
                Collections.singletonList(product(42, "Milk")));
        when(shoppingListService.getCurrentShoppingListByID(1)).thenReturn(Optional.of(list));

        mockMvc.perform(get("/api/shoppinglist/current/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingListId").value(1))
                .andExpect(jsonPath("$.shoppingList[0].productName").value("Milk"));
    }

    @Test
    @DisplayName("The endpoint of the old list gives the products")
    void oldListEndpointGivesProducts() throws Exception {
        OldShoppingList list = new OldShoppingList(1,
                Collections.singletonList(product(42, "Milk")));
        when(shoppingListService.getOldShoppingListByID(1)).thenReturn(Optional.of(list));

        mockMvc.perform(get("/api/shoppinglist/old/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingList[0].productName").value("Milk"));
    }

    @Test
    @DisplayName("The endpoint of the current list changes the current list")
    void currentListEndpointChangesCurrentList() throws Exception {
        // The endpoint changed the old list before.
        when(shoppingListService.updateAllItemsCurrentShoppingList(anyInt(), any()))
                .thenReturn(2);

        mockMvc.perform(put("/api/shoppinglist/current/update/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LIST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));

        verify(shoppingListService, never()).updateAllItemsOldShoppingList(anyInt(), any());
    }

    @Test
    @DisplayName("The endpoint of the old list changes the old list")
    void oldListEndpointChangesOldList() throws Exception {
        when(shoppingListService.updateAllItemsOldShoppingList(anyInt(), any())).thenReturn(2);

        mockMvc.perform(put("/api/shoppinglist/old/update/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(LIST_BODY))
                .andExpect(status().isOk());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<ArrayList<Product>> products = ArgumentCaptor.forClass(ArrayList.class);
        verify(shoppingListService).updateAllItemsOldShoppingList(anyInt(), products.capture());
        assertThat(products.getValue()).extracting(Product::getProductName)
                .containsExactly("Milk", "Bread");
        verify(shoppingListService, never()).updateAllItemsCurrentShoppingList(anyInt(), any());
    }

    @Test
    @DisplayName("The create endpoint makes a product on the current list")
    void createEndpointMakesProductOnCurrentList() throws Exception {
        when(shoppingListService.createProductForCurrentShoppingList(anyInt(), any()))
                .thenReturn(42);

        mockMvc.perform(post("/api/shoppinglist/current/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productName\":\"Milk\","
                        + "\"expirationDate\":\"2022-02-02\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42));

        ArgumentCaptor<Product> product = ArgumentCaptor.forClass(Product.class);
        verify(shoppingListService).createProductForCurrentShoppingList(anyInt(),
                product.capture());
        assertThat(product.getValue().getProductName()).isEqualTo("Milk");
    }

    @Test
    @DisplayName("Each add endpoint moves a product between the two lists")
    void addEndpointsMoveProduct() throws Exception {
        when(shoppingListService.addProductInCurrentShoppingList(1, 42)).thenReturn(1);
        when(shoppingListService.addProductInOldShoppingList(1, 42)).thenReturn(1);

        mockMvc.perform(post("/api/shoppinglist/current/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/shoppinglist/old/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42}"))
                .andExpect(status().isOk());

        verify(shoppingListService).addProductInCurrentShoppingList(1, 42);
        verify(shoppingListService).addProductInOldShoppingList(1, 42);
    }

    @Test
    @DisplayName("Each update endpoint changes one product of its own list")
    void updateEndpointsChangeOneProduct() throws Exception {
        when(shoppingListService.updateProductInCurrentShoppingList(anyInt(), any()))
                .thenReturn(1);
        when(shoppingListService.updateProductInOldShoppingList(anyInt(), any())).thenReturn(1);

        mockMvc.perform(put("/api/shoppinglist/current/update/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42,\"productName\":\"Milk\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/shoppinglist/old/update/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42,\"productName\":\"Milk\"}"))
                .andExpect(status().isOk());

        verify(shoppingListService).updateProductInCurrentShoppingList(anyInt(), any());
        verify(shoppingListService).updateProductInOldShoppingList(anyInt(), any());
    }

    @Test
    @DisplayName("Each delete endpoint only takes the product off its own list")
    void deleteEndpointsTakeProductOffList() throws Exception {
        when(shoppingListService.removeProductInCurrentShoppingList(1, 42)).thenReturn(1);
        when(shoppingListService.removeProductInOldShoppingList(1, 42)).thenReturn(1);

        mockMvc.perform(delete("/api/shoppinglist/current/delete/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/shoppinglist/old/delete/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"productId\":42}"))
                .andExpect(status().isOk());

        verify(shoppingListService).removeProductInCurrentShoppingList(1, 42);
        verify(shoppingListService).removeProductInOldShoppingList(1, 42);
    }
}
