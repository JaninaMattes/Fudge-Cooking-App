package com.foodtracker.foodtrackerapi.repositories;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ShoppingListRepositoryImpl implements ShoppingListRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    InventoryRepository productRepository;

    // INSERT Statements
    private static final String SQL_CREATE_PRODUCT_IN_CUR_SHOPPINGLIST = "INSERT INTO ET_PRODUCT(PRODUCT_ID, " +
            "CUR_SHOPPINGLIST_ID, OLD_SHOPPINGLIST_ID, " +
            "INVENTORYLIST_ID, PRODUCT_NAME, EXPIRATION_DATE, QUANTITY, MANUFACTURER, NUTRITION_VALUE) " +
            "VALUES(NEXTVAL('ET_PRODUCT_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_TAG = "INSERT INTO ET_DIETRY_TAG (TAG_ID, LABEL) VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?);";
    private static final String SQL_CREATE_PRODUCT_TAG = "INSERT INTO ET_PRODUCT_TAG (PRODUCT_ID, TAG_ID) VALUES (?, ?);"; // compound
                                                                                                                           // table
    private static final String SQL_CREATE_PRODUCT_IMAGE = "INSERT INTO ET_PRODUCT_IMAGE " +
            "(IMAGE_ID, PRODUCT_ID, IMAGE_NAME, IMAGE_URL) " +
            "VALUES (NEXTVAL('ET_PRODUCT_IMAGE_SEQ'), ?, ?, ?);";

    // UPDATE Statements
    private static final String SQL_UPDATE_PRODUCT_IN_CUR_SHOPPINGLIST = "UPDATE ET_PRODUCT SET  " +
            "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? WHERE CUR_SHOPPINGLIST_ID = ? AND PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_IN_OLD_SHOPPINGLIST = "UPDATE ET_PRODUCT SET  " +
            "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? WHERE OLD_SHOPPINGLIST_ID = ? AND PRODUCT_ID = ? ;";
    private static final String SQL_UPDATE_PRODCUT_TO_OLD_SHOPPINGLIST = "UPDATE ET_PRODUCT SET CUR_SHOPPINGLIST_ID = NULL, OLD_SHOPPINGLIST_ID = ?, QUANTITY = ? WHERE PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_PRODCUT_TO_CUR_SHOPPINGLIST = "UPDATE ET_PRODUCT SET CUR_SHOPPINGLIST_ID = ?, OLD_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_REMOVE_PRODCUT_FROM_CURSHOPPINGLIST = "UPDATE ET_PRODUCT SET CUR_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_REMOVE_PRODCUT_FROM_OLDSHOPPINGLIST = "UPDATE ET_PRODUCT SET OLD_SHOPPINGLIST_ID = NULL WHERE PRODUCT_ID = ?;";

    // DELETE Statements
    private static final String SQL_DELETE_PRODUCT_IN_CURRENT_SHOPPINGLIST = "DELETE FROM ET_PRODUCT " +
            "WHERE  CUR_SHOPPINGLIST_ID = (SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?) AND RECIPE_ID = ?;";
    private static final String SQL_DELETE_PRODUCT_IN_OLD_SHOPPINGLIST = "DELETE FROM ET_PRODUCT " +
            "WHERE OLD_SHOPPINGLIST_ID = (SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?) AND RECIPE_ID = ?;";

    // SELECT Statements
    private static final String SQL_GET_ALL_PRODUCTS_IN_CURRENT_SHOPPINGLIST = "SELECT * FROM ET_PRODUCT WHERE CUR_SHOPPINGLIST_ID = (SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_ALL_PRODUCTS_IN_OLD_SHOPPINGLIST = "SELECT * FROM ET_PRODUCT WHERE OLD_SHOPPINGLIST_ID = (SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_CURRENT_SHOPPINGLIST_ID = "SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_OLD_SHOPPINGLIST_ID = "SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_INVENTORYLIST_ID = "SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?;";

    /*
     * New products are created for current shopping list.
     * Products in shoppinglist have no images and now tags.
     */
    @Override
    public Integer createProductInCurrentShoppingList(Integer userId, Product product) {
        Integer productId = 0;
        try {
            Integer curshplistid = getCurrentShoppingListId(userId);
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            var expDate = (Date) product.getExpirationDate();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_PRODUCT_IN_CUR_SHOPPINGLIST,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, curshplistid);
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
                ps.setString(4, product.getProductName());
                ps.setDate(5, expDate);
                ps.setString(6, product.getQuantity());
                ps.setString(7, product.getManufacturer());
                ps.setString(8, product.getNutritionValue());
                return ps;
            }, keyHolder);
            productId = (Integer) keyHolder.getKeys().get("PRODUCT_ID");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productId;
    }

    @Override
    public Integer createDietryTag(String label) {
        Integer dietryTagId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        SQL_CREATE_TAG, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, label);
                return ps;
            }, keyHolder);
            dietryTagId = (Integer) keyHolder.getKeys().get("TAG_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return dietryTagId;

    }

    @Override
    public Integer createProductTag(Integer productId, Integer tagId) {
        // Create new entry in dietry_tag table then create the composite table
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_CREATE_PRODUCT_TAG, productId, tagId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    /**
     * Create user image.
     */
    @Override
    public Integer createProductImage(Integer productId, String imageName, String imageUrl) {
        Integer result = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        SQL_CREATE_PRODUCT_IMAGE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, productId);
                ps.setString(2, imageName);
                ps.setString(3, imageUrl);
                return ps;
            }, keyHolder);
            result = (Integer) keyHolder.getKeys().get("IMAGE_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    /**
     * Products can be created or added, for current shoppinglist.
     */
    @Override
    public Integer addProductInCurShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            Integer curshplistid = getCurrentShoppingListId(userId);
            if (curshplistid != 0) {
                result = jdbcTemplate.update(SQL_UPDATE_PRODCUT_TO_CUR_SHOPPINGLIST, curshplistid, productId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Products can only be added, NOT created for old shoppinglist.
     */
    @Override
    public Integer addProductInOldShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            Integer oldshplistid = getOldShoppingListId(userId);
            result = jdbcTemplate.update(SQL_UPDATE_PRODCUT_TO_OLD_SHOPPINGLIST, oldshplistid, "1 Package", productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Optional<List<Product>> getAllProductsInCurrentShoppingList(Integer userId) {
        List<Product> products = new ArrayList<>();
        try {
            products = jdbcTemplate.query(SQL_GET_ALL_PRODUCTS_IN_CURRENT_SHOPPINGLIST, productRowMapper, userId);
            for (Product product : products) {
                Integer productId = product.getProductId();
                if (productId != 0) {
                    Optional<Image> image = productRepository.getProductImage(productId);
                    if (!image.isEmpty() && image.get() != null) {
                        product.setProductImage(image.get());
                    }
                    Optional<ArrayList<Tag>> tags = productRepository.getProductTags(productId);
                    if (!tags.isEmpty() && tags.get() != null) {
                        product.setProductTags(tags.get());
                    }
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return Optional.of(products);
    }

    @Override
    public Optional<List<Product>> getAllProductsInOldShoppingList(Integer userID) {
        List<Product> products = new ArrayList<>();
        try {
            products = jdbcTemplate.query(SQL_GET_ALL_PRODUCTS_IN_OLD_SHOPPINGLIST, productRowMapper, userID);
            for (Product product : products) {
                Integer productId = product.getProductId();
                if (productId != 0) {
                    Optional<Image> image = productRepository.getProductImage(productId);
                    if (!image.isEmpty() && image.get() != null) {
                        product.setProductImage(image.get());
                    }
                    Optional<ArrayList<Tag>> tags = productRepository.getProductTags(productId);
                    if (!tags.isEmpty() && tags.get() != null) {
                        product.setProductTags(tags.get());
                    }
                }
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        return Optional.of(products);
    }

    @Override
    public Integer updateProductInCurrentShoppingList(Integer userId, Product product) {
        Integer result = 0;
        try {
            Integer curshplistid = getCurrentShoppingListId(userId);
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_IN_CUR_SHOPPINGLIST,
                    product.getProductName(), product.getExpirationDate(), product.getQuantity(),
                    product.getManufacturer(), product.getNutritionValue(), curshplistid, product.getProductId());
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateProductInOldShoppingList(Integer userId, Product product) {
        Integer result = 0;
        try {
            Integer oldshplistid = getOldShoppingListId(userId);
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_IN_OLD_SHOPPINGLIST,
                    product.getProductName(), product.getExpirationDate(), product.getQuantity(),
                    product.getManufacturer(), product.getNutritionValue(), oldshplistid, product.getProductId());
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer removeProductFromCurShoppingList(Integer productId, Integer curShoppingListId) {
        Integer result = 0;
        try {
            if (curShoppingListId != 0) {
                result = jdbcTemplate.update(SQL_UPDATE_REMOVE_PRODCUT_FROM_CURSHOPPINGLIST, productId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer removeProductFromOldShoppingList(Integer productId, Integer oldShoppingListId) {
        Integer result = 0;
        try {
            if (oldShoppingListId != 0) {
                result = jdbcTemplate.update(SQL_UPDATE_REMOVE_PRODCUT_FROM_OLDSHOPPINGLIST, productId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteProductInCurrentShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_PRODUCT_IN_CURRENT_SHOPPINGLIST, userId, productId);

        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;

    }

    @Override
    public Integer deleteProductInOldShoppingList(Integer userId, Integer productId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_PRODUCT_IN_OLD_SHOPPINGLIST, productId, userId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer getCurrentShoppingListId(Integer userId) {
        Integer shopId = 0;
        try {
            shopId = jdbcTemplate.queryForObject(SQL_GET_CURRENT_SHOPPINGLIST_ID, Integer.class,
                    new Object[] { userId });
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return shopId;
    }

    @Override
    public Integer getOldShoppingListId(Integer userId) {
        Integer shopId = 0;
        try {
            shopId = jdbcTemplate.queryForObject(SQL_GET_OLD_SHOPPINGLIST_ID, Integer.class, new Object[] { userId });
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return shopId;
    }

    @Override
    public Integer getInventoryListId(Integer userId) {
        Integer invId = 0;
        try {
            invId = jdbcTemplate.queryForObject(SQL_GET_INVENTORYLIST_ID, Integer.class, new Object[] { userId });
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return invId;
    }

    private RowMapper<Product> productRowMapper = ((rs, rowNum) -> {
        return new Product(
                rs.getInt("PRODUCT_ID"),
                rs.getString("PRODUCT_NAME"),
                rs.getDate("EXPIRATION_DATE"),
                rs.getString("QUANTITY"),
                rs.getString("MANUFACTURER"),
                rs.getString("NUTRITION_VALUE"));
    });

}
