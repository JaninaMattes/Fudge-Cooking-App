package com.foodtracker.foodtrackerapi.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;

/**
 * <p>
 * Reads and writes the recipes of a user and their ingredients.
 * </p>
 *
 * <p>
 * The table ET_RECIPE holds the sum of all marks and the number of marks. The
 * SQL statements calculate the average mark, because the database can sort by
 * it.
 * </p>
 *
 * @author Janina Mattes
 */
@Repository
@Transactional
public class RecipeRepositoryImpl extends AbstractJdbcRepository implements RecipeRepository {

    /** Number of recipes that the dashboard shows. */
    private static final int POPULAR_RECIPE_LIMIT = 5;

    // Create statements
    private static final String SQL_CREATE_RECIPE = "INSERT INTO ET_RECIPE (RECIPE_ID, USER_ID, TITLE, "
            + "RECIPE_LABEL, RECIPE_DESCRIPTION, CUMMULATIVERATING, AMOUNTOFRATINGS, COOKING_TIME, "
            + "DIFFICULTY, INSTRUCTION, NUTRITION_VALUE, IMAGE_URL, FAVORITE_RECIPE) "
            + "VALUES (NEXTVAL('ET_RECIPE_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_INGREDIENT = "INSERT INTO ET_INGREDIENT (INGREDIENT_ID, "
            + "INGREDIENT_NAME, QUANTITY) VALUES (NEXTVAL('ET_INGREDIENT_SEQ'), ?, ?);";
    // Compound table. It holds foreign keys only.
    private static final String SQL_CREATE_RECIPE_INGREDIENT = "INSERT INTO ET_RECIPE_INGREDIENT "
            + "(RECIPE_ID, INGREDIENT_ID) VALUES (?, ?);";

    // Update statements
    private static final String SQL_RATE_RECIPE = "UPDATE ET_RECIPE SET "
            + "CUMMULATIVERATING = CUMMULATIVERATING + ?, AMOUNTOFRATINGS = AMOUNTOFRATINGS + 1 "
            + "WHERE RECIPE_ID = ?;";

    // Delete statements
    private static final String SQL_DELETE_RECIPE_BY_ID = "DELETE FROM ET_RECIPE "
            + "WHERE RECIPE_ID = ? AND USER_ID = ?;";
    private static final String SQL_DELETE_INGREDIENT_BY_ID = "DELETE FROM ET_INGREDIENT "
            + "WHERE ET_INGREDIENT.INGREDIENT_ID ="
            + "(SELECT INGREDIENT_ID FROM ET_RECIPE_INGREDIENT WHERE RECIPE_ID = ?);";
    private static final String SQL_DELETE_RECIPE_INGREDIENT_BY_ID = "DELETE FROM ET_RECIPE_INGREDIENT "
            + "WHERE RECIPE_ID = ?;";

    // Select statements
    private static final String SQL_GET_MOST_POPULAR_RECIPE = "SELECT *, "
            + "(ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) AS RATING FROM ET_RECIPE "
            + "ORDER BY RATING DESC LIMIT 1;";
    private static final String SQL_GET_N_MOST_POPULAR_RECIPES = "SELECT *, "
            + "(ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) AS RATING FROM ET_RECIPE "
            + "WHERE USER_ID = ? ORDER BY RATING DESC LIMIT ?;";
    private static final String SQL_GET_RECIPE_BY_ID = "SELECT * FROM ET_RECIPE WHERE RECIPE_ID = ?;";
    private static final String SQL_GET_RECIPE_BY_TYPE = "SELECT * FROM ET_RECIPE "
            + "WHERE RECIPE_DESCRIPTION = ?;";
    private static final String SQL_GET_RECIPE_BY_RATING = "SELECT * FROM ET_RECIPE "
            + "WHERE (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) = ? AND USER_ID = ?;";
    private static final String SQL_GET_ALL_RECIPES_BY_USERID = "SELECT * FROM ET_RECIPE WHERE USER_ID = ?;";
    private static final String SQL_GET_ALL_INGREDIENTS_BY_RECIPE_ID = "SELECT "
            + "ET_RECIPE_INGREDIENT.INGREDIENT_ID, ET_INGREDIENT.INGREDIENT_NAME, ET_INGREDIENT.QUANTITY "
            + "FROM ET_RECIPE_INGREDIENT "
            + "INNER JOIN ET_INGREDIENT "
            + "ON ET_RECIPE_INGREDIENT.INGREDIENT_ID = ET_INGREDIENT.INGREDIENT_ID "
            + "WHERE ET_RECIPE_INGREDIENT.RECIPE_ID = ?;";

    private final RowMapper<Recipe> recipeRowMapper = (rs, rowNum) -> new Recipe(
            rs.getInt("RECIPE_ID"),
            rs.getString("TITLE"),
            rs.getString("RECIPE_LABEL"),
            rs.getString("RECIPE_DESCRIPTION"),
            rs.getFloat("CUMMULATIVERATING"),
            rs.getInt("AMOUNTOFRATINGS"),
            rs.getString("COOKING_TIME"),
            rs.getString("DIFFICULTY"),
            rs.getString("INSTRUCTION"),
            rs.getString("NUTRITION_VALUE"),
            rs.getString("IMAGE_URL"),
            new ArrayList<>());

    private final RowMapper<Ingredient> ingredientRowMapper = (rs, rowNum) -> new Ingredient(
            rs.getInt("INGREDIENT_ID"),
            rs.getString("INGREDIENT_NAME"),
            rs.getString("QUANTITY"));

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate connection to the database
     */
    public RecipeRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Integer createIngredient(String ingredientName, String quantity) {
        return insertAndGetKey("create an ingredient", SQL_CREATE_INGREDIENT, "INGREDIENT_ID",
                statement -> {
                    statement.setString(1, ingredientName);
                    statement.setString(2, quantity);
                });
    }

    @Override
    public Integer createRecipeIngredient(Integer recipeId, Integer ingredienId) {
        return update("link the ingredient to the recipe", SQL_CREATE_RECIPE_INGREDIENT, recipeId,
                ingredienId);
    }

    @Override
    public Integer createRecipe(Recipe recipe, Integer userId) {
        return insertAndGetKey("create a recipe", SQL_CREATE_RECIPE, "RECIPE_ID", statement -> {
            statement.setInt(1, userId);
            statement.setString(2, recipe.getTitle());
            statement.setString(3, recipe.getRecipeLabel());
            statement.setString(4, recipe.getRecipeDescription());
            statement.setFloat(5, orZero(recipe.getCummulativeRating()));
            statement.setInt(6, orZero(recipe.getAmountOfRatings()));
            statement.setString(7, recipe.getCookingTime());
            statement.setString(8, recipe.getDifficulty());
            statement.setString(9, recipe.getInstructions());
            statement.setString(10, recipe.getNutritionValue());
            statement.setString(11, recipe.getImageUrl());
            // The user marks a recipe as a favorite later.
            statement.setBoolean(12, false);
        });
    }

    @Override
    public List<Ingredient> getAllIngredientsPerRecipe(Integer recipeId) {
        return Attempt.orDefault("read the ingredients of the recipe", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_ALL_INGREDIENTS_BY_RECIPE_ID, ingredientRowMapper,
                        recipeId));
    }

    @Override
    public Optional<List<Recipe>> getAllRecipes(Integer userId) {
        List<Recipe> recipes = Attempt.orDefault("read all recipes of the user", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_ALL_RECIPES_BY_USERID, recipeRowMapper, userId));
        for (Recipe recipe : recipes) {
            recipe.setIngredients(new ArrayList<>(getAllIngredientsPerRecipe(recipe.getRecipeId())));
        }
        return Optional.of(recipes);
    }

    @Override
    public List<Recipe> getMostPopularRecipesByAmount(Integer amount, Integer userId) {
        return Attempt.orDefault("read the most popular recipes", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_N_MOST_POPULAR_RECIPES, recipeRowMapper, userId,
                        amount));
    }

    /**
     * Reads the five recipes of a user that have the best mark.
     *
     * @param userId key of the user
     * @return the recipes. The list is empty if the user has no recipe.
     */
    public List<Recipe> getFiveMostPopularRecipes(Integer userId) {
        return getMostPopularRecipesByAmount(POPULAR_RECIPE_LIMIT, userId);
    }

    @Override
    public Optional<Recipe> getMostPopularRecipe() {
        List<Recipe> recipes = Attempt.orDefault("read the most popular recipe", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_MOST_POPULAR_RECIPE, recipeRowMapper));
        return recipes.stream().findFirst();
    }

    @Override
    public Optional<Recipe> getRecipeById(int id) {
        List<Recipe> recipes = Attempt.orDefault("read a recipe by its key", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_RECIPE_BY_ID, recipeRowMapper, id));
        return recipes.stream().findFirst();
    }

    @Override
    public Optional<Recipe> getRecipeByType(String type) {
        List<Recipe> recipes = Attempt.orDefault("read a recipe by its type", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_RECIPE_BY_TYPE, recipeRowMapper, type));
        return recipes.stream().findFirst();
    }

    @Override
    public List<Recipe> getAllRecipesByRating(int rating, Integer userId) {
        return Attempt.orDefault("read the recipes with this mark", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_RECIPE_BY_RATING, recipeRowMapper, rating, userId));
    }

    @Override
    public Optional<Recipe> getRecipeByRating(double rating, Integer userId) {
        List<Recipe> recipes = Attempt.orDefault("read a recipe by its mark", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_RECIPE_BY_RATING, recipeRowMapper, rating, userId));
        return recipes.stream().findFirst();
    }

    @Override
    public Integer rateRecipe(int id, int rating) {
        return update("give a mark to the recipe", SQL_RATE_RECIPE, rating, id);
    }

    @Override
    public List<Recipe> getAllRecipesByRatingAndTypeAndIngredients(int userId) {
        // The database cannot yet compare the ingredients of a recipe with the
        // products of the user. The method gives the recipes with the best mark.
        return getMostPopularRecipesByAmount(POPULAR_RECIPE_LIMIT, userId);
    }

    @Override
    public List<Recipe> getAllRecipesByRatingAndMatchWithInventory(int userId) {
        return getAllRecipesByRatingAndTypeAndIngredients(userId);
    }

    @Override
    public Integer deleteIngredient(Integer recipeId) {
        return update("delete the ingredients of the recipe", SQL_DELETE_INGREDIENT_BY_ID, recipeId);
    }

    @Override
    public Integer deleteRecipeIngredient(Integer recipeId) {
        return update("delete the links to the ingredients", SQL_DELETE_RECIPE_INGREDIENT_BY_ID,
                recipeId);
    }

    @Override
    public Integer deleteRecipe(Integer recipeId, Integer userId) {
        return update("delete a recipe", SQL_DELETE_RECIPE_BY_ID, recipeId, userId);
    }

    private static float orZero(Float value) {
        return value != null ? value : 0.0F;
    }

    private static int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
