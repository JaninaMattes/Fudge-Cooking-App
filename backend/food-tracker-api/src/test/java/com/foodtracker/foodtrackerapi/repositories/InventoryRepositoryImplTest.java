package com.foodtracker.foodtrackerapi.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Types;
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

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * Tests for {@link InventoryRepositoryImpl}.
 *
 * @author Janina Mattes
 */
class InventoryRepositoryImplTest {

    private RecordingJdbcTemplate jdbc;
    private InventoryRepositoryImpl repository;

    private static Product product(Integer id, String name) {
        return new Product(id, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        repository = new InventoryRepositoryImpl(jdbc);
    }

    @Nested
    @DisplayName("createProductInInventoryList")
    class CreateProduct {

        @Test
        @DisplayName("writes the values and leaves the shopping lists empty")
        void writesValuesAndLeavesShoppingListsEmpty() throws Exception {
            jdbc.givesKey("PRODUCT_ID", 42);

            assertThat(repository.createProductInInventoryList(product(null, "Milk"), 5))
                    .isEqualTo(42);

            verify(jdbc.statement()).setNull(1, Types.INTEGER);
            verify(jdbc.statement()).setNull(2, Types.INTEGER);
            verify(jdbc.statement()).setInt(3, 5);
            verify(jdbc.statement()).setString(4, "Milk");
            verify(jdbc.statement()).setDate(5, Date.valueOf("2022-02-02"));
            verify(jdbc.statement()).setString(6, "1 Package");
            verify(jdbc.statement()).setString(7, "Dairy");
            verify(jdbc.statement()).setString(8, "250 kcal");
        }

        @Test
        @DisplayName("accepts a product without an expiration date")
        void acceptsProductWithoutDate() throws Exception {
            jdbc.givesKey("PRODUCT_ID", 42);
            Product product = new Product(null, "Milk", null, "1", "Dairy", "250 kcal");

            assertThat(repository.createProductInInventoryList(product, 5)).isEqualTo(42);
            verify(jdbc.statement()).setDate(5, null);
        }

        @Test
        @DisplayName("converts a date of the application into a date of the database")
        void convertsUtilDateToSqlDate() throws Exception {
            jdbc.givesKey("PRODUCT_ID", 42);
            java.util.Date date = new java.util.Date(Date.valueOf("2022-02-02").getTime());
            Product product = new Product(null, "Milk", date, "1", "Dairy", "250 kcal");

            repository.createProductInInventoryList(product, 5);

            verify(jdbc.statement()).setDate(5, Date.valueOf("2022-02-02"));
        }

        @Test
        @DisplayName("gives the key 0 when the database is not available")
        void givesZeroOnError() {
            jdbc.fails();

            assertThat(repository.createProductInInventoryList(product(null, "Milk"), 5)).isZero();
        }
    }

    @Nested
    @DisplayName("createProductImage")
    class CreateProductImage {

        @Test
        @DisplayName("writes the image data when the image holds data")
        void writesImageData() throws Exception {
            jdbc.givesKey("IMAGE_ID", 7);

            assertThat(repository.createProductImage(42,
                    new Image(0, "Milk", "url", "RmVlZE1l"))).isEqualTo(7);

            verify(jdbc.statement()).setInt(1, 42);
            verify(jdbc.statement()).setString(2, "Milk");
            verify(jdbc.statement()).setString(3, "url");
            verify(jdbc.statement()).setBytes(4, "FeedMe".getBytes(StandardCharsets.UTF_8));
        }

        @Test
        @DisplayName("writes an empty value when the image data decodes to no bytes")
        void writesNullForImageDataWithoutBytes() throws Exception {
            jdbc.givesKey("IMAGE_ID", 7);

            // The MIME decoder ignores the characters that are not Base64, so
            // the image holds an array without bytes.
            repository.createProductImage(42, new Image(0, "Milk", "url", "!!!"));

            verify(jdbc.statement()).setNull(4, Types.NULL);
        }

        @Test
        @DisplayName("writes an empty value when the image holds no data")
        void writesNullWithoutImageData() throws Exception {
            jdbc.givesKey("IMAGE_ID", 7);

            repository.createProductImage(42, new Image(0, "Milk", "url", null));

            verify(jdbc.statement()).setNull(4, Types.NULL);
        }
    }

    @Nested
    @DisplayName("The write operations")
    class WriteOperations {

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
            assertThat(jdbc.lastSql()).contains("ET_PRODUCT_TAG");
        }

        @Test
        @DisplayName("updateProductInInventoryList writes the user and the product last")
        void updateProductWritesKeysLast() {
            assertThat(repository.updateProductInInventoryList(1, product(42, "Milk"), 5))
                    .isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly("Milk", Date.valueOf("2022-02-02"),
                    "1 Package", "Dairy", "250 kcal", 1, 42);
        }

        @Test
        @DisplayName("updateProductImage converts the Base64 string into bytes")
        void updateProductImageConvertsBase64() {
            assertThat(repository.updateProductImage(new Image(7, "Milk", "url", "RmVlZE1l")))
                    .isEqualTo(1);

            assertThat(jdbc.lastArgs().get(0)).isEqualTo("url");
            assertThat((byte[]) jdbc.lastArgs().get(1))
                    .isEqualTo("FeedMe".getBytes(StandardCharsets.UTF_8));
            assertThat(jdbc.lastArgs().get(2)).isEqualTo(7);
        }

        @Test
        @DisplayName("removeProductFromInventoryList only clears the foreign key")
        void removeProductClearsForeignKey() {
            assertThat(repository.removeProductFromInventoryList(5, 42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).contains("INVENTORYLIST_ID = NULL");
            assertThat(jdbc.lastArgs()).containsExactly(5, 42);
        }

        @Test
        @DisplayName("deleteProductOnInventoryList uses the key of the product only")
        void deleteProductUsesProductKeyOnly() {
            // The method sent two values to a statement with one single
            // placeholder before, so the delete always failed.
            assertThat(repository.deleteProductOnInventoryList(42)).isEqualTo(1);

            assertThat(jdbc.lastSql()).isEqualTo("DELETE FROM ET_PRODUCT WHERE PRODUCT_ID = ?;");
            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("deleteDietryTagById uses the key of the tag")
        void deleteDietryTagUsesTagKey() {
            assertThat(repository.deleteDietryTagById(2)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(2);
        }

        @Test
        @DisplayName("deleteProductTagById uses both keys")
        void deleteProductTagUsesBothKeys() {
            assertThat(repository.deleteProductTagById(42, 2)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(42, 2);
        }

        @Test
        @DisplayName("deleteProductImage uses the key of the product")
        void deleteProductImageUsesProductKey() {
            assertThat(repository.deleteProductImage(42)).isEqualTo(1);

            assertThat(jdbc.lastArgs()).containsExactly(42);
        }

        @Test
        @DisplayName("Each write operation gives 0 when the database is not available")
        void writeOperationsGiveZeroOnError() {
            jdbc.fails();

            assertThat(repository.createProductTag(42, 2)).isZero();
            assertThat(repository.updateProductInInventoryList(1, product(42, "Milk"), 5)).isZero();
            assertThat(repository.updateProductImage(new Image(7, "Milk"))).isZero();
            assertThat(repository.removeProductFromInventoryList(5, 42)).isZero();
            assertThat(repository.deleteProductOnInventoryList(42)).isZero();
            assertThat(repository.deleteDietryTagById(2)).isZero();
            assertThat(repository.deleteProductTagById(42, 2)).isZero();
            assertThat(repository.deleteProductImage(42)).isZero();
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("getInventoryList reads the image and the tags of each product")
        void inventoryListReadsImagesAndTags() {
            jdbc.givesObject(5);
            jdbc.givesRows(Collections.singletonList(product(42, "Milk")));

            Inventorylist list = repository.getInventoryList(1).orElseThrow(AssertionError::new);

            assertThat(list.getInventoryId()).isEqualTo(5);
            assertThat(list.getInventoryList()).hasSize(1);
        }

        @Test
        @DisplayName("getInventoryList does not read the image of a product without a key")
        void inventoryListSkipsProductWithoutKey() {
            jdbc.givesRows(Arrays.asList(product(0, "Milk"), product(null, "Bread")));

            Inventorylist list = repository.getInventoryList(1).orElseThrow(AssertionError::new);

            assertThat(list.getInventoryList()).hasSize(2);
            assertThat(list.getInventoryList().get(0).getProductImage()).isNull();
        }

        @Test
        @DisplayName("getInventoryList gives an empty list when the database is not available")
        void inventoryListGivesEmptyListOnError() {
            jdbc.fails();

            Inventorylist list = repository.getInventoryList(1).orElseThrow(AssertionError::new);

            assertThat(list.getInventoryList()).isEmpty();
        }

        @Test
        @DisplayName("getExpiringProductsList sends the days before the user")
        void expiringProductsSendsDaysFirst() {
            jdbc.givesRows(Collections.singletonList(product(42, "Milk")));

            repository.getExpiringProductsList(1, 3);

            assertThat(jdbc.calls().get(0).args()).containsExactly(3, 1);
        }

        @Test
        @DisplayName("getExpiringProductsList gives an empty list when the database fails")
        void expiringProductsGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getExpiringProductsList(1, 3))
                    .get().asList().isEmpty();
        }

        @Test
        @DisplayName("countExpiringProducts gives the number of products")
        void countExpiringProductsGivesCount() {
            jdbc.givesObject(2);

            assertThat(repository.countExpiringProducts(1, 3)).isEqualTo(2);
            assertThat(jdbc.lastArgs()).containsExactly(3, 1);
        }

        @Test
        @DisplayName("countExpiringProducts gives 0 when the database is not available")
        void countExpiringProductsGivesZeroOnError() {
            jdbc.fails();

            assertThat(repository.countExpiringProducts(1, 3)).isZero();
        }

        @Test
        @DisplayName("getInventoryListId gives the key of the list")
        void inventoryListIdGivesKey() {
            jdbc.givesObject(5);

            assertThat(repository.getInventoryListId(1)).isEqualTo(5);
        }

        @Test
        @DisplayName("getProductImage gives an empty result when there is no image")
        void productImageGivesEmptyResultWithoutImage() {
            // The method gave Optional.of(null) before and stopped the caller
            // with a NullPointerException.
            jdbc.givesObject(null);

            assertThat(repository.getProductImage(42)).isEmpty();
        }

        @Test
        @DisplayName("getProductImage gives the image of the product")
        void productImageGivesImage() {
            Image stored = new Image(7, "Milk");
            jdbc.givesObject(stored);

            assertThat(repository.getProductImage(42)).contains(stored);
        }

        @Test
        @DisplayName("getProductTags gives the tags of the product")
        void productTagsGivesTags() {
            jdbc.givesRows(Arrays.asList(new Tag(1, "Vegan"), new Tag(2, "Lactose")));

            assertThat(repository.getProductTags(42)).get().asList().hasSize(2);
        }

        @Test
        @DisplayName("getAllTagsByProductId gives the keys of the tags only")
        void allTagsGivesKeysOnly() {
            jdbc.givesRows(Arrays.asList(new Tag(1, "Vegan"), new Tag(2, "Lactose")));

            assertThat(repository.getAllTagsByProductId(42)).get().asList()
                    .containsExactly(1, 2);
        }

        @Test
        @DisplayName("getAllTagsByProductId gives an empty list when the database fails")
        void allTagsGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllTagsByProductId(42)).get().asList().isEmpty();
        }

        @Test
        @DisplayName("getProductWithShortExpDate gives an empty list when the database fails")
        void shortExpDateGivesEmptyListOnError() {
            // The method gave null before. The dashboard then stopped with a
            // NullPointerException when it counted the products.
            jdbc.fails();

            assertThat(repository.getProductWithShortExpDate(1)).isEmpty();
        }

        @Test
        @DisplayName("getProductWithShortExpDate gives the products")
        void shortExpDateGivesProducts() {
            jdbc.givesRows(Collections.singletonList(product(42, "Milk")));

            assertThat(repository.getProductWithShortExpDate(1)).hasSize(1);
        }

        @Test
        @DisplayName("getAllProductTags gives an empty list when the database fails")
        void allProductTagsGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getAllProductTags(1)).isEmpty();
        }

        @Test
        @DisplayName("getAllProductTags gives the tags")
        void allProductTagsGivesTags() {
            jdbc.givesRows(Collections.singletonList(new Tag(1, "Vegan")));

            assertThat(repository.getAllProductTags(1)).hasSize(1);
        }

        @Test
        @DisplayName("The keys of the shopping lists come from their own tables")
        void shoppingListKeysComeFromTheirTables() {
            jdbc.givesObject(11);

            assertThat(repository.getCurrentShoppinglistId(1)).isEqualTo(11);
            assertThat(jdbc.lastSql()).contains("ET_CURRENT_SHOPPINGLIST");

            assertThat(repository.getOldShoppinglistId(1)).isEqualTo(11);
            assertThat(jdbc.lastSql()).contains("ET_OLD_SHOPPINGLIST");
        }
    }

    @Nested
    @DisplayName("The row mappers")
    class RowMappers {

        @Test
        @DisplayName("The mapper of the product reads all columns")
        void productMapperReadsAllColumns() throws Exception {
            repository.getProductWithShortExpDate(1);
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

        @Test
        @DisplayName("The mapper of the image converts the bytes into a Base64 string")
        void imageMapperConvertsBytes() throws Exception {
            jdbc.givesObject(null);
            repository.getProductImage(42);
            Map<String, Object> row = new HashMap<>();
            row.put("IMAGE_ID", 7);
            row.put("IMAGE_NAME", "Milk");
            row.put("IMAGE_URL", "url");
            row.put("IMAGE_BYTES", "FeedMe".getBytes(StandardCharsets.UTF_8));

            Image mapped = jdbc.mapRow(row);

            assertThat(mapped.getBase64ImageString()).isEqualTo("RmVlZE1l");
        }

        @Test
        @DisplayName("The mapper of the tag reads the key and the name")
        void tagMapperReadsFields() throws Exception {
            jdbc.givesRows(new ArrayList<Tag>());
            repository.getProductTags(42);
            Map<String, Object> row = new HashMap<>();
            row.put("TAG_ID", 2);
            row.put("LABEL", "Vegan");

            Tag mapped = jdbc.mapRow(row);

            assertThat(mapped).isEqualTo(new Tag(2, "Vegan"));
        }
    }

    @Nested
    @DisplayName("The SQL statements")
    class SqlStatements {

        @Test
        @DisplayName("Each statement of the products uses the column PRODUCT_ID")
        void statementsUseProductIdColumn() {
            List<Runnable> operations = Arrays.asList(
                    () -> repository.deleteProductOnInventoryList(42),
                    () -> repository.deleteProductImage(42),
                    () -> repository.removeProductFromInventoryList(5, 42));

            for (Runnable operation : operations) {
                operation.run();
                assertThat(jdbc.lastSql()).contains("PRODUCT_ID");
            }
        }
    }
}
