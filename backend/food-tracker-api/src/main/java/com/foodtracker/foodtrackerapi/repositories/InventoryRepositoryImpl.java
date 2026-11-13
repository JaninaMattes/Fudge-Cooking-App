package com.foodtracker.foodtrackerapi.repositories;

import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.common.utils.DateUtils;
import com.foodtracker.foodtrackerapi.common.utils.ImageUtils;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * <p>
 * Reads and writes the products of the inventory list, with their images and
 * their dietary tags.
 * </p>
 *
 * <p>
 * A product of the inventory list and a product of a shopping list are rows of
 * the same table ET_PRODUCT. The foreign keys decide on which list a product
 * is. A product that belongs to no list has three empty foreign keys.
 * </p>
 *
 * @author Janina Mattes
 */
@Repository
@Transactional
public class InventoryRepositoryImpl extends AbstractJdbcRepository implements InventoryRepository {

    // Create statements
    private static final String SQL_CREATE_TAG = "INSERT INTO ET_DIETRY_TAG (TAG_ID, LABEL) "
            + "VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?);";
    // Compound table. It holds foreign keys only.
    private static final String SQL_CREATE_PRODUCT_TAG = "INSERT INTO ET_PRODUCT_TAG (PRODUCT_ID, TAG_ID) "
            + "VALUES (?, ?);";
    private static final String SQL_CREATE_PRODUCT = "INSERT INTO ET_PRODUCT (PRODUCT_ID, "
            + "CUR_SHOPPINGLIST_ID, OLD_SHOPPINGLIST_ID, INVENTORYLIST_ID, PRODUCT_NAME, "
            + "EXPIRATION_DATE, QUANTITY, "
            + "MANUFACTURER, NUTRITION_VALUE) "
            + "VALUES(NEXTVAL('ET_PRODUCT_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_PRODUCT_IMAGE = "INSERT INTO ET_PRODUCT_IMAGE "
            + "(IMAGE_ID, PRODUCT_ID, IMAGE_NAME, IMAGE_URL, IMAGE_BYTES) "
            + "VALUES (NEXTVAL('ET_PRODUCT_IMAGE_SEQ'), ?, ?, ?, ?);";

    // Update statements
    private static final String SQL_UPDATE_PRODUCT_IN_INVENTARYLIST = "UPDATE ET_PRODUCT SET "
            + "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? "
            + "WHERE INVENTORYLIST_ID  = "
            + "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?) AND PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_IMAGE = "UPDATE ET_PRODUCT_IMAGE "
            + "SET IMAGE_URL = ?, IMAGE_BYTES = ? WHERE IMAGE_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_ON_INVENTORYLIST = "UPDATE ET_PRODUCT SET "
            + "INVENTORYLIST_ID = NULL WHERE INVENTORYLIST_ID = ? AND PRODUCT_ID = ?;";

    // Delete statements
    private static final String SQL_DELETE_PRODUCT_IMAGE = "DELETE FROM ET_PRODUCT_IMAGE "
            + "WHERE PRODUCT_ID = ?;";
    private static final String SQL_DELETE_PRODUCT_TAG = "DELETE FROM ET_PRODUCT_TAG "
            + "WHERE PRODUCT_ID = ? AND TAG_ID = ?;";
    private static final String SQL_DELETE_TAG = "DELETE FROM ET_DIETRY_TAG WHERE TAG_ID = ?";
    private static final String SQL_DELETE_PRODUCT = "DELETE FROM ET_PRODUCT WHERE PRODUCT_ID = ?;";

    // Select statements
    private static final String SQL_GET_ALL_PRODUCTS_IN_INVENTORY = "SELECT * FROM ET_PRODUCT "
            + "WHERE ET_PRODUCT.INVENTORYLIST_ID = "
            + "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_ALL_PRODUCT_TAGS = "SELECT PRODUCT_ID, TAG_ID FROM ET_PRODUCT_TAG "
            + "FULL OUTER JOIN ET_DIETRY_TAG ON ET_PRODUCT_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID "
            + "WHERE PRODUCT_ID=?;";
    private static final String SQL_GET_TAG_IDS = "SELECT * FROM ET_PRODUCT_TAG WHERE PRODUCT_ID = ?;";
    private static final String SQL_GET_PRODUCT_WITH_SHORT_EXPDATE = "SELECT * FROM ET_PRODUCT "
            + "WHERE EXPIRATION_DATE <= CURRENT_DATE + 3 AND ET_PRODUCT.INVENTORYLIST_ID = "
            + "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_INVENTORYLIST_BY_ID = "SELECT INVENTORYLIST_ID "
            + "FROM ET_INVENTORYLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_CURRENT_SHOPPINGLIST_BY_ID = "SELECT CUR_SHOPPINGLIST_ID "
            + "FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_OLD_SHOPPINGLIST_BY_ID = "SELECT OLD_SHOPPINGLIST_ID "
            + "FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_PRODUCT_IMAGE_BY_ID = "SELECT IMAGE_ID, PRODUCT_ID, IMAGE_NAME, "
            + "IMAGE_URL, IMAGE_BYTES FROM ET_PRODUCT_IMAGE WHERE PRODUCT_ID = ?;";
    private static final String SQL_GET_PRODUCT_TAGS = "SELECT * FROM ET_PRODUCT INNER JOIN ET_PRODUCT_TAG "
            + "ON ET_PRODUCT.PRODUCT_ID = ET_PRODUCT_TAG.PRODUCT_ID INNER JOIN ET_DIETRY_TAG ON "
            + "ET_PRODUCT_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID WHERE ET_PRODUCT.PRODUCT_ID = ?;";
    private static final String SQL_GET_EXPIRING_PRODUCTS_BY_ID = "SELECT * FROM ET_PRODUCT "
            + "WHERE EXPIRATION_DATE <= CURRENT_DATE + ? AND ET_PRODUCT.INVENTORYLIST_ID = "
            + "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_COUNT_EXPIRING_PRODUCTS_BY_ID = "SELECT COUNT(*) FROM ET_PRODUCT "
            + "WHERE EXPIRATION_DATE <= CURRENT_DATE + ? AND ET_PRODUCT.INVENTORYLIST_ID = "
            + "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";

    private final RowMapper<Product> productRowMapper = (rs, rowNum) -> new Product(
            rs.getInt("PRODUCT_ID"),
            rs.getString("PRODUCT_NAME"),
            rs.getDate("EXPIRATION_DATE"),
            rs.getString("QUANTITY"),
            rs.getString("MANUFACTURER"),
            rs.getString("NUTRITION_VALUE"));

    private final RowMapper<Image> imageRowMapper = (rs, rowNum) -> new Image(
            rs.getInt("IMAGE_ID"),
            rs.getString("IMAGE_NAME"),
            rs.getString("IMAGE_URL"),
            ImageUtils.bytesToBase64(rs.getBytes("IMAGE_BYTES")));

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> new Tag(
            rs.getInt("TAG_ID"),
            rs.getString("LABEL"));

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate connection to the database
     */
    public InventoryRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Integer createProductInInventoryList(Product product, int inventorylistId) {
        Date expirationDate = DateUtils.toSqlDate(product.getExpirationDate());
        return insertAndGetKey("create a product", SQL_CREATE_PRODUCT, "PRODUCT_ID", statement -> {
            // A product of the inventory list is on no shopping list.
            statement.setNull(1, Types.INTEGER);
            statement.setNull(2, Types.INTEGER);
            statement.setInt(3, inventorylistId);
            statement.setString(4, product.getProductName());
            statement.setDate(5, expirationDate);
            statement.setString(6, product.getQuantity());
            statement.setString(7, product.getManufacturer());
            statement.setString(8, product.getNutritionValue());
        });
    }

    @Override
    public Integer createProductImage(Integer productId, Image image) {
        byte[] imageBytes = ImageUtils.base64ToBytes(image.getBase64ImageString());
        return insertAndGetKey("create the product image", SQL_CREATE_PRODUCT_IMAGE, "IMAGE_ID",
                statement -> {
                    statement.setInt(1, productId);
                    statement.setString(2, image.getImageName());
                    statement.setString(3, image.getImageUrl());
                    if (imageBytes != null && imageBytes.length > 0) {
                        statement.setBytes(4, imageBytes);
                    } else {
                        statement.setNull(4, Types.NULL);
                    }
                });
    }

    @Override
    public Integer createDietryTag(String label) {
        return insertAndGetKey("create a dietary tag", SQL_CREATE_TAG, "TAG_ID",
                statement -> statement.setString(1, label));
    }

    @Override
    public Integer createProductTag(Integer productId, Integer dietryTagId) {
        return update("link the dietary tag to the product", SQL_CREATE_PRODUCT_TAG, productId,
                dietryTagId);
    }

    @Override
    public Integer updateProductInInventoryList(Integer userId, Product product,
            Integer inventorylistId) {
        return update("change a product of the inventory list", SQL_UPDATE_PRODUCT_IN_INVENTARYLIST,
                product.getProductName(), product.getExpirationDate(), product.getQuantity(),
                product.getManufacturer(), product.getNutritionValue(), userId,
                product.getProductId());
    }

    @Override
    public Integer updateProductImage(Image image) {
        byte[] imageBytes = ImageUtils.base64ToBytes(image.getBase64ImageString());
        return update("change the product image", SQL_UPDATE_PRODUCT_IMAGE, image.getImageUrl(),
                imageBytes, image.getImageId());
    }

    @Override
    public Optional<Inventorylist> getInventoryList(Integer userId) {
        Inventorylist inventorylist = new Inventorylist();
        inventorylist.setInventoryId(getInventoryListId(userId));
        List<Product> products = Attempt.orDefault("read the inventory list", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_ALL_PRODUCTS_IN_INVENTORY, productRowMapper,
                        userId));
        inventorylist.setInventoryList(withImagesAndTags(products));
        return Optional.of(inventorylist);
    }

    @Override
    public Optional<ArrayList<Product>> getExpiringProductsList(Integer userId, Integer days) {
        List<Product> products = Attempt.orDefault("read the expiring products", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_EXPIRING_PRODUCTS_BY_ID, productRowMapper, days,
                        userId));
        return Optional.of(withImagesAndTags(products));
    }

    @Override
    public Integer countExpiringProducts(Integer userId, Integer expDays) {
        return queryForId("count the expiring products", SQL_COUNT_EXPIRING_PRODUCTS_BY_ID, expDays,
                userId);
    }

    @Override
    public Integer getInventoryListId(Integer userId) {
        return queryForId("read the key of the inventory list", SQL_GET_INVENTORYLIST_BY_ID, userId);
    }

    @Override
    public Optional<Image> getProductImage(Integer productId) {
        return Optional.ofNullable(Attempt.orDefault("read the product image", null,
                () -> jdbcTemplate.queryForObject(SQL_GET_PRODUCT_IMAGE_BY_ID, imageRowMapper,
                        productId)));
    }

    @Override
    public Optional<ArrayList<Tag>> getProductTags(Integer productId) {
        List<Tag> tags = Attempt.orDefault("read the tags of the product", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_PRODUCT_TAGS, tagRowMapper, productId));
        return Optional.of(new ArrayList<>(tags));
    }

    @Override
    public Optional<List<Integer>> getAllTagsByProductId(Integer productId) {
        List<Tag> tags = Attempt.orDefault("read the keys of the product tags", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_TAG_IDS, tagRowMapper, productId));
        return Optional.of(tags.stream().map(Tag::getTagId).collect(Collectors.toList()));
    }

    /**
     * Takes the product off the inventory list but keeps the row in the
     * database. The product stays available for the shopping lists.
     */
    @Override
    public Integer removeProductFromInventoryList(Integer inventoryId, Integer productId) {
        return update("take the product off the inventory list",
                SQL_UPDATE_PRODUCT_ON_INVENTORYLIST, inventoryId, productId);
    }

    @Override
    public Integer deleteProductOnInventoryList(Integer productId) {
        return update("delete a product", SQL_DELETE_PRODUCT, productId);
    }

    @Override
    public Integer deleteDietryTagById(Integer tagId) {
        return update("delete a dietary tag", SQL_DELETE_TAG, tagId);
    }

    @Override
    public Integer deleteProductTagById(Integer productId, Integer tagId) {
        return update("delete the link between the product and the tag", SQL_DELETE_PRODUCT_TAG,
                productId, tagId);
    }

    @Override
    public Integer deleteProductImage(Integer productId) {
        return update("delete the product image", SQL_DELETE_PRODUCT_IMAGE, productId);
    }

    @Override
    public List<Product> getProductWithShortExpDate(Integer userId) {
        return Attempt.orDefault("read the products that expire soon", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_PRODUCT_WITH_SHORT_EXPDATE, productRowMapper,
                        userId));
    }

    @Override
    public List<Tag> getAllProductTags(Integer userId) {
        return Attempt.orDefault("read all product tags", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_ALL_PRODUCT_TAGS, tagRowMapper, userId));
    }

    @Override
    public Integer getCurrentShoppinglistId(Integer userId) {
        return queryForId("read the key of the current shopping list",
                SQL_GET_CURRENT_SHOPPINGLIST_BY_ID, userId);
    }

    @Override
    public Integer getOldShoppinglistId(Integer userId) {
        return queryForId("read the key of the old shopping list", SQL_GET_OLD_SHOPPINGLIST_BY_ID,
                userId);
    }

    /**
     * Reads the image and the tags of each product. One row of ET_PRODUCT holds
     * the text values only, because the image and the tags are in other tables.
     *
     * @param products products to complete
     * @return the same products, with their image and their tags
     */
    private ArrayList<Product> withImagesAndTags(List<Product> products) {
        ArrayList<Product> result = new ArrayList<>(products);
        for (Product product : result) {
            Integer productId = product.getProductId();
            if (productId == null || NO_ID.equals(productId)) {
                continue;
            }
            getProductImage(productId).ifPresent(product::setProductImage);
            getProductTags(productId).ifPresent(product::setProductTags);
        }
        return result;
    }

}
