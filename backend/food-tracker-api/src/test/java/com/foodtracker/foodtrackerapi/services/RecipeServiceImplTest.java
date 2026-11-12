package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.repositories.RecipeRepository;

/**
 * Tests for {@link RecipeServiceImpl}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeServiceImplTest {

    @Mock
    private RecipeRepository recipeRepository;

    private RecipeServiceImpl service;

    private static Recipe recipe(Integer id, String title, Float rating) {
        return new Recipe(id, title, "Vegan", "A recipe", rating, 10, "45 min", "easy", "Mix.");
    }

    private static List<Recipe> recipes(int amount) {
        List<Recipe> result = new ArrayList<>();
        for (int i = 1; i <= amount; i++) {
            result.add(recipe(i, "Recipe " + i, 4.0F));
        }
        return result;
    }

    @BeforeEach
    void setUp() {
        service = new RecipeServiceImpl(recipeRepository);
    }

    @Nested
    @DisplayName("getNRandomRecipes")
    class RandomRecipes {

        @Test
        @DisplayName("gives the number of recipes that the caller asked for")
        void givesWantedAmount() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(recipes(20)));

            assertThat(service.getNRandomRecipes(1, 10)).hasSize(10);
        }

        @Test
        @DisplayName("gives all recipes when the user has fewer than the caller asked for")
        void givesAllRecipesWhenUserHasFewer() {
            // The method read ten recipes from a shorter list before and
            // stopped with an IndexOutOfBoundsException.
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(recipes(3)));

            assertThat(service.getNRandomRecipes(1, 10)).hasSize(3);
        }

        @Test
        @DisplayName("gives an empty list when the user has no recipe")
        void givesEmptyListWithoutRecipes() {
            when(recipeRepository.getAllRecipes(1))
                    .thenReturn(Optional.of(Collections.emptyList()));

            assertThat(service.getNRandomRecipes(1, 10)).isEmpty();
        }

        @Test
        @DisplayName("gives an empty list when the database gives no answer")
        void givesEmptyListWithoutAnswer() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.empty());

            assertThat(service.getNRandomRecipes(1, 10)).isEmpty();
        }

        @Test
        @DisplayName("gives an empty list for a number that is 0, negative or null")
        void givesEmptyListForEmptyAmount() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(recipes(5)));

            assertThat(service.getNRandomRecipes(1, 0)).isEmpty();
            assertThat(service.getNRandomRecipes(1, -1)).isEmpty();
            assertThat(service.getNRandomRecipes(1, null)).isEmpty();
        }

        @Test
        @DisplayName("gives each recipe one time only")
        void givesEachRecipeOneTime() {
            Recipe pizza = recipe(1, "Pizza", 4.0F);
            when(recipeRepository.getAllRecipes(1))
                    .thenReturn(Optional.of(Arrays.asList(pizza, recipe(1, "Pizza", 4.0F))));

            assertThat(service.getNRandomRecipes(1, 10)).hasSize(1);
        }

        @Test
        @DisplayName("keeps the list of the database unchanged")
        void keepsListOfDatabaseUnchanged() {
            List<Recipe> stored = new ArrayList<>(recipes(5));
            List<Recipe> order = new ArrayList<>(stored);
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(stored));

            service.getNRandomRecipes(1, 5);

            assertThat(stored).containsExactlyElementsOf(order);
        }
    }

    @Nested
    @DisplayName("getAllRecipesByRating")
    class RecipesByRating {

        @Test
        @DisplayName("gives the recipes that have a better mark")
        void givesBetterRecipes() {
            List<Recipe> stored = Arrays.asList(
                    recipe(1, "Good", 4.8F), recipe(2, "Bad", 2.0F));
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(stored));

            assertThat(service.getAllRecipesByRating(4.3F, 1))
                    .get().asList().extracting("title").containsExactly("Good");
        }

        @Test
        @DisplayName("gives a result and not null when the user has no recipe")
        void givesResultWithoutRecipes() {
            // The method gave Optional.of(null) before and stopped the caller
            // with a NullPointerException.
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.empty());

            assertThat(service.getAllRecipesByRating(4.3F, 1)).isPresent();
            assertThat(service.getAllRecipesByRating(4.3F, 1).get()).isEmpty();
        }

        @Test
        @DisplayName("accepts a recipe without a mark")
        void acceptsRecipeWithoutMark() {
            when(recipeRepository.getAllRecipes(1))
                    .thenReturn(Optional.of(Arrays.asList(recipe(1, "No mark", null))));

            assertThat(service.getAllRecipesByRating(4.3F, 1).get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("loadRecipesFromAPI")
    class LoadRecipes {

        @Test
        @DisplayName("deletes the old recipes and then stores the new recipes")
        void deletesOldRecipesAndStoresNewRecipes() {
            Recipe stored = recipe(99, "Old", 4.0F);
            when(recipeRepository.getAllRecipes(1))
                    .thenReturn(Optional.of(Collections.singletonList(stored)));
            when(recipeRepository.createRecipe(any(), eq(1))).thenReturn(42);

            Recipe newRecipe = recipe(null, "Pizza", 4.5F);
            newRecipe.setIngredients(new ArrayList<>(
                    Collections.singletonList(new Ingredient(null, "Flour", "200.0 G"))));

            assertThat(service.loadRecipesFromAPI(Collections.singletonList(newRecipe), 1))
                    .isEqualTo(1);

            InOrder order = Mockito.inOrder(recipeRepository);
            order.verify(recipeRepository).deleteRecipe(99, 1);
            order.verify(recipeRepository).createRecipe(newRecipe, 1);
            verify(recipeRepository).createIngredient("Flour", "200.0 G");
            verify(recipeRepository).createRecipeIngredient(eq(42), anyInt());
        }

        @Test
        @DisplayName("counts only the recipes that the database stored")
        void countsStoredRecipesOnly() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.empty());
            when(recipeRepository.createRecipe(any(), eq(1))).thenReturn(42, 0);

            List<Recipe> newRecipes = Arrays.asList(
                    recipe(null, "Pizza", 4.5F), recipe(null, "Pasta", 4.0F));

            assertThat(service.loadRecipesFromAPI(newRecipes, 1)).isEqualTo(1);
        }

        @Test
        @DisplayName("stores no ingredient for a recipe without ingredients")
        void storesNoIngredientWithoutIngredients() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.empty());
            when(recipeRepository.createRecipe(any(), eq(1))).thenReturn(42);
            Recipe newRecipe = recipe(null, "Pizza", 4.5F);
            newRecipe.setIngredients(null);

            assertThat(service.loadRecipesFromAPI(Collections.singletonList(newRecipe), 1))
                    .isEqualTo(1);

            verify(recipeRepository, never()).createIngredient(anyString(), anyString());
        }

        @Test
        @DisplayName("gives 0 for a list that is empty or null")
        void givesZeroForEmptyList() {
            assertThat(service.loadRecipesFromAPI(Collections.emptyList(), 1)).isZero();
            assertThat(service.loadRecipesFromAPI(null, 1)).isZero();

            verify(recipeRepository, never()).getAllRecipes(anyInt());
        }
    }

    @Nested
    @DisplayName("deleteRecipeById")
    class DeleteRecipe {

        @Test
        @DisplayName("deletes the links, then the ingredients and then the recipe")
        void deletesInCorrectOrder() {
            when(recipeRepository.deleteRecipe(42, 1)).thenReturn(1);

            assertThat(service.deleteRecipeById(42, 1)).isEqualTo(1);

            InOrder order = Mockito.inOrder(recipeRepository);
            order.verify(recipeRepository).deleteRecipeIngredient(42);
            order.verify(recipeRepository).deleteIngredient(42);
            order.verify(recipeRepository).deleteRecipe(42, 1);
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("Each operation gives the answer of the repository")
        void operationsGiveAnswerOfRepository() {
            Optional<Recipe> one = Optional.of(recipe(42, "Pizza", 4.5F));
            Optional<List<Recipe>> all = Optional.of(recipes(3));
            List<Recipe> matching = recipes(2);
            when(recipeRepository.getRecipeByType("A recipe")).thenReturn(one);
            when(recipeRepository.getRecipeById(42)).thenReturn(one);
            when(recipeRepository.getRecipeByRating(4.5D, 1)).thenReturn(one);
            when(recipeRepository.getAllRecipes(1)).thenReturn(all);
            when(recipeRepository.getMostPopularRecipe()).thenReturn(one);
            when(recipeRepository.getAllRecipesByRatingAndTypeAndIngredients(1))
                    .thenReturn(matching);
            when(recipeRepository.rateRecipe(42, 5)).thenReturn(1);

            assertThat(service.getRecipeByType("A recipe")).isSameAs(one);
            assertThat(service.getRecipeByID(42)).isSameAs(one);
            assertThat(service.getRecipeByRating(4.5D, 1)).isSameAs(one);
            assertThat(service.getAllRecipes(1)).isSameAs(all);
            assertThat(service.getMostPopularRecipe()).isSameAs(one);
            assertThat(service.getAllRecipesByMatchingUserInventory(1)).isSameAs(matching);
            assertThat(service.rateRecipe(42, 5)).isEqualTo(1);
        }

        @Test
        @DisplayName("The recipes of the user are read one time for a random selection")
        void readsRecipesOneTime() {
            when(recipeRepository.getAllRecipes(1)).thenReturn(Optional.of(recipes(20)));

            service.getNRandomRecipes(1, 10);

            verify(recipeRepository, times(1)).getAllRecipes(1);
        }
    }
}
