package com.foodtracker.foodtrackerapi.repositories;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.common.utils.DateUtils;
import com.foodtracker.foodtrackerapi.models.Product;

/**
 * <p>
 * Reads and writes the products of the two shopping lists of a user.
 * </p>
 *
 * <p>
 * A product is on the current list, on the old list or on the inventory list.
 * The three foreign keys of the table ET_PRODUCT hold this information. To move
 * a product, the application sets one foreign key and clears the other.
 * </p>
 *
 * @author Janina Mattes
 */
@Repository
@Transactional
public class ShoppingListRepositoryImpl extends AbstractJdbcRepository
        implements ShoppingListRepository {

    /**
     * Amount that a product gets when the user moves it to the old shopping
     * list. The old list works as a template, so one package is a useful start
     * value.
     */
    private static final String DEFAULT_QUANTITY = "1 Package";

    // Create statements
    private static final String SQL_CREATE_PRODUCT_IN_CUR_SHOPPINGLIST = "INSERT INTO ET_PRODUCT("
            + "PRODUCT_ID, CUR_SHOPPINGLIST_ID, OLD_SHOPPINGLIST_ID, INVENTORYLIST_ID, PRODUCT_NAME, "
            + "EXPIRATION_DATE, QUANTITY, MANUFACTURER, NUTRITION_VALUE) "
            + "VALUES(NEXTVAL('ET_PRODUCT_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_TAG = "INSERT INTO ET_DIETRY_TAG (TAG_ID, LABEL) "
            + "VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?);";
    // Compound table. It holds foreign keys only.
    private static final String SQL_CREATE_PRODUCT_TAG = "INSERT INTO ET_PRODUCT_TAG (PRODUCT_ID, TAG_ID) "
            + "VALUES (?, ?);";
    private static final String SQL_CREATE_PRODUCT_IMAGE = "INSERT INTO ET_PRODUCT_IMAGE "
            + "(IMAGE_ID, PRODUCT_ID, IMAGE_NAME, IMAGE_URL) "
            + "VALUES (NEXTVAL('ET_PRODUCT_IMAGE_SEQ'), ?, ?, ?);";

    // Update statements
    private static final String SQL_UPDATE_PRODUCT_IN_CUR_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? "
            + "WHERE CUR_SHOPPINGLIST_ID = ? AND PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_IN_OLD_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? "
            + "WHERE OLD_SHOPPINGLIST_ID = ? AND PRODUCT_ID = ?;";
    private static final String SQL_MOVE_PRODUCT_TO_OLD_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "CUR_SHOPPINGLIST_ID = NULL, OLD_SHOPPINGLIST_ID = ?, QUANTITY = ? WHERE PRODUCT_ID = ?;";
    private static final String SQL_MOVE_PRODUCT_TO_CUR_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "CUR_SHOPPINGLIST_ID = ?, OLD_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";
    private static final String SQL_REMOVE_PRODUCT_FROM_CUR_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "CUR_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";
    private static final String SQL_REMOVE_PRODUCT_FROM_OLD_SHOPPINGLIST = "UPDATE ET_PRODUCT SET "
            + "OLD_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";

    // Delete statements
    private static final String SQL_DELETE_PRODUCT_IN_CURRENT_SHOPPINGLIST = "DELETE FROM ET_PRODUCT "
            + "WHERE CUR_SHOPPINGLIST_ID = "
            + "(SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?) "
            + "AND PRODUCT_ID = ?;";
    private static final String SQL_DELETE_PRODUCT_IN_OLD_SHOPPINGLIST = "DELETE FROM ET_PRODUCT "
            + "WHERE OLD_SHOPPINGLIST_ID = "
            + "(SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?) "
            + "AND PRODUCT_ID = ?;";

    // Select statements
    private static final String SQL_GET_ALL_PRODUCTS_IN_CURRENT_SHOPPINGLIST = "SELECT * FROM ET_PRODUCT "
            + "WHERE CUR_SHOPPINGLIST_ID = "
            + "(SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_ALL_PRODUCTS_IN_OLD_SHOPPINGLIST = "SELECT * FROM ET_PRODUCT "
            + "WHERE OLD_SHOPPINGLIST_ID = "
            + "(SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_CURRENT_SHOPPINGLIST_ID = "SELECT CUR_SHOPPINGLIST_ID "
            + "FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_OLD_SHOPPINGLIST_ID = "SELECT OLD_SHOPPINGLIST_ID "
            + "FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_INVENTORYLIST_ID = "SELECT INVENTORYLIST_ID "
            + "FROM ET_INVENTORYLIST WHERE USER_ID = ?;";

    private final InventoryRepository productRepository;

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> new Product(
            rs.getInt("PRODUCT_ID"),
            rs.getString("PRODUCT_NAME"),
            rs.getDate("EXPIRATION_DATE"),
            rs.getString("QUANTITY"),
            rs.getString("MANUFACTURER"),
            rs.getString("NUTRITION_VALUE"));

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate      connection to the database
     * @param productRepository repository that reads the images and the tags of
     *                          a product
     */
    public ShoppingListRepositoryImpl(JdbcTemplate jdbcTemplate,
            InventoryRepository productRepository) {
        super(jdbcTemplate);
        this.productRepository = productRepository;
    }

    /**
     * Makes a product on the current shopping list. A new product has no image
     * and no tags. The user adds them when the product goes to the inventory
     * list.
     */
    @Override
    public Integer createProductInCurrentShoppingList(Integer userId, Product product) {
        Integer currentShoppingListId = getCurrentShoppingListId(userId);
        Date expirationDate = DateUtils.toSqlDate(product.getExpirationDate());
        return insertAndGetKey("create a product on the current shopping list",
                SQL_CREATE_PRODUCT_IN_CUR_SHOPPINGLIST, "PRODUCT_ID", statement -> {
                    statement.setInt(1, currentShoppingListId);
                    statement.setNull(2, Types.INTEGER);
                    statement.setNull(3, Types.INTEGER);
                    statement.setString(4, product.getProductName());
                    statement.setDate(5, expirationDate);
                    statement.setString(6, product.getQuantity());
                    statement.setString(7, product.getManufacturer());
                    statement.setString(8, product.getNutritionValue());
                });
    }

    @Override
    public Integer createDietryTag(String label) {
        return insertAndGetKey("create a dietary tag", SQL_CREATE_TAG, "TAG_ID",
                statement -> statement.setString(1, label));
    }

    @Override
    public Integer createProductTag(Integer productId, Integer tagId) {
        return update("link the dietary tag to the product", SQL_CREATE_PRODUCT_TAG, productId,
                tagId);
    }

    @Override
    public Integer createProductImage(Integer productId, String imageName, String imageUrl) {
        return insertAndGetKey("create the product image", SQL_CREATE_PRODUCT_IMAGE, "IMAGE_ID",
                statement -> {
                    statement.setInt(1, productId);
                    statement.setString(2, imageName);
                    statement.setString(3, imageUrl);
                });
    }

    @Override
    public Integer addProductInCurShoppingList(Integer userId, Integer productId) {
        Integer currentShoppingListId = getCurrentShoppingListId(userId);
        if (NO_ID.equals(currentShoppingListId)) {
            return NO_ID;
        }
        return update("move the product to the current shopping list",
                SQL_MOVE_PRODUCT_TO_CUR_SHOPPINGLIST, currentShoppingListId, productId);
    }

    @Override
    public Integer addProductInOldShoppingList(Integer userId, Integer productId) {
        Integer oldShoppingListId = getOldShoppingListId(userId);
        if (NO_ID.equals(oldShoppingListId)) {
            return NO_ID;
        }
        return update("move the product to the old shopping list",
                SQL_MOVE_PRODUCT_TO_OLD_SHOPPINGLIST, oldShoppingListId, DEFAULT_QUANTITY,
                productId);
    }

    @Override
    public Optional<List<Product>> getAllProductsInCurrentShoppingList(Integer userId) {
        List<Product> products = Attempt.orDefault("read the current shopping list",
                new ArrayList<>(), () -> jdbcTemplate
                        .query(SQL_GET_ALL_PRODUCTS_IN_CURRENT_SHOPPINGLIST, productRowMapper,
                                userId));
        return Optional.of(withImagesAndTags(products));
    }

    @Override
    public Optional<List<Product>> getAllProductsInOldShoppingList(Integer userID) {
        List<Product> products = Attempt.orDefault("read the old shopping list", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_ALL_PRODUCTS_IN_OLD_SHOPPINGLIST, productRowMapper,
                        userID));
        return Optional.of(withImagesAndTags(products));
    }

    @Override
    public Integer updateProductInCurrentShoppingList(Integer userId, Product product) {
        Integer currentShoppingListId = getCurrentShoppingListId(userId);
        return update("change a product of the current shopping list",
                SQL_UPDATE_PRODUCT_IN_CUR_SHOPPINGLIST, product.getProductName(),
                product.getExpirationDate(), product.getQuantity(), product.getManufacturer(),
                product.getNutritionValue(), currentShoppingListId, product.getProductId());
    }

    @Override
    public Integer updateProductInOldShoppingList(Integer userId, Product product) {
        Integer oldShoppingListId = getOldShoppingListId(userId);
        return update("change a product of the old shopping list",
                SQL_UPDATE_PRODUCT_IN_OLD_SHOPPINGLIST, product.getProductName(),
                product.getExpirationDate(), product.getQuantity(), product.getManufacturer(),
                product.getNutritionValue(), oldShoppingListId, product.getProductId());
    }

    @Override
    public Integer removeProductFromCurShoppingList(Integer productId, Integer curShoppingListId) {
        if (NO_ID.equals(curShoppingListId)) {
            return NO_ID;
        }
        return update("take the product off the current shopping list",
                SQL_REMOVE_PRODUCT_FROM_CUR_SHOPPINGLIST, productId);
    }

    @Override
    public Integer removeProductFromOldShoppingList(Integer productId, Integer oldShoppingListId) {
        if (NO_ID.equals(oldShoppingListId)) {
            return NO_ID;
        }
        return update("take the product off the old shopping list",
                SQL_REMOVE_PRODUCT_FROM_OLD_SHOPPINGLIST, productId);
    }

    @Override
    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId) {
        return update("delete a product of the current shopping list",
                SQL_DELETE_PRODUCT_IN_CURRENT_SHOPPINGLIST, userId, productId);
    }

    @Override
    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId) {
        return update("delete a product of the old shopping list",
                SQL_DELETE_PRODUCT_IN_OLD_SHOPPINGLIST, userId, productId);
    }

    @Override
    public Integer getCurrentShoppingListId(Integer userId) {
        return queryForId("read the key of the current shopping list",
                SQL_GET_CURRENT_SHOPPINGLIST_ID, userId);
    }

    @Override
    public Integer getOldShoppingListId(Integer userId) {
        return queryForId("read the key of the old shopping list", SQL_GET_OLD_SHOPPINGLIST_ID,
                userId);
    }

    @Override
    public Integer getInventoryListId(Integer userId) {
        return queryForId("read the key of the inventory list", SQL_GET_INVENTORYLIST_ID, userId);
    }

    /**
     * Reads the image and the tags of each product.
     *
     * @param products products to complete
     * @return the same products, with their image and their tags
     */
    private List<Product> withImagesAndTags(List<Product> products) {
        for (Product product : products) {
            Integer productId = product.getProductId();
            if (productId == null || NO_ID.equals(productId)) {
                continue;
            }
            productRepository.getProductImage(productId).ifPresent(product::setProductImage);
            productRepository.getProductTags(productId).ifPresent(product::setProductTags);
        }
        return products;
    }

}
