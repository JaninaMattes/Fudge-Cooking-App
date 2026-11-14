package com.foodtracker.foodtrackerapi.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Tests for the model classes. Lombok writes the methods {@code equals},
 * {@code hashCode}, {@code toString} and the access methods. The tests prove
 * that the result fits the application: two objects with the same values are
 * equal, and the short constructors fill the other fields with a useful value.
 * </p>
 *
 * @author Janina Mattes
 */
class ModelsTest {

    @Nested
    @DisplayName("Tag")
    class TagTest {

        @Test
        @DisplayName("Two tags with the same values are equal")
        void equalValuesAreEqual() {
            Tag first = new Tag(1, "Vegan");
            Tag second = new Tag(1, "Vegan");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
            assertThat(first).isNotEqualTo(new Tag(2, "Vegan"));
            assertThat(first).isNotEqualTo(null);
            assertThat(first).isNotEqualTo("Vegan");
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            Tag tag = new Tag();
            tag.setTagId(5);
            tag.setLabel("Lactose");

            assertThat(tag.getTagId()).isEqualTo(5);
            assertThat(tag.getLabel()).isEqualTo("Lactose");
            assertThat(tag.toString()).contains("Lactose");
        }
    }

    @Nested
    @DisplayName("Ingredient")
    class IngredientTest {

        @Test
        @DisplayName("Two ingredients with the same values are equal")
        void equalValuesAreEqual() {
            Ingredient first = new Ingredient(1, "Flour", "200.0 G");
            Ingredient second = new Ingredient(1, "Flour", "200.0 G");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
            assertThat(first.toString()).contains("Flour");
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            Ingredient ingredient = new Ingredient();
            ingredient.setIngredientId(2);
            ingredient.setIngredientName("Sugar");
            ingredient.setQuantity("1 Tbsp");

            assertThat(ingredient.getIngredientId()).isEqualTo(2);
            assertThat(ingredient.getIngredientName()).isEqualTo("Sugar");
            assertThat(ingredient.getQuantity()).isEqualTo("1 Tbsp");
        }
    }

    @Nested
    @DisplayName("Image")
    class ImageTest {

        @Test
        @DisplayName("The short constructor with two values leaves the other fields empty")
        void twoValueConstructorLeavesOtherFieldsEmpty() {
            Image image = new Image(1, "Profilbild");

            assertThat(image.getImageId()).isEqualTo(1);
            assertThat(image.getImageName()).isEqualTo("Profilbild");
            assertThat(image.getImageUrl()).isNull();
            assertThat(image.getBase64ImageString()).isNull();
        }

        @Test
        @DisplayName("The short constructor with three values leaves the image data empty")
        void threeValueConstructorLeavesDataEmpty() {
            Image image = new Image(1, "Profilbild", "http://example.org/a.png");

            assertThat(image.getImageUrl()).isEqualTo("http://example.org/a.png");
            assertThat(image.getBase64ImageString()).isNull();
        }

        @Test
        @DisplayName("Two images with the same values are equal")
        void equalValuesAreEqual() {
            Image first = new Image(1, "A", "url", "data");
            Image second = new Image(1, "A", "url", "data");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            Image image = new Image();
            image.setImageId(9);
            image.setImageName("Product image");
            image.setImageUrl("url");
            image.setBase64ImageString("data");

            assertThat(image.getImageId()).isEqualTo(9);
            assertThat(image.getImageName()).isEqualTo("Product image");
            assertThat(image.getImageUrl()).isEqualTo("url");
            assertThat(image.getBase64ImageString()).isEqualTo("data");
        }
    }

    @Nested
    @DisplayName("Settings")
    class SettingsTest {

        @Test
        @DisplayName("A new user has no permission")
        void newUserHasNoPermission() {
            Settings settings = new Settings(1);

            assertThat(settings.getSettingsId()).isEqualTo(1);
            assertThat(settings.getAllowPushNotifications()).isFalse();
            assertThat(settings.getRemindBeforeProductExpiration()).isFalse();
            assertThat(settings.getSuggestProductsForShoppingList()).isFalse();
        }

        @Test
        @DisplayName("The access methods read and write the permissions")
        void accessMethodsWork() {
            Settings settings = new Settings(1, true, true, true);
            settings.setAllowPushNotifications(false);

            assertThat(settings.getAllowPushNotifications()).isFalse();
            assertThat(settings.getRemindBeforeProductExpiration()).isTrue();
            assertThat(settings.getSuggestProductsForShoppingList()).isTrue();
            assertThat(settings).isEqualTo(new Settings(1, false, true, true));
        }
    }

    @Nested
    @DisplayName("User")
    class UserTest {

        @Test
        @DisplayName("The short constructor gives an empty list of preferences")
        void shortConstructorGivesEmptyPreferences() {
            User user = new User(1, "Janina", "Mattes", "a@b.c", "hash");

            assertThat(user.getDietaryPreferences()).isEmpty();
            assertThat(user.getUserImage()).isNull();
            assertThat(user.getUserSettings()).isNull();
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            User user = new User();
            user.setUserId(1);
            user.setFirstName("Janina");
            user.setLastName("Mattes");
            user.setEmail("a@b.c");
            user.setPassword("hash");
            user.setUserImage(new Image(1, "Profilbild"));
            user.setUserSettings(new Settings(1));
            user.setDietaryPreferences(new ArrayList<>(
                    Collections.singletonList(new Tag(1, "Vegan"))));

            assertThat(user.getUserId()).isEqualTo(1);
            assertThat(user.getFirstName()).isEqualTo("Janina");
            assertThat(user.getLastName()).isEqualTo("Mattes");
            assertThat(user.getEmail()).isEqualTo("a@b.c");
            assertThat(user.getPassword()).isEqualTo("hash");
            assertThat(user.getUserImage()).isNotNull();
            assertThat(user.getUserSettings()).isNotNull();
            assertThat(user.getDietaryPreferences()).hasSize(1);
            assertThat(user.toString()).contains("Janina");
        }

        @Test
        @DisplayName("Two users with the same values are equal")
        void equalValuesAreEqual() {
            User first = new User(1, "A", "B", "a@b.c", "hash");
            User second = new User(1, "A", "B", "a@b.c", "hash");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }
    }

    @Nested
    @DisplayName("Product")
    class ProductTest {

        @Test
        @DisplayName("The short constructor gives no image and an empty list of tags")
        void shortConstructorGivesEmptyTags() {
            Product product = new Product(1, "Milk", Date.valueOf("2022-02-02"), "1 Package",
                    "Dairy", "250 kcal");

            assertThat(product.getProductImage()).isNull();
            assertThat(product.getProductTags()).isEmpty();
        }

        @Test
        @DisplayName("The constructor with tags gives no image")
        void constructorWithTagsGivesNoImage() {
            ArrayList<Tag> tags = new ArrayList<>(Collections.singletonList(new Tag(1, "Vegan")));

            Product product = new Product(1, "Milk", null, "1", "Dairy", "250 kcal", tags);

            assertThat(product.getProductTags()).hasSize(1);
            assertThat(product.getProductImage()).isNull();
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            Product product = new Product();
            product.setProductId(1);
            product.setProductName("Milk");
            product.setExpirationDate(Date.valueOf("2022-02-02"));
            product.setQuantity("1 Package");
            product.setManufacturer("Dairy");
            product.setNutritionValue("250 kcal");
            product.setProductImage(new Image(1, "Milk"));
            product.setProductTags(new ArrayList<>());

            assertThat(product.getProductId()).isEqualTo(1);
            assertThat(product.getProductName()).isEqualTo("Milk");
            assertThat(product.getExpirationDate()).isEqualTo(Date.valueOf("2022-02-02"));
            assertThat(product.getQuantity()).isEqualTo("1 Package");
            assertThat(product.getManufacturer()).isEqualTo("Dairy");
            assertThat(product.getNutritionValue()).isEqualTo("250 kcal");
            assertThat(product.getProductImage()).isNotNull();
            assertThat(product.toString()).contains("Milk");
        }

        @Test
        @DisplayName("Two products with the same values are equal")
        void equalValuesAreEqual() {
            Product first = new Product(1, "Milk", null, "1", "Dairy", "250 kcal");
            Product second = new Product(1, "Milk", null, "1", "Dairy", "250 kcal");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }
    }

    @Nested
    @DisplayName("Recipe")
    class RecipeTest {

        @Test
        @DisplayName("The short constructor gives an empty list of ingredients")
        void shortConstructorGivesEmptyIngredients() {
            Recipe recipe = new Recipe(1, "Pizza", "Vegan", "A pizza", 4.5F, 10, "45 min", "easy",
                    "Mix all parts.");

            assertThat(recipe.getIngredients()).isEmpty();
            assertThat(recipe.getImageUrl()).isNull();
            assertThat(recipe.getNutritionValue()).isNull();
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            Recipe recipe = new Recipe();
            recipe.setRecipeId(1);
            recipe.setTitle("Pizza");
            recipe.setRecipeLabel("Vegan");
            recipe.setRecipeDescription("A pizza");
            recipe.setCummulativeRating(4.5F);
            recipe.setAmountOfRatings(10);
            recipe.setCookingTime("45 min");
            recipe.setDifficulty("easy");
            recipe.setInstructions("Mix all parts.");
            recipe.setNutritionValue("560 kcal");
            recipe.setImageUrl("http://example.org/pizza.png");
            recipe.setIngredients(new ArrayList<>(
                    Collections.singletonList(new Ingredient(1, "Flour", "200.0 G"))));

            assertThat(recipe.getRecipeId()).isEqualTo(1);
            assertThat(recipe.getTitle()).isEqualTo("Pizza");
            assertThat(recipe.getRecipeLabel()).isEqualTo("Vegan");
            assertThat(recipe.getRecipeDescription()).isEqualTo("A pizza");
            assertThat(recipe.getCummulativeRating()).isEqualTo(4.5F);
            assertThat(recipe.getAmountOfRatings()).isEqualTo(10);
            assertThat(recipe.getCookingTime()).isEqualTo("45 min");
            assertThat(recipe.getDifficulty()).isEqualTo("easy");
            assertThat(recipe.getInstructions()).isEqualTo("Mix all parts.");
            assertThat(recipe.getNutritionValue()).isEqualTo("560 kcal");
            assertThat(recipe.getImageUrl()).isEqualTo("http://example.org/pizza.png");
            assertThat(recipe.getIngredients()).hasSize(1);
            assertThat(recipe.toString()).contains("Pizza");
        }

        @Test
        @DisplayName("Two recipes with the same values are equal")
        void equalValuesAreEqual() {
            Recipe first = new Recipe(1, "Pizza", "Vegan", "A pizza", 4.5F, 10, "45 min", "easy",
                    "Mix.");
            Recipe second = new Recipe(1, "Pizza", "Vegan", "A pizza", 4.5F, 10, "45 min", "easy",
                    "Mix.");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("Equal recipes count as one in a list without duplicates")
        void equalRecipesAreOneValue() {
            Recipe first = new Recipe(1, "Pizza", null, null, 4.5F, 10, null, null, null);
            Recipe second = new Recipe(1, "Pizza", null, null, 4.5F, 10, null, null, null);

            assertThat(Arrays.asList(first, second).stream().distinct().count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("BarcodeProduct")
    class BarcodeProductTest {

        @Test
        @DisplayName("empty gives a product in which all values are an empty text")
        void emptyGivesEmptyValues() {
            BarcodeProduct product = BarcodeProduct.empty();

            assertThat(product.getFoodId()).isEmpty();
            assertThat(product.getLabel()).isEmpty();
            assertThat(product.getNutrients()).isEmpty();
            assertThat(product.getBrand()).isEmpty();
            assertThat(product.getCategory()).isEmpty();
            assertThat(product.getCategoryLabel()).isEmpty();
        }

        @Test
        @DisplayName("The access methods read and write the values")
        void accessMethodsWork() {
            BarcodeProduct product = new BarcodeProduct();
            product.setFoodId("food_1");
            product.setLabel("Milk");
            product.setNutrients("250");
            product.setBrand("Dairy");
            product.setCategory("Generic foods");
            product.setCategoryLabel("food");

            assertThat(product.getFoodId()).isEqualTo("food_1");
            assertThat(product.getLabel()).isEqualTo("Milk");
            assertThat(product.getNutrients()).isEqualTo("250");
            assertThat(product.getBrand()).isEqualTo("Dairy");
            assertThat(product.getCategory()).isEqualTo("Generic foods");
            assertThat(product.getCategoryLabel()).isEqualTo("food");
            assertThat(product.toString()).contains("Milk");
            assertThat(product).isEqualTo(new BarcodeProduct("food_1", "Milk", "250", "Dairy",
                    "Generic foods", "food"));
        }
    }
}
