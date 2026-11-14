package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * Tests for {@link RecipeShoppingServiceImpl}.
 *
 * <p>The cases of a kitchen: the pantry holds a part of the recipe, it holds
 * nothing, the user holds no pantry at all, the recipe does not exist, and
 * the list refuses a product.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeShoppingServiceImplTest {

    private static final Integer USER_ID = 1;

    private static final Integer RECIPE_ID = 7;

    @Mock
    private RecipeService recipeService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private ShoppingListService shoppingListService;

    private RecipeShoppingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RecipeShoppingServiceImpl(recipeService, inventoryService,
                shoppingListService);
    }

    private static Ingredient ingredient(String name, String quantity) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientName(name);
        ingredient.setQuantity(quantity);
        return ingredient;
    }

    private static Product product(String name) {
        Product product = new Product();
        product.setProductName(name);
        product.setExpirationDate(new Date(System.currentTimeMillis() + 86_400_000L));
        return product;
    }

    private void givenRecipe(Ingredient... ingredients) {
        Recipe recipe = new Recipe();
        recipe.setIngredients(new ArrayList<>(Arrays.asList(ingredients)));
        when(recipeService.getRecipeByID(RECIPE_ID)).thenReturn(Optional.of(recipe));
    }

    private void givenPantry(Product... products) {
        Inventorylist list = new Inventorylist();
        list.setInventoryList(new ArrayList<>(Arrays.asList(products)));
        when(inventoryService.getInventoryList(USER_ID)).thenReturn(Optional.of(list));
    }

    @Test
    @DisplayName("writes only the ingredients that the pantry does not hold")
    void writesOnlyWhatIsMissing() {
        givenRecipe(ingredient("Onions", "3 pieces"), ingredient("Flour", "500 gr"));
        givenPantry(product("Onion"));
        when(shoppingListService.createProductForCurrentShoppingList(any(), any()))
                .thenReturn(42);

        Integer added = service.addMissingProductsForRecipe(USER_ID, RECIPE_ID);

        assertThat(added).isEqualTo(1);
        ArgumentCaptor<Product> written = ArgumentCaptor.forClass(Product.class);
        verify(shoppingListService).createProductForCurrentShoppingList(any(),
                written.capture());
        assertThat(written.getValue().getProductName()).isEqualTo("Flour");
        assertThat(written.getValue().getQuantity()).isEqualTo("500 gr");
        assertThat(written.getValue().getExpirationDate()).isAfter(new Date());
    }

    @Test
    @DisplayName("a recipe that does not exist leaves the list as it is")
    void aRecipeThatIsGoneChangesNothing() {
        when(recipeService.getRecipeByID(RECIPE_ID)).thenReturn(Optional.empty());

        Integer added = service.addMissingProductsForRecipe(USER_ID, RECIPE_ID);

        assertThat(added).isZero();
        verify(shoppingListService, never()).createProductForCurrentShoppingList(any(), any());
    }

    @Test
    @DisplayName("a user without a pantry needs every ingredient")
    void aUserWithoutAPantryNeedsEverything() {
        givenRecipe(ingredient("Salt", "1 tsp"), ingredient("Oil", "25 ml"));
        when(inventoryService.getInventoryList(USER_ID)).thenReturn(Optional.empty());
        when(shoppingListService.createProductForCurrentShoppingList(any(), any()))
                .thenReturn(11);

        assertThat(service.addMissingProductsForRecipe(USER_ID, RECIPE_ID)).isEqualTo(2);
    }

    @Test
    @DisplayName("a product that the list refuses is not counted")
    void aProductThatTheListRefusesIsNotCounted() {
        givenRecipe(ingredient("Salt", "1 tsp"));
        givenPantry();
        when(shoppingListService.createProductForCurrentShoppingList(any(), any()))
                .thenReturn(0);

        assertThat(service.addMissingProductsForRecipe(USER_ID, RECIPE_ID)).isZero();
    }

    @Test
    @DisplayName("an ingredient without an amount reaches the list with one piece")
    void anIngredientWithoutAnAmountGetsOnePiece() {
        givenRecipe(ingredient("Pepper", null), ingredient("Sugar", "  "));
        givenPantry();
        when(shoppingListService.createProductForCurrentShoppingList(any(), any()))
                .thenReturn(5);

        service.addMissingProductsForRecipe(USER_ID, RECIPE_ID);

        ArgumentCaptor<Product> written = ArgumentCaptor.forClass(Product.class);
        verify(shoppingListService, times(2))
                .createProductForCurrentShoppingList(any(), written.capture());
        List<Product> products = written.getAllValues();
        assertThat(products).extracting(Product::getQuantity)
                .containsExactly("1 Piece", "1 Piece");
    }

    @Test
    @DisplayName("a recipe without ingredients leaves the list as it is")
    void aRecipeWithoutIngredientsChangesNothing() {
        givenRecipe();
        givenPantry(product("Onion"));

        assertThat(service.addMissingProductsForRecipe(USER_ID, RECIPE_ID)).isZero();
        verify(shoppingListService, never()).createProductForCurrentShoppingList(anyInt(),
                any(Product.class));
    }
}
