package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.common.utils.ImageUtils;
import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.mindrot.jbcrypt.BCrypt;
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
public class UserRepositoryImpl implements UserRepository {

    // Create Statements
    private static final String SQL_CREATE_USER = "INSERT INTO ET_USERS(" +
            "USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, IS_LOGGED_IN) " +
            "VALUES(NEXTVAL('ET_USERS_SEQ'), ?, ?, ?, ?, ?)";
    private static final String SQL_CREATE_USER_IMAGE = "INSERT INTO ET_USER_IMAGE("+
            "IMAGE_ID, USER_ID, IMAGE_NAME, IMAGE_URL) " +
            "VALUES(NEXTVAL('ET_USER_IMAGE_SEQ'), ?, ?, ?)";
    private static final String SQL_CREATE_USER_PROFILE_SETTINGS = "INSERT INTO ET_PROFILE_SETTINGS(" +
            "SETTINGS_ID, USER_ID, REMINDER_PRODUCT_EXPIRATION, SUGGEST_RECIPES, PUSH_NOTIFICATIONS) " +
            "VALUES(NEXTVAL('ET_PROFILE_SETTINGS_SEQ'), ?, ?, ?, ?)";
    private static final String SQL_CREATE_USER_DIETARY_TAG = "INSERT INTO ET_DIETRY_TAG(TAG_ID, LABEL) " +
            "VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?)";
    private static final String SQL_CREATE_USER_TAG = "INSERT INTO ET_USER_TAG(USER_ID, TAG_ID) " + // this is a compound table
            "VALUES(?, ?)";
    private static final String SQL_CREATE_USER_INVENTORY_LIST = "INSERT INTO ET_INVENTORYLIST(INVENTORYLIST_ID, USER_ID) " +
            "VALUES(NEXTVAL('ET_INVENTORYLIST_SEQ'), ?)";
    private static final String SQL_CREATE_USER_SHOPPINGLIST = "INSERT INTO ET_SHOPPINGLIST(SHOPPINGLIST_ID) " +
            "VALUES(NEXTVAL('ET_SHOPPINGLIST_SEQ'))";
    private static final String SQL_CREATE_USER_CUR_SHOPPINGLIST = "INSERT INTO ET_CURRENT_SHOPPINGLIST("+
            "CUR_SHOPPINGLIST_ID, SHOPPINGLIST_ID, USER_ID) " +
            "VALUES(NEXTVAL('ET_CURRENT_SHOPPINGLIST_SEQ'), ?, ?)";
    private static final String SQL_CREATE_USER_OLD_SHOPPINGLIST = "INSERT INTO ET_OLD_SHOPPINGLIST("+
            "OLD_SHOPPINGLIST_ID, SHOPPINGLIST_ID, USER_ID) " +
            "VALUES(NEXTVAL('ET_OLD_SHOPPINGLIST_SEQ'), ?, ?)";

    // Update Statements
    private static final String SQL_UPDATE_ALLOW_PUSH = "UPDATE ET_PROFILE_SETTINGS SET " + 
            "PUSH_NOTIFICATIONS = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_ALLOW_REMINDER = "UPDATE ET_PROFILE_SETTINGS SET " +
            "REMINDER_PRODUCT_EXPIRATION = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_ALLOW_SUGGESTION = "UPDATE ET_PROFILE_SETTINGS SET SUGGEST_RECIPES = ? " +
            "WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_DIETRY_TAG = "UPDATE ET_DIETRY_TAG SET LABEL = ? WHERE TAG_ID = " +
            "(SELECT TAG_ID FROM ET_USER_TAG WHERE USER_ID = ?);";
    private static final String SQL_UPDATE_USER_LOGGEDIN = "UPDATE ET_USERS SET IS_LOGGED_IN = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_USER_INFORMATION = "UPDATE ET_USERS SET FIRST_NAME = ?, " +
            "LAST_NAME = ? , EMAIL = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_USER_IMAGE = "UPDATE ET_USER_IMAGE SET IMAGE_URL = ?, " +
            "IMAGE_BYTES = ? WHERE IMAGE_ID = ?;";

    // Delete Statements

    // Select Statements
    private static final String SQL_COUNT_BY_EMAIL = "SELECT COUNT(*) FROM ET_USERS WHERE EMAIL = ?";
    private static final String SQL_FIND_USER_BY_ID = "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD " +
            "FROM ET_USERS WHERE USER_ID = ?";
    private static final String SQL_FIND_BY_EMAIL = "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD " +
            "FROM ET_USERS WHERE EMAIL = ?";
    private static final String SQL_FIND_LOGIN_STATUS = "SELECT IS_LOGGED_IN FROM ET_USERS WHERE USER_ID = ?";
    private static final String SQL_FIND_USER_IMAGE_BY_ID = "SELECT IMAGE_ID, IMAGE_NAME, IMAGE_URL, IMAGE_BYTES " +
            "FROM ET_USER_IMAGE WHERE USER_ID = ?";
    private static final String SQL_FIND_USER_SETTINGS_BY_ID = "SELECT SETTINGS_ID, REMINDER_PRODUCT_EXPIRATION, " +
            "SUGGEST_RECIPES, PUSH_NOTIFICATIONS " +
            "FROM ET_PROFILE_SETTINGS WHERE USER_ID = ?";
    private static final String SQL_GET_USER_DIETRY_TAGS = "SELECT * FROM ET_USERS INNER JOIN ET_USER_TAG  " +
            "ON ET_USERS.USER_ID = ET_USER_TAG.USER_ID INNER JOIN ET_DIETRY_TAG ON " +
            "ET_USER_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID WHERE ET_USERS.USER_ID = ?;";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public Integer createUser(String firstName, String lastName, String email, String password) throws FTAuthException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, hashedPassword);
                ps.setBoolean(5, false);
                return ps;
            }, keyHolder);
            return (Integer) keyHolder.getKeys().get("USER_ID");
        } catch (Exception e) {
            throw new FTAuthException("Invalid details. Failed to create account");
        }
    }

    @Override
    public Integer createUserImage(Integer userId) {
        // Set default values when user is created
        String imageName = "Profilbild";
        String imageUrl = "";

        Integer imgId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_IMAGE,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setString(2, imageName);
                ps.setString(3, imageUrl);
                return ps;
            }, keyHolder);
            imgId = (Integer) keyHolder.getKeys().get("IMAGE_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return imgId;
    }

    @Override
    public Integer createProfileSettings(Integer userId) {

        // Set default values when user is created
        Integer settingsId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_PROFILE_SETTINGS,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                ps.setBoolean(2, false);
                ps.setBoolean(3, false);
                ps.setBoolean(4, false);
                return ps;
            }, keyHolder);
            settingsId = (Integer) keyHolder.getKeys().get("SETTINGS_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return settingsId;
    }

    @Override
    public Integer createDietryTag(String LABEL) {
        Integer tagId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_DIETARY_TAG,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, LABEL);
                return ps;
            }, keyHolder);
            tagId = (Integer) keyHolder.getKeys().get("TAG_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return tagId;
    }

    @Override
    public Integer createUserTag(Integer userId, Tag dietryTag, Integer tagId) {
        // Create compound table, doesnt have an own id only PK/FKs
        Integer result = 0;
        try {
            // second create compound table with user id + tag id
            result = jdbcTemplate.update(SQL_CREATE_USER_TAG, userId, tagId);

        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer createInventoryList(Integer userId) {
        // Set default values when user is created
        Integer invId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_INVENTORY_LIST,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, userId);
                return ps;
            }, keyHolder);
            invId = (Integer) keyHolder.getKeys().get("INVENTORYLIST_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return invId;
    }

    @Override
    public Integer createShoppingList(Integer userId) {
        // Set default values when user is created
        // reflects the IS-A relationship in the DB
        Integer shopId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                return connection.prepareStatement(SQL_CREATE_USER_SHOPPINGLIST,
                        Statement.RETURN_GENERATED_KEYS);
            }, keyHolder);
            shopId = (Integer) keyHolder.getKeys().get("SHOPPINGLIST_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return shopId;
    }

    @Override
    public Integer createCurrentShoppingList(Integer shopId, Integer userId) {
        // Set default values when user is created
        // reflects the IS-A relationship in the DB
        Integer curShopId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_CUR_SHOPPINGLIST,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, shopId);
                ps.setInt(2, userId);
                return ps;
            }, keyHolder);
            curShopId = (Integer) keyHolder.getKeys().get("CUR_SHOPPINGLIST_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return curShopId;
    }

    @Override
    public Integer createOldShoppingList(Integer shopId, Integer userId) {
        // Set default values when user is created
        // reflects the IS-A relationship in the DB
        Integer oldShopId = 0;
        try {
            // Keyholdes
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE_USER_OLD_SHOPPINGLIST,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, shopId);
                ps.setInt(2, userId);
                return ps;
            }, keyHolder);
            oldShopId = (Integer) keyHolder.getKeys().get("CUR_SHOPPINGLIST_ID");
        } catch (Exception e) {
            e.getStackTrace();
        }
        return oldShopId;
    }

    @Override
    public User findByEmailAndPassword(String email, String password) throws FTAuthException {
        try {
            User user = jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, userRowMapper, new Object[] { email });
            if (!BCrypt.checkpw(password, user.getPassword()))
                throw new FTAuthException("Invalid EMAIL/PASSWORD");
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new FTAuthException("Invalid EMAIL/PASSWORD");
        }
    }

    @Override
    public User existsByEmail(String email){
        User user = null;
        try {
            user = jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, userRowMapper, new Object[] { email });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Integer getCountByEmail(String email) {
        Integer count = 0;
        try {
            count = jdbcTemplate.queryForObject(SQL_COUNT_BY_EMAIL, Integer.class, new Object[] { email });
        } catch (Exception e) {
            e.getStackTrace();
        }
        return count;
    }

    @Override
    public Settings getUserSettingsById(int userId) {
        Settings settings = null;
        try{
            settings = jdbcTemplate.queryForObject(SQL_FIND_USER_SETTINGS_BY_ID, settingsRowMapper, new Object[] { userId });
        } catch (Exception e) {
            e.getStackTrace();
        }
        return settings;
    }

    @Override
    public Image getImageById(int userId) {
        Image image = null;
        try{
            image = jdbcTemplate.queryForObject(SQL_FIND_USER_IMAGE_BY_ID, imageRowMapper, new Object[] { userId });
        } catch (Exception e) {
            e.getStackTrace();
        }
        return image;
    }

    @Override
    public User getUserById(int userId) {
        User result = null;
        try {
            result = jdbcTemplate.queryForObject(SQL_FIND_USER_BY_ID, userRowMapper, new Object[] { userId });
            return result;
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Optional<ArrayList<Tag>> getUserDietryTags(Integer userId) {
        List<Tag> tags = new ArrayList<>();
        try {
            tags = jdbcTemplate.query(SQL_GET_USER_DIETRY_TAGS, tagRowMapper, userId);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return Optional.of(new ArrayList<>(tags));
    }

    @Override
    public Boolean isUserLoggedIn(Integer userId) {
        Boolean isloggedIn = false;
        try {
            isloggedIn = jdbcTemplate.queryForObject(SQL_FIND_LOGIN_STATUS, Boolean.class, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isloggedIn;
    }

    @Override
    public Integer allowPushNotification(int id, boolean allow) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_ALLOW_PUSH, allow, id);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer allowReminder(int id, boolean allow) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_ALLOW_REMINDER, allow, id);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer allowSuggestion(int id, boolean allow) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_ALLOW_SUGGESTION, allow, id);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateDietryTag(int id, String type) {
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_DIETRY_TAG, type, id);
        } catch (Exception e) {
            e.getStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateUserInformation(String firstName, String lastName, String email, Integer userId) {
        // Update user information
        Integer result = 0;
        try {
            result = jdbcTemplate.update(SQL_UPDATE_USER_INFORMATION, firstName, lastName, email, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateUserImage(Image image) {
        // Update image information
        Integer result = 0;
        try {
            byte[] imageBytes = convertBase64StringToBytes(image.getBase64ImageString());
            result = jdbcTemplate.update(SQL_UPDATE_USER_IMAGE, image.getImageUrl(), imageBytes, image.getImageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Integer updateUserLoggedIn(int userId, boolean loggedIn) {
        // Log user in or log user out
        Integer result = 0;
        try{
            result = jdbcTemplate.update(SQL_UPDATE_USER_LOGGEDIN, loggedIn, userId);
            } catch (Exception e) {
                e.printStackTrace();
        }
        return result;
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
     * @param base64Str
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


    private RowMapper<User> userRowMapper = ((rs, rowNum) -> {
        return new User(rs.getInt("USER_ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"));
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

    private RowMapper<Settings> settingsRowMapper = ((rs, rowNum) -> {
        return new Settings(
                rs.getInt("SETTINGS_ID"),
                rs.getBoolean("PUSH_NOTIFICATIONS"),
                rs.getBoolean("REMINDER_PRODUCT_EXPIRATION"),
                rs.getBoolean("SUGGEST_RECIPES"));
    });

    private RowMapper<Tag> tagRowMapper = ((rs, rowNum) -> {
        return new Tag(
                rs.getInt("TAG_ID"),
                rs.getString("LABEL"));
    });
}
