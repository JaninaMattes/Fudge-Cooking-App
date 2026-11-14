package com.foodtracker.foodtrackerapi.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.foodtracker.foodtrackerapi.services.RecipeShoppingService;

/**
 * Tests for {@link RecipeShoppingResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeShoppingResourceTest {

    private static final String PATH = "/api/shoppinglist/current/add/missing";

    @Mock
    private RecipeShoppingService recipeShoppingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RecipeShoppingResource(recipeShoppingService))
                .build();
    }

    @Test
    @DisplayName("gives back how many products reached the list")
    void givesTheNumberOfProducts() throws Exception {
        when(recipeShoppingService.addMissingProductsForRecipe(1, 7)).thenReturn(3);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"recipeId\":7}"))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(recipeShoppingService).addMissingProductsForRecipe(1, 7);
    }

    @Test
    @DisplayName("a body without the keys reaches the service with no key")
    void aBodyWithoutKeysIsAnswered() throws Exception {
        when(recipeShoppingService.addMissingProductsForRecipe(any(), any())).thenReturn(0);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}
