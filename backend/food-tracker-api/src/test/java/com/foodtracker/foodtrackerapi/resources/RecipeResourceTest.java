package com.foodtracker.foodtrackerapi.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.services.RecipeRestService;
import com.foodtracker.foodtrackerapi.services.RecipeService;

/**
 * Tests for {@link RecipeResource}, {@link RecipeRestResource} and
 * {@link DashboardResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeResourceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private RecipeRestService recipeRestService;

    private ApiProperties apiProperties;
    private MockMvc recipeMvc;
    private MockMvc restMvc;
    private MockMvc dashboardMvc;

    private static Recipe recipe(Integer id, String title) {
        return new Recipe(id, title, "Vegan", "A recipe", 4.5F, 10, "45 min", "easy", "Mix.");
    }

    @BeforeEach
    void setUp() {
        apiProperties = new ApiProperties();
        apiProperties.setSpoonacularRecipeKey("spoonacular-key");
        recipeMvc = MockMvcBuilders.standaloneSetup(new RecipeResource(recipeService)).build();
        restMvc = MockMvcBuilders.standaloneSetup(
                new RecipeRestResource(recipeRestService, recipeService, apiProperties)).build();
        dashboardMvc = MockMvcBuilders.standaloneSetup(
                new DashboardResource(recipeService, null)).build();
    }

    @Test
    @DisplayName("The endpoint for all recipes gives the recipes of the user")
    void allRecipesEndpointGivesRecipes() throws Exception {
        when(recipeService.getAllRecipes(1)).thenReturn(
                Optional.of(Collections.singletonList(recipe(42, "Pizza"))));

        recipeMvc.perform(get("/api/recipe/all/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pizza"));
    }

    @Test
    @DisplayName("The endpoint for the most popular recipe gives the recipe")
    void popularEndpointGivesRecipe() throws Exception {
        when(recipeService.getMostPopularRecipe()).thenReturn(Optional.of(recipe(42, "Pizza")));

        recipeMvc.perform(get("/api/recipe/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pizza"));
    }

    @Test
    @DisplayName("The endpoint for the most popular recipe gives an empty answer")
    void popularEndpointGivesEmptyAnswer() throws Exception {
        when(recipeService.getMostPopularRecipe()).thenReturn(Optional.empty());

        recipeMvc.perform(get("/api/recipe/popular"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("The rating endpoint reads the mark of the body")
    void rateEndpointReadsMark() throws Exception {
        when(recipeService.rateRecipe(42, 5)).thenReturn(1);

        recipeMvc.perform(put("/api/recipe/rate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipeId\":42,\"cummulativeRating\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("The endpoint for one recipe reads the key of the body")
    void recipeByIdEndpointReadsKey() throws Exception {
        when(recipeService.getRecipeByID(42)).thenReturn(Optional.of(recipe(42, "Pizza")));

        recipeMvc.perform(get("/api/recipe/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recipeId\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pizza"));
    }

    @Test
    @DisplayName("The endpoint for a type of recipe reads the type of the body")
    void recipeByTypeEndpointReadsType() throws Exception {
        when(recipeService.getRecipeByType("A recipe"))
                .thenReturn(Optional.of(recipe(42, "Pizza")));

        recipeMvc.perform(get("/api/recipe/type")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"A recipe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pizza"));
    }

    @Test
    @DisplayName("The poll endpoint loads the recipes of the external API and stores them")
    void pollEndpointLoadsAndStoresRecipes() throws Exception {
        List<Recipe> loaded = Collections.singletonList(recipe(null, "Pizza"));
        when(recipeRestService.getMatchingRecipesByUserId(1, "spoonacular-key",
                RecipeRestResource.POLL_SEARCH_COUNT, RecipeRestResource.EXPIRATION_WARNING_DAYS))
                .thenReturn(Optional.of(loaded));
        when(recipeService.loadRecipesFromAPI(loaded, 1)).thenReturn(1);

        restMvc.perform(get("/api/recipe/rest/poll/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("The poll endpoint accepts an external API that gives no recipe")
    void pollEndpointAcceptsEmptyAnswer() throws Exception {
        when(recipeRestService.getMatchingRecipesByUserId(anyInt(), anyString(), anyInt(),
                anyInt())).thenReturn(Optional.empty());

        restMvc.perform(get("/api/recipe/rest/poll/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        verify(recipeService).loadRecipesFromAPI(Collections.emptyList(), 1);
    }

    @Test
    @DisplayName("The list endpoint gives ten recipes")
    void listEndpointGivesTenRecipes() throws Exception {
        when(recipeService.getNRandomRecipes(1, RecipeRestResource.RECIPE_LIST_SIZE))
                .thenReturn(Arrays.asList(recipe(42, "Pizza"), recipe(43, "Pasta")));

        restMvc.perform(get("/api/recipe/rest/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pizza"));

        assertThat(RecipeRestResource.RECIPE_LIST_SIZE).isEqualTo(10);
    }

    @Test
    @DisplayName("The match endpoint gives the recipes of the external API")
    void matchEndpointGivesRecipesOfExternalApi() throws Exception {
        when(recipeRestService.getMatchingRecipesByUserId(1, "spoonacular-key",
                RecipeRestResource.MATCH_SEARCH_COUNT,
                RecipeRestResource.EXPIRATION_WARNING_DAYS))
                .thenReturn(Optional.of(Collections.singletonList(recipe(42, "Pizza"))));

        restMvc.perform(get("/api/recipe/rest/match/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pizza"));
    }

    @Test
    @DisplayName("The endpoint for the best recipes uses the lowest mark of the resource")
    void bestRatedEndpointUsesLimitOfResource() throws Exception {
        when(recipeService.getAllRecipesByRating(RecipeRestResource.BEST_RATING_LIMIT, 1))
                .thenReturn(Optional.of(new ArrayList<>(
                        Collections.singletonList(recipe(42, "Pizza")))));

        restMvc.perform(get("/api/recipe/rest/recipes/rating/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Pizza"));
    }

    @Test
    @DisplayName("The dashboard gives the most popular recipe")
    void dashboardGivesPopularRecipe() throws Exception {
        when(recipeService.getMostPopularRecipe()).thenReturn(Optional.of(recipe(42, "Pizza")));

        dashboardMvc.perform(get("/api/dashboard/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pizza"));
    }

    @Test
    @DisplayName("The dashboard gives the products that expire soon")
    void dashboardGivesExpiringProducts() throws Exception {
        com.foodtracker.foodtrackerapi.services.InventoryService productService =
                org.mockito.Mockito.mock(
                        com.foodtracker.foodtrackerapi.services.InventoryService.class);
        when(productService.getProductWithShortExpDate(1)).thenReturn(
                Collections.singletonList(new com.foodtracker.foodtrackerapi.models.Product(
                        42, "Milk", null, null, null, null)));
        MockMvc dashboard = MockMvcBuilders.standaloneSetup(
                new DashboardResource(recipeService, productService)).build();

        dashboard.perform(get("/api/dashboard/shortexp/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Milk"));
    }

    @Test
    @DisplayName("The dashboard reads a recipe by its type")
    void dashboardReadsRecipeByType() throws Exception {
        when(recipeService.getRecipeByType("A recipe"))
                .thenReturn(Optional.of(recipe(42, "Pizza")));

        dashboardMvc.perform(get("/api/dashboard/type")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"TYPE\":\"A recipe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pizza"));
    }
}
