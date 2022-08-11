package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.common.utils.ImageUtils;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryRepositoryImpl implements InventoryRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // Create
    private static final String SQL_CREATE_TAG = "INSERT INTO ET_DIETRY_TAG (TAG_ID, LABEL) VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?);";
    private static final String SQL_CREATE_PRODUCT_TAG = "INSERT INTO ET_PRODUCT_TAG (PRODUCT_ID, TAG_ID) VALUES (?, ?);"; // compound table
    private static final String SQL_CREATE_PRODUCT = "INSERT INTO ET_PRODUCT (PRODUCT_ID, CUR_SHOPPINGLIST_ID, " +
            "OLD_SHOPPINGLIST_ID, INVENTORYLIST_ID, PRODUCT_NAME, EXPIRATION_DATE, QUANTITY, MANUFACTURER, NUTRITION_VALUE) " +
            "VALUES(NEXTVAL('ET_PRODUCT_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SQL_CREATE_PRODUCT_IMAGE = "INSERT INTO ET_PRODUCT_IMAGE " +
            "(IMAGE_ID, PRODUCT_ID, IMAGE_NAME, IMAGE_URL, IMAGE_BYTES) " +
            "VALUES (NEXTVAL('ET_PRODUCT_IMAGE_SEQ'), ?, ?, ?, ?);";

    // Update
    private static final String SQL_UPDATE_PRODUCT_IN_INVENTARYLIST = "UPDATE ET_PRODUCT SET " +
            "PRODUCT_NAME = ?, EXPIRATION_DATE = ?, QUANTITY = ?, MANUFACTURER = ?, NUTRITION_VALUE = ? " +
            "WHERE INVENTORYLIST_ID  = " +
            "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?) AND PRODUCT_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_IMAGE = "UPDATE ET_PRODUCT_IMAGE SET IMAGE_URL = ?, IMAGE_BYTES = ? WHERE IMAGE_ID = ?;";
    private static final String SQL_UPDATE_PRODUCT_ON_INVENTORYLIST = "UPDATE ET_PRODUCT SET INVENTORYLIST_ID = NULL " +
            "WHERE INVENTORYLIST_ID = ? AND PRODUCT_ID = ?;";

    // Delete
    private static final String SQL_DELETE_PRODUCT_IMAGE = "DELETE FROM ET_PRODUCT_IMAGE WHERE PRODUCT_ID = ?;";
    private static final String SQL_DELETE_PRODUCT_TAG = "DELETE FROM ET_PRODUCT_TAG WHERE PRODUCT_ID = ? AND TAG_ID = ?;";
    private static final String SQL_DELETE_TAG = "DELETE FROM ET_DIETRY_TAG WHERE TAG_ID = ?";
    private static final String SQL_DELETE_PRODUCT = "DELETE FROM ET_PRODUCT WHERE PRODUCT_ID = ?;";

    // Select
    private static final String SQL_GET_ALL_PRODUCTS_IN_INVENTORY = "SELECT * FROM ET_PRODUCT " +
            "WHERE ET_PRODUCT.INVENTORYLIST_ID = " +
            "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_ALL_PRODUCT_TAGS = "SELECT PRODUCT_ID, TAG_ID FROM ET_PRODUCT_TAG " +
            "FULL OUTER JOIN ET_DIETRY_TAG ON ET_PRODUCT_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID WHERE PRODUCT_ID=?;";
    private static final String SQL_GET_TAG_IDS = "SELECT * FROM ET_PRODUCT_TAG WHERE PRODUCT_ID = ?;";
    private static final String SQL_GET_PRODUCT_WITH_SHORT_EXPDATE = "SELECT * FROM ET_PRODUCT " +
            "WHERE EXPIRATION_DATE <= CURRENT_DATE + 3 AND ET_PRODUCT.INVENTORYLIST_ID = " +
            "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_GET_INVENTORYLIST_BY_ID = "SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_CURRENT_SHOPPINGLIST_BY_ID = "SELECT CUR_SHOPPINGLIST_ID FROM ET_CURRENT_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_OLD_SHOPPINGLIST_BY_ID = "SELECT OLD_SHOPPINGLIST_ID FROM ET_OLD_SHOPPINGLIST WHERE USER_ID = ?;";
    private static final String SQL_GET_PRODUCT_IMAGE_BY_ID = "SELECT IMAGE_ID, PRODUCT_ID, IMAGE_NAME, IMAGE_URL, IMAGE_BYTES FROM ET_PRODUCT_IMAGE WHERE PRODUCT_ID = ?;";
    private static final String SQL_GET_PRODUCT_TAGS = "SELECT * FROM ET_PRODUCT INNER JOIN ET_PRODUCT_TAG " +
            "ON ET_PRODUCT.PRODUCT_ID = ET_PRODUCT_TAG.PRODUCT_ID INNER JOIN ET_DIETRY_TAG ON " +
            "ET_PRODUCT_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID WHERE ET_PRODUCT.PRODUCT_ID = ?;";
    private static final String SQL_GET_EXPIRING_PRODUCTS_BY_ID = "SELECT * FROM ET_PRODUCT " +
            "WHERE EXPIRATION_DATE <= CURRENT_DATE + ? AND ET_PRODUCT.INVENTORYLIST_ID = " +
            "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";
    private static final String SQL_COUNT_EXPIRING_PRODUCTS_BY_ID = "SELECT COUNT(*) FROM ET_PRODUCT " +
            "WHERE EXPIRATION_DATE <= CURRENT_DATE + ? AND ET_PRODUCT.INVENTORYLIST_ID = " +
            "(SELECT INVENTORYLIST_ID FROM ET_INVENTORYLIST WHERE USER_ID = ?);";


    @Override
    public Integer createProductInInventoryList(Product product, int inventorylistId) {
        Integer productId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            Date expDate = (Date) product.getExpirationDate();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_PRODUCT, Statement.RETURN_GENERATED_KEYS);
                ps.setNull(1, Types.INTEGER); // shoppinglists are null
                ps.setNull(2, Types.INTEGER);
                ps.setInt(3, inventorylistId);
                ps.setString(4, product.getProductName());
                ps.setDate(5, expDate);
                ps.setString(6, product.getQuantity());
                ps.setString(7, product.getManufacturer());
                ps.setString(8, product.getNutritionValue());
                return ps;
            }, keyHolder);
            return (Integer) keyHolder.getKeys().get("PRODUCT_ID");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productId;
    }

    @Override
    public Integer createProductImage(Integer productId, Image image) {
        Integer imgId = 0;
        try {
            byte[] imageBytes = convertBase64StringToBytes(image.getBase64ImageString());
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        SQL_CREATE_PRODUCT_IMAGE, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, productId);
                ps.setString(2, image.getImageName());
                ps.setString(3, image.getImageUrl());
                if(imageBytes != null && imageBytes.length > 0){
                    ps.setBytes(4, imageBytes);
                }else{
                    ps.setNull(4, Types.NULL);
                }
                return ps;
            }, keyHolder);
            imgId = (Integer) keyHolder.getKeys().get("IMAGE_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return imgId;
    }


    @Override
    public Integer createDietryTag(String label) {
        Integer result = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        SQL_CREATE_TAG, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, label);
                return ps;
            }, keyHolder);
            result = (Integer) keyHolder.getKeys().get("TAG_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;

    }

    @Override
    public Integer createProductTag(Integer productId, Integer dietryTagId) {
        // Create new entry in dietry_tag table then create the composite table
        Integer result = 0;
        try {
            // second create compound table with product id + tag id
            result = jdbcTemplate.update(SQL_CREATE_PRODUCT_TAG, productId, dietryTagId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateProductInInventoryList(Integer userId, Product product, Integer inventorylistId) {
        Integer result = 0;
        try {
            Integer productId = product.getProductId();
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_IN_INVENTARYLIST,
                    product.getProductName(), product.getExpirationDate(), product.getQuantity(),
                    product.getManufacturer(),
                    product.getNutritionValue(), userId, productId);
        } catch (

        Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateProductImage(Image image) {
        Integer result = 0;
        try {
            byte[] imageBytes = convertBase64StringToBytes(image.getBase64ImageString());
            // params.addValue("IMAGE_BYTES", image.getImageBytes()); // can be null
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_IMAGE, image.getImageUrl(), imageBytes, image.getImageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Optional<Inventorylist> getInventoryList(Integer userId) {
        Inventorylist inventorylist = new Inventorylist();

        try {
            inventorylist.setInventoryId(getInventoryListId(userId));
            List<Product> inventory = jdbcTemplate.query(SQL_GET_ALL_PRODUCTS_IN_INVENTORY, productRowMapper, userId);
            ArrayList<Product> products = new ArrayList<>(inventory);
            for (Product product : products) {
                Optional<Image> image = getProductImage(product.getProductId());
                if (!image.isEmpty() && image.get() != null) {
                    product.setProductImage(image.get());
                }
                Optional<ArrayList<Tag>> tags = getProductTags(product.getProductId());
                if (!tags.isEmpty() && tags.get() != null) {
                    product.setProductTags(new ArrayList<>(tags.get()));
                }
            }
            inventorylist.setInventoryList(products);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.of(inventorylist);
    }

    @Override
    public Optional<ArrayList<Product>> getExpiringProductsList(Integer userId, Integer days) {
        ArrayList<Product> products = new ArrayList<>();
        try {
            List<Product> results = jdbcTemplate.query(SQL_GET_EXPIRING_PRODUCTS_BY_ID, productRowMapper, days, userId);
            products = new ArrayList<>(results);

            for (Product product : products) {
                Integer productId = product.getProductId();

                if (productId != 0) { // if product id is available
                    Optional<Image> image = getProductImage(productId);
                    if (!image.isEmpty() && image.get() != null) {
                        product.setProductImage(image.get());
                    }
                    Optional<ArrayList<Tag>> tags = getProductTags(productId);
                    if (!tags.isEmpty() && tags.get() != null) {
                        product.setProductTags(new ArrayList<>(tags.get()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.of(products);
    }

    @Override
    public Integer countExpiringProducts(Integer userId, Integer expDays){
        Integer count = 0;
        try{
            count = jdbcTemplate.queryForObject(SQL_COUNT_EXPIRING_PRODUCTS_BY_ID, Integer.class, expDays, userId);
        }catch(Exception e){
            e.printStackTrace();
        }
        return count;
    }

    @Override
    public Integer getInventoryListId(Integer userId) {
        Integer id = 0;
        try {
            id = jdbcTemplate.queryForObject(SQL_GET_INVENTORYLIST_BY_ID, Integer.class, new Object[] { userId });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public Optional<Image> getProductImage(Integer productId) {
        Image image = null;
        try {
            image = jdbcTemplate.queryForObject(SQL_GET_PRODUCT_IMAGE_BY_ID, imageRowMapper, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.of(image);
    }

    @Override
    public Optional<ArrayList<Tag>> getProductTags(Integer productId) {
        List<Tag> tags = new ArrayList<>();
        try {
            tags = jdbcTemplate.query(SQL_GET_PRODUCT_TAGS, tagRowMapper, productId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return Optional.of(new ArrayList<>(tags));
    }

    @Override
    public Optional<List<Integer>> getAllTagsByProductId(Integer productId) {
        List<Integer> tagIds = new ArrayList<>();
        try {
            List<Tag> tags = jdbcTemplate.query(SQL_GET_TAG_IDS, tagRowMapper, productId);
            for (Tag tag : tags) {
                tagIds.add(tag.getTagId());
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return Optional.of(tagIds);
    }

    /**
     * Set FK to null before product can be deleted.
     */
    @Override
    public Integer removeProductFromInventoryList(Integer inventoryId, Integer productId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_PRODUCT_ON_INVENTORYLIST, inventoryId, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteProductOnInventoryList(Integer productId, Integer inventoryId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_PRODUCT, productId, inventoryId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteDietryTagById(Integer tagId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_TAG, tagId);
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteProductTagById(Integer productId, Integer tagId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_PRODUCT_TAG, productId, tagId);
        } catch (

        Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer deleteProductImage(Integer productId) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_DELETE_PRODUCT_IMAGE, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Product> getProductWithShortExpDate(Integer userId) {
        try {
            List<Product> products = jdbcTemplate.query(SQL_GET_PRODUCT_WITH_SHORT_EXPDATE, productRowMapper, userId);
            return products;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Tag> getAllProductTags(Integer userId) {
        try {
            List<Tag> tags = jdbcTemplate.query(SQL_GET_ALL_PRODUCT_TAGS, tagRowMapper, userId);
            return tags;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer getCurrentShoppinglistId(Integer userId) {
        return jdbcTemplate.queryForObject(SQL_GET_CURRENT_SHOPPINGLIST_BY_ID, Integer.class, new Object[] { userId });
    }

    @Override
    public Integer getOldShoppinglistId(Integer userId) {
        return jdbcTemplate.queryForObject(SQL_GET_OLD_SHOPPINGLIST_BY_ID, Integer.class, new Object[] { userId });
    }

    /**
     * Converts the base64 string to byte array.
     * 
     * @param base64Str
     * @return
     */
    private byte[] convertBase64StringToBytes(String base64Str) {
        // convert base64 string back to byte arrays
        byte[] byteArray = null;
        try {
            if (base64Str != null && !base64Str.isEmpty()) {
                byteArray = ImageUtils.base64ToBytes(base64Str); // from object input stream read to map
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    /**
     * Converts the base64 string to byte array.
     * 
     * @param "base64Str"
     * @return
     */
    private String convertBytesToBase64String(byte[] byteArray) {
        // convert base64 string back to byte arrays
        String base64Str = null;
        try {
            if (byteArray != null && byteArray.length != 0) {
                base64Str = ImageUtils.bytesToBase64(byteArray); // from object input stream read to map
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64Str;
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

    private RowMapper<Image> imageRowMapper = ((rs, rowNum) -> {
        byte[] imgBytes = rs.getBytes("IMAGE_BYTES");
        // encode byte array retrieved from DB to base64 encoded string
        String base64Str = convertBytesToBase64String(imgBytes);
        return new Image(
                rs.getInt("IMAGE_ID"),
                rs.getString("IMAGE_NAME"),
                rs.getString("IMAGE_URL"),
                base64Str);
    });

    private RowMapper<Tag> tagRowMapper = ((rs, rowNum) -> {
        return new Tag(
                rs.getInt("TAG_ID"),
                rs.getString("LABEL"));
    });

}