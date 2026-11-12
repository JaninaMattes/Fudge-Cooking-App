package com.foodtracker.foodtrackerapi.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * Tests for {@link RecipeRepositoryImpl}.
 *
 * @author Janina Mattes
 */
class RecipeRepositoryImplTest {

    private RecordingJdbcTemplate jdbc;
    private RecipeRepositoryImpl repository;

    private static Recipe recipe(Integer id, String title) {
        return new Recipe(id, title, "Vegan", "A recipe", 4.5F, 10, "45 min", "easy", "Mix.",
                "560 kcal", "http://example.org/a.png", new ArrayList<>());
    }

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        repository = new RecipeRepositoryImpl(jdbc);
    }

    @Nested
    @DisplayName("The write operations")
    class WriteOperations {

        @Test
        @DisplayName("createRecipe writes the values in the order of the statement")
        void createRecipeWritesValues() throws Exception {
            jdbc.givesKey("RECIPE_ID", 42);

            assertThat(repository.createRecipe(recipe(null, "Pizza"), 1)).isEqualTo(42);

            verify(jdbc.statement()).setInt(1, 1);
            verify(jdbc.statement()).setString(2, "Pizza");
            verify(jdbc.statement()).setString(3, "Vegan");
            verify(jdbc.statement()).setString(4, "A recipe");
            verify(jdbc.statement()).setFloat(5, 4.5F);
            verify(jdbc.statement()).setInt(6, 10);
            verify(jdbc.statement()).setString(7, "45 min");
            verify(jdbc.statement()).setString(8, "easy");
            verify(jdbc.statement()).setString(9, "Mix.");
            verify(jdbc.statement()).setString(10, "560 kcal");
            verify(jdbc.statement()).setString(11, "http://example.org/a.png");
            verify(jdbc.statement()).setBoolean(12, false);
        }

        @Test
        @DisplayName("createRecipe accepts a recipe without a mark")
        void createRecipeAcceptsRecipeWithoutRating() throws Exception {
            jdbc.givesKey("RECIPE_ID", 42);
            Recipe recipe = new Recipe();
            recipe.setTitle("Pizza");

            assertThat(repository.createRecipe(recipe, 1)).isEqualTo(42);
            verify(jdbc.statement()).setFloat(5, 0.0F);
            verify(jdbc.statement()).setInt(6, 0);
        }

        @Test
        @DisplayName("createIngredient gives the new key")
        void createIngredientGivesNewKey() throws Exception {
            jdbc.givesKey("INGREDIENT_ID", 3);

            assertThat(repository.createIngredient("Flour", "200.0 G")).isEqualTo(3);
            verify(jdbc.statement()).setString(1, "Flour");
            verify(jdbc.statement()).setString(2, "200.0 G");
        }

        @Test
        @DisplayName("createRecipeIngredient writes the two foreign keys")
        void createRecipeIngredientWritesForeignKeys() {
            assertThat(repository.createRecipeIngredient(42, 3)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(42, 3);
            assertThat(jdbc.lastSql()).contains("ET_RECIPE_INGREDIENT");
        }

        @Test
        @DisplayName("rateRecipe adds the mark and counts one more mark")
        void rateRecipeAddsMark() {
            assertThat(repository.rateRecipe(42, 5)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("CUMMULATIVERATING = CUMMULATIVERATING + ?");
            assertThat(jdbc.lastSql()).contains("AMOUNTOFRATINGS = AMOUNTOFRATINGS + 1");
            assertThat(jdbc.lastArgs()).containsExactly(5, 42);
        }

        @Test
        @DisplayName("deleteRecipe uses the key of the recipe and of the user")
        void deleteRecipeUsesBothKeys() {
            assertThat(repository.deleteRecipe(42, 1)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(42, 1);
        }

        @Test
        @DisplayName("deleteIngredient and deleteRecipeIngredient use the key of the recipe")
        void deleteIngredientsUseRecipeKey() {
            assertThat(repository.deleteIngredient(42)).isEqualTo(1);
            assertThat(jdbc.lastArgs()).containsExactly(42);

            assertThat(repository.deleteRecipeIngredient(42)).isEqualTo(1);
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("Each write operation gives 0 when the database is not available")
        void writeOperationsGiveZeroOnError() {
            jdbc.fails();

            assertThat(repository.createRecipe(recipe(null, "Pizza"), 1)).isZero();
            assertThat(repository.createIngredient("Flour", "200.0 G")).isZero();
            assertThat(repository.createRecipeIngredient(42, 3)).isZero();
            assertThat(repository.rateRecipe(42, 5)).isZero();
            assertThat(repository.deleteRecipe(42, 1)).isZero();
            assertThat(repository.deleteIngredient(42)).isZero();
            assertThat(repository.deleteRecipeIngredient(42)).isZero();
        }
    }

    @Nested
    @DisplayName("getMostPopularRecipe")
    class MostPopularRecipe {

        @Test
        @DisplayName("gives the first recipe of the answer")
        void givesFirstRecipe() {
            // The method dropped the answer of the database and then always
            // gave Optional.of(null), which stopped the caller.
            jdbc.givesRows(Arrays.asList(recipe(42, "Pizza"), recipe(43, "Pasta")));

            assertThat(repository.getMostPopularRecipe())
                    .map(Recipe::getTitle).contains("Pizza");
        }

        @Test
        @DisplayName("sorts by the average mark")
        void sortsByAverageMark() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            repository.getMostPopularRecipe();

            assertThat(jdbc.lastSql()).contains("ORDER BY RATING DESC LIMIT 1");
        }

        @Test
        @DisplayName("gives an empty result when there is no recipe")
        void givesEmptyResultWithoutRecipe() {
            jdbc.givesRows(Collections.emptyList());

            assertThat(repository.getMostPopularRecipe()).isEmpty();
        }

        @Test
        @DisplayName("gives an empty result when the database is not available")
        void givesEmptyResultOnError() {
            jdbc.fails();

            assertThat(repository.getMostPopularRecipe()).isEmpty();
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("getAllRecipes reads the ingredients of each recipe")
        void allRecipesReadsIngredients() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            List<Recipe> recipes = repository.getAllRecipes(1).orElseThrow(AssertionError::new);

            assertThat(recipes).hasSize(1);
            // The first call reads the recipes, the second the ingredients.
            assertThat(jdbc.calls()).hasSize(2);
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("getAllRecipes gives an empty list when the database is not available")
        void allRecipesGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllRecipes(1)).get().asList().isEmpty();
        }

        @Test
        @DisplayName("getAllIngredientsPerRecipe gives an empty list when the database fails")
        void ingredientsGiveEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllIngredientsPerRecipe(42)).isEmpty();
        }

        @Test
        @DisplayName("getRecipeById gives the recipe")
        void recipeByIdGivesRecipe() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getRecipeById(42)).map(Recipe::getTitle).contains("Pizza");
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("getRecipeById gives an empty result and not null when the database fails")
        void recipeByIdGivesEmptyResultOnError() {
            // The method gave null before, which stopped the caller.
            jdbc.fails();

            assertThat(repository.getRecipeById(42)).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("getRecipeByType gives an empty result and not null when the database fails")
        void recipeByTypeGivesEmptyResultOnError() {
            jdbc.fails();

            assertThat(repository.getRecipeByType("A recipe")).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("getRecipeByType searches in the description")
        void recipeByTypeSearchesDescription() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getRecipeByType("A recipe")).isPresent();
            assertThat(jdbc.lastSql()).contains("RECIPE_DESCRIPTION = ?");
            assertThat(jdbc.lastArgs()).containsExactly("A recipe");
        }

        @Test
        @DisplayName("getRecipeByRating sends the mark and the user")
        void recipeByRatingSendsBothValues() {
            // The method sent one value to a statement with two placeholders
            // before, so the read always failed.
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getRecipeByRating(4.5D, 1)).isPresent();
            assertThat(jdbc.lastArgs()).containsExactly(4.5D, 1);
        }

        @Test
        @DisplayName("getRecipeByRating gives an empty result when the database fails")
        void recipeByRatingGivesEmptyResultOnError() {
            jdbc.fails();

            assertThat(repository.getRecipeByRating(4.5D, 1)).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("getAllRecipesByRating sends the mark and the user")
        void allRecipesByRatingSendsBothValues() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getAllRecipesByRating(4, 1)).hasSize(1);
            assertThat(jdbc.lastArgs()).containsExactly(4, 1);
        }

        @Test
        @DisplayName("getAllRecipesByRating gives an empty list when the database fails")
        void allRecipesByRatingGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllRecipesByRating(4, 1)).isEmpty();
        }

        @Test
        @DisplayName("getMostPopularRecipesByAmount sends the user and the number")
        void mostPopularByAmountSendsUserAndNumber() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getMostPopularRecipesByAmount(5, 1)).hasSize(1);
            assertThat(jdbc.lastSql()).contains("LIMIT ?");
            assertThat(jdbc.lastArgs()).containsExactly(1, 5);
        }

        @Test
        @DisplayName("The search by inventory gives the recipes with the best mark")
        void searchByInventoryGivesBestRecipes() {
            // The statement had a placeholder but received no value before, so
            // the read always failed.
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getAllRecipesByRatingAndTypeAndIngredients(1)).hasSize(1);
            assertThat(jdbc.lastArgs()).containsExactly(1, 5);

            assertThat(repository.getAllRecipesByRatingAndMatchWithInventory(1)).hasSize(1);
            assertThat(jdbc.lastArgs()).containsExactly(1, 5);
        }

        @Test
        @DisplayName("getFiveMostPopularRecipes asks for five recipes")
        void fiveMostPopularAsksForFive() {
            jdbc.givesRows(Collections.singletonList(recipe(42, "Pizza")));

            assertThat(repository.getFiveMostPopularRecipes(1)).hasSize(1);
            assertThat(jdbc.lastArgs()).containsExactly(1, 5);
        }
    }

    @Nested
    @DisplayName("The row mappers")
    class RowMappers {

        @Test
        @DisplayName("The mapper of the recipe reads all columns")
        void recipeMapperReadsAllColumns() throws Exception {
            jdbc.givesRows(new ArrayList<Recipe>());
            repository.getRecipeById(42);
            Map<String, Object> row = new HashMap<>();
            row.put("RECIPE_ID", 42);
            row.put("TITLE", "Pizza");
            row.put("RECIPE_LABEL", "Vegan");
            row.put("RECIPE_DESCRIPTION", "A recipe");
            row.put("CUMMULATIVERATING", 4.5F);
            row.put("AMOUNTOFRATINGS", 10);
            row.put("COOKING_TIME", "45 min");
            row.put("DIFFICULTY", "easy");
            row.put("INSTRUCTION", "Mix.");
            row.put("NUTRITION_VALUE", "560 kcal");
            row.put("IMAGE_URL", "http://example.org/a.png");

            Recipe mapped = jdbc.mapRow(row);

            assertThat(mapped).isEqualTo(recipe(42, "Pizza"));
        }

        @Test
        @DisplayName("The mapper of the ingredient reads all columns")
        void ingredientMapperReadsAllColumns() throws Exception {
            jdbc.givesRows(new ArrayList<Ingredient>());
            repository.getAllIngredientsPerRecipe(42);
            Map<String, Object> row = new HashMap<>();
            row.put("INGREDIENT_ID", 3);
            row.put("INGREDIENT_NAME", "Flour");
            row.put("QUANTITY", "200.0 G");

            Ingredient mapped = jdbc.mapRow(row);

            assertThat(mapped).isEqualTo(new Ingredient(3, "Flour", "200.0 G"));
        }
    }
}
