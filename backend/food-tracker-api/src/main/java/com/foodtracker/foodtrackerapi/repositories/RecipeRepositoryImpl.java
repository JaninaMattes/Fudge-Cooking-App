package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RecipeRepositoryImpl implements RecipeRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // Create Statement
    private static final String SQL_CREATE_RECIPE = "INSERT INTO ET_RECIPE (RECIPE_ID, USER_ID, TITLE, RECIPE_LABEL, RECIPE_DESCRIPTION, "+
            "CUMMULATIVERATING, AMOUNTOFRATINGS, COOKING_TIME, DIFFICULTY, INSTRUCTION, NUTRITION_VALUE, IMAGE_URL, FAVORITE_RECIPE) " +
            "VALUES (NEXTVAL('ET_RECIPE_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_INGREDIENT = "INSERT INTO ET_INGREDIENT (INGREDIENT_ID, INGREDIENT_NAME, QUANTITY) " +
            "VALUES (NEXTVAL('ET_INGREDIENT_SEQ'), ?, ?);";
    private static final String SQL_CREATE_RECIPE_INGREDIENT = "INSERT INTO ET_RECIPE_INGREDIENT (RECIPE_ID, INGREDIENT_ID) " + 
            "VALUES (?, ?);"; // does only contain ids of other tables as it is a composit table
    
    // Update Statement
    private static final String SQL_RATE_RECIPE = "UPDATE ET_RECIPE SET  CUMMULATIVERATING = CUMMULATIVERATING + ?, " +
            "AMOUNTOFRATINGS = AMOUNTOFRATINGS + 1 WHERE RECIPE_ID = ?;";

    // Delete Statement
    private static final String SQL_DELETE_ALL_RECIPES_BY_USER_ID = "DELETE FROM ET_RECIPE WHERE USER_ID = ?;";
    private static final String SQL_DELETE_RECIPE_BY_ID = "DELETE FROM ET_RECIPE WHERE RECIPE_ID = ? AND USER_ID = ?;";
    private static final String SQL_DELETE_INGREDIENT_BY_ID = "DELETE FROM ET_INGREDIENT WHERE ET_INGREDIENT.INGREDIENT_ID =" +
            "(SELECT INGREDIENT_ID FROM ET_RECIPE_INGREDIENT WHERE RECIPE_ID = ?);";
    private static final String SQL_DELETE_RECIPE_INGREDIENT_BY_ID = "DELETE FROM ET_RECIPE_INGREDIENT WHERE RECIPE_ID = ?;";

    // Select Statement
    private static final String SQL_GET_MOST_POPULAR_RECIPE = "SELECT *, (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) "+
            "AS RATING FROM ET_RECIPE ORDER BY RATING DESC LIMIT 1;";
    private static final String SQL_GET_FIVE_MOST_POPULAR_RECIPE = "SELECT *, (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) " +
            "AS RATING FROM ET_RECIPE WHERE USER_ID = ? ORDER BY RATING DESC LIMIT 5;";
    private static final String SQL_GET_N_MOST_POPULAR_RECIPES = "SELECT *, (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) " +
            "AS RATING FROM ET_RECIPE WHERE USER_ID = ? ORDER BY RATING DESC LIMIT ?;";
    private static final String SQL_GET_MOST_POPULAR_RECIPES_ON_INVENTARY = "SELECT *, (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) " +
            "AS RATING FROM ET_RECIPE "+
            "WHERE  ET_PRODUCT.INVENTORYLIST_ID = (SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?) " +
            "ORDER BY RATING DESC LIMIT 5;";
    private static final String SQL_GET_RECIPE_BY_ID = "SELECT * FROM ET_RECIPE WHERE RECIPE_ID = ?;";
    private static final String SQL_GET_RECIPE_BY_TYPE = "SELECT * FROM ET_RECIPE WHERE RECIPE_DESCRIPTION = ?;";
    private static final String SQL_GET_RECIPE_BY_RATING = "SELECT * FROM ET_RECIPE WHERE (ET_RECIPE.CUMMULATIVERATING/ET_RECIPE.AMOUNTOFRATINGS) = ? AND USER_ID = ?;";
    private static final String SQL_GET_RECIPE_BY_AMOUNT = "SELECT * FROM ET_RECIPE WHERE AMOUNT = ? AND USER_ID = ?;";
    private static final String SQL_GET_RATING_BY_ID = "SELECT (CUMMULATIVERATING / AMOUNTOFRATINGS) FROM ET_RECIPE WHERE RECIPE_ID = ?;";
    private static final String SQL_GET_ALL_RECIPES_BY_USERID = "SELECT * FROM ET_RECIPE WHERE USER_ID = ?;";
    // Note: Added SQL query to get all ingredients (Janina)
    private static final String SQL_GET_ALL_INGREDIENTS_BY_RECIPE_ID = "SELECT ET_RECIPE_INGREDIENT.INGREDIENT_ID, " +
            "ET_INGREDIENT.INGREDIENT_NAME, ET_INGREDIENT.QUANTITY " +
            "FROM ET_RECIPE_INGREDIENT " +
            "INNER JOIN ET_INGREDIENT " +
            "ON ET_RECIPE_INGREDIENT.INGREDIENT_ID = ET_INGREDIENT.INGREDIENT_ID " +
            "WHERE ET_RECIPE_INGREDIENT.RECIPE_ID = ?;";

    @Override
    public Integer createIngredient(String ingredientName, String quantity) {
        int ingredientId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_INGREDIENT, 
                Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, ingredientName);
                ps.setString(2, quantity);
                return ps;
            }, keyHolder);
            ingredientId = (Integer) keyHolder.getKeys().get("INGREDIENT_ID");

        } catch (Exception e) {
            e.getStackTrace();
        }
        return ingredientId;
    }

    @Override
    public Integer createRecipeIngredient(Integer recipeId, Integer ingredienId){
        int result = 0;
        try {
            result = jdbcTemplate.update(SQL_CREATE_RECIPE_INGREDIENT, recipeId, ingredienId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer createRecipe(Recipe recipe, Integer userId) {
        int recipeId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_RECIPE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, recipe.getTitle());
                ps.setString(3, recipe.getRecipeLabel());
                ps.setString(4, recipe.getRecipeDescription());
                ps.setFloat(5, recipe.getCummulativeRating());
                ps.setInt(6, recipe.getAmountOfRatings());
                ps.setString(7, recipe.getCookingTime());
                ps.setString(8, recipe.getDifficulty());
                ps.setString(9, recipe.getInstructions());
                ps.setString(10, recipe.getNutritionValue());
                ps.setString(11, recipe.getImageUrl());
                ps.setBoolean(12, false); //TODO rating
                return ps;
            }, keyHolder);
            recipeId = (Integer) keyHolder.getKeys().get("RECIPE_ID");

        } catch (Exception e) {
            e.getStackTrace();
        }
        return recipeId;
    }

    @Override
    public List<Recipe> getAllRecipesByRatingAndMatchWithInventory(int userId) {
        return jdbcTemplate.query(SQL_GET_MOST_POPULAR_RECIPES_ON_INVENTARY, recipeRowMapper, userId);
    }

    @Override
    public List<Ingredient> getAllIngredientsPerRecipe(Integer recipeId) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {

            ingredients = jdbcTemplate.query(SQL_GET_ALL_INGREDIENTS_BY_RECIPE_ID, 
                    ingredientRowMapper, recipeId);
        } catch (Exception e) {
            e.getStackTrace();
        }

        return ingredients;
    }

    @Override
    public Optional<List<Recipe>> getAllRecipes(Integer userId) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            recipes = jdbcTemplate.query(SQL_GET_ALL_RECIPES_BY_USERID, recipeRowMapper, userId);
            for (Recipe recipe : recipes) {
                recipe.setIngredients(new ArrayList<Ingredient>(getAllIngredientsPerRecipe(recipe.getRecipeId())));
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return Optional.of(recipes);
    }

    @Override
    public List<Recipe> getMostPopularRecipesByAmount(Integer amount, Integer userId) {
        return jdbcTemplate.query(SQL_GET_RECIPE_BY_AMOUNT, recipeRowMapper, amount, userId);
    }

    public List<Recipe> getFiveMostPopularRecipes() {
        return jdbcTemplate.query(SQL_GET_FIVE_MOST_POPULAR_RECIPE, recipeRowMapper);
    }

    @Override
    public Optional<Recipe> getMostPopularRecipe() {
        Recipe recipe = null;
        try {
            List<Recipe> recipes = jdbcTemplate.query(SQL_GET_MOST_POPULAR_RECIPE, recipeRowMapper);

        } catch (Exception e) {
            System.out.println("Error in getMostPopularRecipe");
        }
        return Optional.of(recipe);
    }

    @Override
    public Optional<Recipe> getRecipeById(int id) {
        try {
            List<Recipe> recipe = jdbcTemplate.query(SQL_GET_RECIPE_BY_ID, recipeRowMapper, id);
            return recipe.stream().findFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Optional<Recipe> getRecipeByType(String type) {

        try {
            List<Recipe> recipe = jdbcTemplate.query(SQL_GET_RECIPE_BY_TYPE, recipeRowMapper, type);

            return recipe.stream().findFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Recipe> getAllRecipesByRating(int rating, Integer userId) {
        List<Recipe> recipe = jdbcTemplate.query(SQL_GET_RECIPE_BY_RATING, recipeRowMapper, rating, userId);
        return recipe;
    }

    @Override
    public Optional<Recipe> getRecipeByRating(double rating, Integer userId) {
        try {
            List<Recipe> recipe = jdbcTemplate.query(SQL_GET_RECIPE_BY_RATING, recipeRowMapper, rating);
            return recipe.stream().findFirst();

        } catch (Exception e) {
            System.out.println("Error in getRecipeByRating");
        }
        return null;
    }

    @Override
    public Integer rateRecipe(int id, int rating) {
        Integer result = 0;
        try{
            result = jdbcTemplate.update(SQL_RATE_RECIPE, rating, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Recipe> getAllRecipesByRatingAndTypeAndIngredients(int userId) {

        /**
         * String params = "";
         * if(!Ingredients.isEmpty()){
         * for(Product p: Ingredients){
         * params += "WHERE INGREDIENTS = ? AND";
         * }
         * params.substring(0, params.length()-3);
         * String SQL = SQL_GET_RECIPE_BY_ID + params + ";";
         */
        return jdbcTemplate.query(SQL_GET_RECIPE_BY_TYPE, recipeRowMapper);
    }


    @Override
    public Integer deleteIngredient(Integer recipeId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_INGREDIENT_BY_ID, recipeId);
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteRecipeIngredient(Integer recipeId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_RECIPE_INGREDIENT_BY_ID, recipeId);
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteRecipe(Integer recipeId, Integer userId) {
        Integer result = 0;
        try {
            return jdbcTemplate.update(SQL_DELETE_RECIPE_BY_ID, recipeId, userId);
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    private RowMapper<Recipe> recipeRowMapper = (rs, rowNum) -> {
        return new Recipe(rs.getInt("RECIPE_ID"),
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
    };

    private RowMapper<Ingredient> ingredientRowMapper = (rs, rowNum) -> {
        return new Ingredient(rs.getInt("INGREDIENT_ID"),
                rs.getString("INGREDIENT_NAME"),
                rs.getString("QUANTITY"));
    };
}
