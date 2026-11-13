package com.foodtracker.foodtrackerapi.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.common.utils.Attempt;
import com.foodtracker.foodtrackerapi.common.utils.ImageUtils;
import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * <p>
 * Reads and writes the user data, the profile image, the notification settings
 * and the food preferences. The class also makes the empty lists that a new
 * user needs.
 * </p>
 *
 * @author Janina Mattes
 */
@Repository
@Transactional
public class UserRepositoryImpl extends AbstractJdbcRepository implements UserRepository {

    /** Number of rounds that BCrypt uses to make the password hash value. */
    private static final int BCRYPT_ROUNDS = 10;

    /** Name of the profile image that a new user gets. */
    private static final String DEFAULT_IMAGE_NAME = "Profilbild";

    // Create statements
    private static final String SQL_CREATE_USER = "INSERT INTO ET_USERS("
            + "USER_ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, IS_LOGGED_IN) "
            + "VALUES(NEXTVAL('ET_USERS_SEQ'), ?, ?, ?, ?, ?)";
    private static final String SQL_CREATE_USER_IMAGE = "INSERT INTO ET_USER_IMAGE("
            + "IMAGE_ID, USER_ID, IMAGE_NAME, IMAGE_URL) "
            + "VALUES(NEXTVAL('ET_USER_IMAGE_SEQ'), ?, ?, ?)";
    private static final String SQL_CREATE_USER_PROFILE_SETTINGS = "INSERT INTO ET_PROFILE_SETTINGS("
            + "SETTINGS_ID, USER_ID, REMINDER_PRODUCT_EXPIRATION, SUGGEST_RECIPES, PUSH_NOTIFICATIONS) "
            + "VALUES(NEXTVAL('ET_PROFILE_SETTINGS_SEQ'), ?, ?, ?, ?)";
    private static final String SQL_CREATE_USER_DIETARY_TAG = "INSERT INTO ET_DIETRY_TAG(TAG_ID, LABEL) "
            + "VALUES(NEXTVAL('ET_DIETRY_TAG_SEQ'), ?)";
    // Compound table. It holds foreign keys only.
    private static final String SQL_CREATE_USER_TAG = "INSERT INTO ET_USER_TAG(USER_ID, TAG_ID) "
            + "VALUES(?, ?)";
    private static final String SQL_CREATE_USER_INVENTORY_LIST = "INSERT INTO ET_INVENTORYLIST("
            + "INVENTORYLIST_ID, USER_ID) VALUES(NEXTVAL('ET_INVENTORYLIST_SEQ'), ?)";
    private static final String SQL_CREATE_USER_SHOPPINGLIST = "INSERT INTO ET_SHOPPINGLIST(SHOPPINGLIST_ID) "
            + "VALUES(NEXTVAL('ET_SHOPPINGLIST_SEQ'))";
    private static final String SQL_CREATE_USER_CUR_SHOPPINGLIST = "INSERT INTO ET_CURRENT_SHOPPINGLIST("
            + "CUR_SHOPPINGLIST_ID, SHOPPINGLIST_ID, USER_ID) "
            + "VALUES(NEXTVAL('ET_CURRENT_SHOPPINGLIST_SEQ'), ?, ?)";
    private static final String SQL_CREATE_USER_OLD_SHOPPINGLIST = "INSERT INTO ET_OLD_SHOPPINGLIST("
            + "OLD_SHOPPINGLIST_ID, SHOPPINGLIST_ID, USER_ID) "
            + "VALUES(NEXTVAL('ET_OLD_SHOPPINGLIST_SEQ'), ?, ?)";

    // Update statements
    private static final String SQL_UPDATE_ALLOW_PUSH = "UPDATE ET_PROFILE_SETTINGS SET "
            + "PUSH_NOTIFICATIONS = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_ALLOW_REMINDER = "UPDATE ET_PROFILE_SETTINGS SET "
            + "REMINDER_PRODUCT_EXPIRATION = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_ALLOW_SUGGESTION = "UPDATE ET_PROFILE_SETTINGS SET "
            + "SUGGEST_RECIPES = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_DIETRY_TAG = "UPDATE ET_DIETRY_TAG SET LABEL = ? WHERE TAG_ID = "
            + "(SELECT TAG_ID FROM ET_USER_TAG WHERE USER_ID = ?);";
    private static final String SQL_UPDATE_USER_LOGGEDIN = "UPDATE ET_USERS SET IS_LOGGED_IN = ? "
            + "WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_USER_INFORMATION = "UPDATE ET_USERS SET FIRST_NAME = ?, "
            + "LAST_NAME = ? , EMAIL = ? WHERE USER_ID = ?;";
    private static final String SQL_UPDATE_USER_IMAGE = "UPDATE ET_USER_IMAGE SET IMAGE_URL = ?, "
            + "IMAGE_BYTES = ? WHERE IMAGE_ID = ?;";

    // Select statements
    private static final String SQL_COUNT_BY_EMAIL = "SELECT COUNT(*) FROM ET_USERS "
            + "WHERE EMAIL = ?";
    private static final String SQL_FIND_USER_BY_ID = "SELECT USER_ID, FIRST_NAME, LAST_NAME, "
            + "EMAIL, PASSWORD FROM ET_USERS WHERE USER_ID = ?";
    private static final String SQL_FIND_BY_EMAIL = "SELECT USER_ID, FIRST_NAME, LAST_NAME, "
            + "EMAIL, PASSWORD FROM ET_USERS WHERE EMAIL = ?";
    private static final String SQL_FIND_LOGIN_STATUS = "SELECT IS_LOGGED_IN FROM ET_USERS WHERE USER_ID = ?";
    private static final String SQL_FIND_USER_IMAGE_BY_ID = "SELECT IMAGE_ID, IMAGE_NAME, "
            + "IMAGE_URL, IMAGE_BYTES FROM ET_USER_IMAGE WHERE USER_ID = ?";
    private static final String SQL_FIND_USER_SETTINGS_BY_ID = "SELECT SETTINGS_ID, "
            + "REMINDER_PRODUCT_EXPIRATION, SUGGEST_RECIPES, PUSH_NOTIFICATIONS "
            + "FROM ET_PROFILE_SETTINGS WHERE USER_ID = ?";
    private static final String SQL_GET_USER_DIETRY_TAGS = "SELECT * FROM ET_USERS INNER JOIN ET_USER_TAG "
            + "ON ET_USERS.USER_ID = ET_USER_TAG.USER_ID INNER JOIN ET_DIETRY_TAG ON "
            + "ET_USER_TAG.TAG_ID = ET_DIETRY_TAG.TAG_ID WHERE ET_USERS.USER_ID = ?;";

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getInt("USER_ID"),
            rs.getString("FIRST_NAME"),
            rs.getString("LAST_NAME"),
            rs.getString("EMAIL"),
            rs.getString("PASSWORD"));

    private final RowMapper<Image> imageRowMapper = (rs, rowNum) -> new Image(
            rs.getInt("IMAGE_ID"),
            rs.getString("IMAGE_NAME"),
            rs.getString("IMAGE_URL"),
            ImageUtils.bytesToBase64(rs.getBytes("IMAGE_BYTES")));

    private final RowMapper<Settings> settingsRowMapper = (rs, rowNum) -> new Settings(
            rs.getInt("SETTINGS_ID"),
            rs.getBoolean("PUSH_NOTIFICATIONS"),
            rs.getBoolean("REMINDER_PRODUCT_EXPIRATION"),
            rs.getBoolean("SUGGEST_RECIPES"));

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> new Tag(
            rs.getInt("TAG_ID"),
            rs.getString("LABEL"));

    /**
     * Makes the repository.
     *
     * @param jdbcTemplate connection to the database
     */
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Integer createUser(String firstName, String lastName, String email, String password)
            throws FTAuthException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        Integer userId = insertAndGetKey("create a user", SQL_CREATE_USER, "USER_ID", statement -> {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, hashedPassword);
            statement.setBoolean(5, false);
        });
        if (NO_ID.equals(userId)) {
            throw new FTAuthException("Invalid details. Failed to create account");
        }
        return userId;
    }

    @Override
    public Integer createUserImage(Integer userId) {
        return insertAndGetKey("create the profile image", SQL_CREATE_USER_IMAGE, "IMAGE_ID",
                statement -> {
                    statement.setInt(1, userId);
                    statement.setString(2, DEFAULT_IMAGE_NAME);
                    statement.setString(3, "");
                });
    }

    @Override
    public Integer createProfileSettings(Integer userId) {
        // A new user has given no permission yet, so all settings are false.
        return insertAndGetKey("create the profile settings", SQL_CREATE_USER_PROFILE_SETTINGS,
                "SETTINGS_ID", statement -> {
                    statement.setInt(1, userId);
                    statement.setBoolean(2, false);
                    statement.setBoolean(3, false);
                    statement.setBoolean(4, false);
                });
    }

    @Override
    public Integer createDietryTag(String label) {
        return insertAndGetKey("create a dietary tag", SQL_CREATE_USER_DIETARY_TAG, "TAG_ID",
                statement -> statement.setString(1, label));
    }

    @Override
    public Integer createUserTag(Integer userId, Tag dietryTag, Integer tagId) {
        return update("link the dietary tag to the user", SQL_CREATE_USER_TAG, userId, tagId);
    }

    @Override
    public Integer createInventoryList(Integer userId) {
        return insertAndGetKey("create the inventory list", SQL_CREATE_USER_INVENTORY_LIST,
                "INVENTORYLIST_ID", statement -> statement.setInt(1, userId));
    }

    @Override
    public Integer createShoppingList(Integer userId) {
        // The table holds the key only. It reflects the gen-spec pattern of the
        // database.
        return insertAndGetKey("create the shopping list", SQL_CREATE_USER_SHOPPINGLIST,
                "SHOPPINGLIST_ID", statement -> {
                    // The statement has no values.
                });
    }

    @Override
    public Integer createCurrentShoppingList(Integer shopId, Integer userId) {
        return insertAndGetKey("create the current shopping list", SQL_CREATE_USER_CUR_SHOPPINGLIST,
                "CUR_SHOPPINGLIST_ID", statement -> {
                    statement.setInt(1, shopId);
                    statement.setInt(2, userId);
                });
    }

    @Override
    public Integer createOldShoppingList(Integer shopId, Integer userId) {
        return insertAndGetKey("create the old shopping list", SQL_CREATE_USER_OLD_SHOPPINGLIST,
                "OLD_SHOPPINGLIST_ID", statement -> {
                    statement.setInt(1, shopId);
                    statement.setInt(2, userId);
                });
    }

    @Override
    public User findByEmailAndPassword(String email, String password) throws FTAuthException {
        try {
            User user = jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, userRowMapper, email);
            if (user == null || password == null || !BCrypt.checkpw(password, user.getPassword())) {
                throw new FTAuthException("Invalid EMAIL/PASSWORD");
            }
            return user;
        } catch (EmptyResultDataAccessException e) {
            throw new FTAuthException("Invalid EMAIL/PASSWORD");
        }
    }

    @Override
    public User existsByEmail(String email) {
        return Attempt.orDefault("search for a user by email", null,
                () -> jdbcTemplate.queryForObject(SQL_FIND_BY_EMAIL, userRowMapper, email));
    }

    @Override
    public Integer getCountByEmail(String email) {
        return queryForId("count the users with this email", SQL_COUNT_BY_EMAIL, email);
    }

    @Override
    public Settings getUserSettingsById(int userId) {
        return Attempt.orDefault("read the profile settings", null,
                () -> jdbcTemplate.queryForObject(SQL_FIND_USER_SETTINGS_BY_ID, settingsRowMapper,
                        userId));
    }

    @Override
    public Image getImageById(int userId) {
        return Attempt.orDefault("read the profile image", null,
                () -> jdbcTemplate.queryForObject(SQL_FIND_USER_IMAGE_BY_ID, imageRowMapper,
                        userId));
    }

    @Override
    public User getUserById(int userId) {
        return Attempt.orDefault("read the user", null,
                () -> jdbcTemplate.queryForObject(SQL_FIND_USER_BY_ID, userRowMapper, userId));
    }

    @Override
    public Optional<ArrayList<Tag>> getUserDietryTags(Integer userId) {
        List<Tag> tags = Attempt.orDefault("read the food preferences", new ArrayList<>(),
                () -> jdbcTemplate.query(SQL_GET_USER_DIETRY_TAGS, tagRowMapper, userId));
        return Optional.of(new ArrayList<>(tags));
    }

    @Override
    public Boolean isUserLoggedIn(Integer userId) {
        return Attempt.orDefault("read the login status", false,
                () -> jdbcTemplate.queryForObject(SQL_FIND_LOGIN_STATUS, Boolean.class, userId));
    }

    @Override
    public Integer allowPushNotification(int userId, boolean allow) {
        return update("set the push notification permission", SQL_UPDATE_ALLOW_PUSH, allow, userId);
    }

    @Override
    public Integer allowReminder(int userId, boolean allow) {
        return update("set the reminder permission", SQL_UPDATE_ALLOW_REMINDER, allow, userId);
    }

    @Override
    public Integer allowSuggestion(int userId, boolean allow) {
        return update("set the suggestion permission", SQL_UPDATE_ALLOW_SUGGESTION, allow, userId);
    }

    @Override
    public Integer updateDietryTag(int userId, String type) {
        return update("change the dietary tag", SQL_UPDATE_DIETRY_TAG, type, userId);
    }

    @Override
    public Integer updateUserInformation(String firstName, String lastName, String email,
            Integer userId) {
        return update("change the user data", SQL_UPDATE_USER_INFORMATION, firstName, lastName,
                email, userId);
    }

    @Override
    public Integer updateUserImage(Image image) {
        byte[] imageBytes = ImageUtils.base64ToBytes(image.getBase64ImageString());
        return update("change the profile image", SQL_UPDATE_USER_IMAGE, image.getImageUrl(),
                imageBytes, image.getImageId());
    }

    @Override
    public Integer updateUserLoggedIn(int userId, boolean loggedIn) {
        return update("change the login status", SQL_UPDATE_USER_LOGGEDIN, loggedIn, userId);
    }
}
