package com.foodtracker.foodtrackerapi.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * Tests for {@link ShoppingListRepositoryImpl}.
 *
 * @author Janina Mattes
 */
class ShoppingListRepositoryImplTest {

    private RecordingJdbcTemplate jdbc;
    private InventoryRepository productRepository;
    private ShoppingListRepositoryImpl repository;

    private static Product product(Integer id, String name) {
        return new Product(id, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        productRepository = mock(InventoryRepository.class);
        when(productRepository.getProductImage(anyInt())).thenReturn(Optional.empty());
        when(productRepository.getProductTags(anyInt())).thenReturn(Optional.of(new ArrayList<>()));
        repository = new ShoppingListRepositoryImpl(jdbc, productRepository);
    }

    @Nested
    @DisplayName("createProductInCurrentShoppingList")
    class CreateProduct {

        @Test
        @DisplayName("writes the key of the current list and leaves the other lists empty")
        void writesCurrentListKeyOnly() throws Exception {
            jdbc.givesObject(11).givesKey("PRODUCT_ID", 42);

            assertThat(repository.createProductInCurrentShoppingList(1, product(null, "Milk")))
                    .isEqualTo(42);

            verify(jdbc.statement()).setInt(1, 11);
            verify(jdbc.statement()).setNull(2, Types.INTEGER);
            verify(jdbc.statement()).setNull(3, Types.INTEGER);
            verify(jdbc.statement()).setString(4, "Milk");
            verify(jdbc.statement()).setDate(5, Date.valueOf("2022-02-02"));
            verify(jdbc.statement()).setString(6, "1 Package");
            verify(jdbc.statement()).setString(7, "Dairy");
            verify(jdbc.statement()).setString(8, "250 kcal");
        }

        @Test
        @DisplayName("accepts a product without an expiration date")
        void acceptsProductWithoutDate() throws Exception {
            jdbc.givesObject(11).givesKey("PRODUCT_ID", 42);
            Product product = new Product(null, "Milk", null, "1", "Dairy", "250 kcal");

            repository.createProductInCurrentShoppingList(1, product);

            verify(jdbc.statement()).setDate(5, null);
        }

        @Test
        @DisplayName("gives the key 0 when the database is not available")
        void givesZeroOnError() {
            jdbc.fails();

            assertThat(repository.createProductInCurrentShoppingList(1, product(null, "Milk")))
                    .isZero();
        }
    }

    @Nested
    @DisplayName("The move operations")
    class MoveOperations {

        @Test
        @DisplayName("A product that moves to the current list leaves the old list")
        void moveToCurrentListClearsOldList() {
            jdbc.givesObject(11);

            assertThat(repository.addProductInCurShoppingList(1, 42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("CUR_SHOPPINGLIST_ID = ?");
            assertThat(jdbc.lastSql()).contains("OLD_SHOPPINGLIST_ID = NULL");
            assertThat(jdbc.lastArgs()).containsExactly(11, 42);
        }

        @Test
        @DisplayName("A product that moves to the old list gets the default amount")
        void moveToOldListSetsDefaultQuantity() {
            jdbc.givesObject(13);

            assertThat(repository.addProductInOldShoppingList(1, 42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("OLD_SHOPPINGLIST_ID = ?");
            assertThat(jdbc.lastSql()).contains("CUR_SHOPPINGLIST_ID = NULL");
            assertThat(jdbc.lastArgs()).containsExactly(13, "1 Package", 42);
        }

        @Test
        @DisplayName("A move gives 0 when the user has no such list")
        void moveGivesZeroWithoutList() {
            jdbc.givesObject(0);

            assertThat(repository.addProductInCurShoppingList(1, 42)).isZero();
            assertThat(repository.addProductInOldShoppingList(1, 42)).isZero();
        }

        @Test
        @DisplayName("removeProductFromCurShoppingList only clears the foreign key")
        void removeFromCurrentListClearsForeignKey() {
            assertThat(repository.removeProductFromCurShoppingList(42, 11)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("CUR_SHOPPINGLIST_ID = NULL");
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("removeProductFromOldShoppingList only clears the foreign key")
        void removeFromOldListClearsForeignKey() {
            assertThat(repository.removeProductFromOldShoppingList(42, 13)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("OLD_SHOPPINGLIST_ID = NULL");
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("A remove gives 0 when the user has no such list")
        void removeGivesZeroWithoutList() {
            assertThat(repository.removeProductFromCurShoppingList(42, 0)).isZero();
            assertThat(repository.removeProductFromOldShoppingList(42, 0)).isZero();
            assertThat(jdbc.calls()).isEmpty();
        }
    }

    @Nested
    @DisplayName("The delete operations")
    class DeleteOperations {

        @Test
        @DisplayName("The delete of the current list uses the user and then the product")
        void deleteInCurrentListUsesCorrectOrder() {
            // The statement named the column RECIPE_ID before, which the table
            // ET_PRODUCT does not hold.
            assertThat(repository.deleteProductInCurrentShoppingList(1, 42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("AND PRODUCT_ID = ?");
            assertThat(jdbc.lastSql()).doesNotContain("RECIPE_ID");
            assertThat(jdbc.lastArgs()).containsExactly(1, 42);
        }

        @Test
        @DisplayName("The delete of the old list uses the user and then the product")
        void deleteInOldListUsesCorrectOrder() {
            // The method sent the values in the wrong order before.
            assertThat(repository.deleteProductInOldShoppingList(1, 42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("AND PRODUCT_ID = ?");
            assertThat(jdbc.lastSql()).doesNotContain("RECIPE_ID");
            assertThat(jdbc.lastArgs()).containsExactly(1, 42);
        }

        @Test
        @DisplayName("A delete gives 0 when the database is not available")
        void deleteGivesZeroOnError() {
            jdbc.fails();

            assertThat(repository.deleteProductInCurrentShoppingList(1, 42)).isZero();
            assertThat(repository.deleteProductInOldShoppingList(1, 42)).isZero();
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("The current list reads the image and the tags of each product")
        void currentListReadsImagesAndTags() {
            Image image = new Image(7, "Milk");
            when(productRepository.getProductImage(42)).thenReturn(Optional.of(image));
            when(productRepository.getProductTags(42)).thenReturn(Optional.of(
                    new ArrayList<>(Collections.singletonList(new Tag(1, "Vegan")))));
            jdbc.givesRows(Collections.singletonList(product(42, "Milk")));

            List<Product> products = repository.getAllProductsInCurrentShoppingList(1)
                    .orElseThrow(AssertionError::new);

            assertThat(products).hasSize(1);
            assertThat(products.get(0).getProductImage()).isSameAs(image);
            assertThat(products.get(0).getProductTags()).hasSize(1);
        }

        @Test
        @DisplayName("The old list reads the image and the tags of each product")
        void oldListReadsImagesAndTags() {
            jdbc.givesRows(Collections.singletonList(product(42, "Milk")));

            assertThat(repository.getAllProductsInOldShoppingList(1))
                    .get().asList().hasSize(1);
            verify(productRepository).getProductImage(42);
            verify(productRepository).getProductTags(42);
        }

        @Test
        @DisplayName("A product with the key 0 gets no image and no tags")
        void productWithKeyZeroIsSkipped() {
            jdbc.givesRows(Collections.singletonList(product(0, "Milk")));

            repository.getAllProductsInCurrentShoppingList(1);

            verify(productRepository, org.mockito.Mockito.never()).getProductImage(anyInt());
        }

        @Test
        @DisplayName("A product without a key gets no image and no tags")
        void productWithoutKeyIsSkipped() {
            jdbc.givesRows(Collections.singletonList(product(null, "Milk")));

            repository.getAllProductsInCurrentShoppingList(1);

            verify(productRepository, org.mockito.Mockito.never()).getProductImage(anyInt());
        }

        @Test
        @DisplayName("A read gives an empty list when the database is not available")
        void readGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllProductsInCurrentShoppingList(1)).get().asList().isEmpty();
            assertThat(repository.getAllProductsInOldShoppingList(1)).get().asList().isEmpty();
        }

        @Test
        @DisplayName("Each key of a list comes from its own table")
        void keysComeFromTheirTables() {
            jdbc.givesObject(11);

            assertThat(repository.getCurrentShoppingListId(1)).isEqualTo(11);
            assertThat(jdbc.lastSql()).contains("ET_CURRENT_SHOPPINGLIST");

            assertThat(repository.getOldShoppingListId(1)).isEqualTo(11);
            assertThat(jdbc.lastSql()).contains("ET_OLD_SHOPPINGLIST");

            assertThat(repository.getInventoryListId(1)).isEqualTo(11);
            assertThat(jdbc.lastSql()).contains("ET_INVENTORYLIST");
        }

        @Test
        @DisplayName("Each key is 0 when the database is not available")
        void keysAreZeroOnError() {
            jdbc.fails();

            assertThat(repository.getCurrentShoppingListId(1)).isZero();
            assertThat(repository.getOldShoppingListId(1)).isZero();
            assertThat(repository.getInventoryListId(1)).isZero();
        }
    }

    @Nested
    @DisplayName("The update operations")
    class UpdateOperations {

        @Test
        @DisplayName("The update of the current list uses the key of the current list")
        void updateCurrentListUsesCurrentListKey() {
            jdbc.givesObject(11);

            assertThat(repository.updateProductInCurrentShoppingList(1, product(42, "Milk")))
                    .isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("WHERE CUR_SHOPPINGLIST_ID = ?");
            assertThat(jdbc.lastArgs()).containsExactly("Milk", Date.valueOf("2022-02-02"),
                    "1 Package", "Dairy", "250 kcal", 11, 42);
        }

        @Test
        @DisplayName("The update of the old list uses the key of the old list")
        void updateOldListUsesOldListKey() {
            jdbc.givesObject(13);

            assertThat(repository.updateProductInOldShoppingList(1, product(42, "Milk")))
                    .isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("WHERE OLD_SHOPPINGLIST_ID = ?");
            assertThat(jdbc.lastArgs()).containsExactly("Milk", Date.valueOf("2022-02-02"),
                    "1 Package", "Dairy", "250 kcal", 13, 42);
        }
    }

    @Nested
    @DisplayName("The other write operations")
    class OtherWriteOperations {

        @Test
        @DisplayName("createDietryTag gives the new key")
        void createDietryTagGivesNewKey() throws Exception {
            jdbc.givesKey("TAG_ID", 2);

            assertThat(repository.createDietryTag("Vegan")).isEqualTo(2);
            verify(jdbc.statement()).setString(1, "Vegan");
        }

        @Test
        @DisplayName("createProductTag writes the two foreign keys")
        void createProductTagWritesForeignKeys() {
            assertThat(repository.createProductTag(42, 2)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(42, 2);
        }

        @Test
        @DisplayName("createProductImage writes the name and the address")
        void createProductImageWritesNameAndUrl() throws Exception {
            jdbc.givesKey("IMAGE_ID", 7);

            assertThat(repository.createProductImage(42, "Product image", "")).isEqualTo(7);

            verify(jdbc.statement()).setInt(1, 42);
            verify(jdbc.statement()).setString(2, "Product image");
            verify(jdbc.statement()).setString(3, "");
        }
    }

    @Nested
    @DisplayName("The row mapper")
    class RowMappers {

        @Test
        @DisplayName("The mapper of the product reads all columns")
        void productMapperReadsAllColumns() throws Exception {
            jdbc.givesRows(new ArrayList<Product>());
            repository.getAllProductsInCurrentShoppingList(1);
            Map<String, Object> row = new HashMap<>();
            row.put("PRODUCT_ID", 42);
            row.put("PRODUCT_NAME", "Milk");
            row.put("EXPIRATION_DATE", Date.valueOf("2022-02-02"));
            row.put("QUANTITY", "1 Package");
            row.put("MANUFACTURER", "Dairy");
            row.put("NUTRITION_VALUE", "250 kcal");

            Product mapped = jdbc.mapRow(row);

            assertThat(mapped).isEqualTo(product(42, "Milk"));
        }
    }
}
