package com.foodtracker.foodtrackerapi.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.EmptyResultDataAccessException;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * Tests for {@link UserRepositoryImpl}.
 *
 * @author Janina Mattes
 */
class UserRepositoryImplTest {

    private RecordingJdbcTemplate jdbc;
    private UserRepositoryImpl repository;

    private static User storedUser(String clearPassword) {
        return new User(1, "Janina", "Mattes", "a@b.c",
                BCrypt.hashpw(clearPassword, BCrypt.gensalt(4)));
    }

    @BeforeEach
    void setUp() {
        jdbc = new RecordingJdbcTemplate();
        repository = new UserRepositoryImpl(jdbc);
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("gives the new key and stores the password as a hash value")
        void givesNewKeyAndHashesPassword() throws Exception {
            jdbc.givesKey("USER_ID", 42);

            Integer userId = repository.createUser("Janina", "Mattes", "a@b.c", "secret");

            assertThat(userId).isEqualTo(42);
            PreparedStatement statement = jdbc.statement();
            verify(statement).setString(1, "Janina");
            verify(statement).setString(2, "Mattes");
            verify(statement).setString(3, "a@b.c");
            verify(statement).setBoolean(5, false);

            ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
            verify(statement).setString(eq(4), hash.capture());
            assertThat(hash.getValue()).isNotEqualTo("secret");
            assertThat(BCrypt.checkpw("secret", hash.getValue())).isTrue();
        }

        @Test
        @DisplayName("throws when the database gives no key")
        void throwsWhenDatabaseGivesNoKey() {
            jdbc.givesKeys(Collections.emptyMap());

            assertThatThrownBy(() -> repository.createUser("A", "B", "a@b.c", "secret"))
                    .isInstanceOf(FTAuthException.class)
                    .hasMessageContaining("Failed to create account");
        }

        @Test
        @DisplayName("throws when the database is not available")
        void throwsWhenDatabaseIsNotAvailable() {
            jdbc.fails();

            assertThatThrownBy(() -> repository.createUser("A", "B", "a@b.c", "secret"))
                    .isInstanceOf(FTAuthException.class);
        }
    }

    @Nested
    @DisplayName("The lists and the settings of a new user")
    class DefaultValues {

        @Test
        @DisplayName("createUserImage stores the default name of the profile image")
        void createUserImageStoresDefaultName() throws Exception {
            jdbc.givesKey("IMAGE_ID", 7);

            assertThat(repository.createUserImage(1)).isEqualTo(7);
            verify(jdbc.statement()).setInt(1, 1);
            verify(jdbc.statement()).setString(2, "Profilbild");
            verify(jdbc.statement()).setString(3, "");
        }

        @Test
        @DisplayName("createProfileSettings gives no permission")
        void createProfileSettingsGivesNoPermission() throws Exception {
            jdbc.givesKey("SETTINGS_ID", 3);

            assertThat(repository.createProfileSettings(1)).isEqualTo(3);
            verify(jdbc.statement()).setInt(1, 1);
            verify(jdbc.statement()).setBoolean(2, false);
            verify(jdbc.statement()).setBoolean(3, false);
            verify(jdbc.statement()).setBoolean(4, false);
        }

        @Test
        @DisplayName("createInventoryList gives the new key")
        void createInventoryListGivesNewKey() throws Exception {
            jdbc.givesKey("INVENTORYLIST_ID", 5);

            assertThat(repository.createInventoryList(1)).isEqualTo(5);
            verify(jdbc.statement()).setInt(1, 1);
        }

        @Test
        @DisplayName("createShoppingList gives the new key")
        void createShoppingListGivesNewKey() {
            jdbc.givesKey("SHOPPINGLIST_ID", 9);

            assertThat(repository.createShoppingList(1)).isEqualTo(9);
        }

        @Test
        @DisplayName("createCurrentShoppingList gives the new key")
        void createCurrentShoppingListGivesNewKey() throws Exception {
            jdbc.givesKey("CUR_SHOPPINGLIST_ID", 11);

            assertThat(repository.createCurrentShoppingList(9, 1)).isEqualTo(11);
            verify(jdbc.statement()).setInt(1, 9);
            verify(jdbc.statement()).setInt(2, 1);
        }

        @Test
        @DisplayName("createOldShoppingList reads the key of the old shopping list")
        void createOldShoppingListReadsItsOwnKey() throws Exception {
            // The class read the key CUR_SHOPPINGLIST_ID before. The method
            // therefore always gave the key of the current shopping list.
            Map<String, Object> keys = new HashMap<>();
            keys.put("OLD_SHOPPINGLIST_ID", 13);
            keys.put("CUR_SHOPPINGLIST_ID", 11);
            jdbc.givesKeys(keys);

            assertThat(repository.createOldShoppingList(9, 1)).isEqualTo(13);
            verify(jdbc.statement()).setInt(1, 9);
            verify(jdbc.statement()).setInt(2, 1);
        }

        @Test
        @DisplayName("createDietryTag gives the new key")
        void createDietryTagGivesNewKey() throws Exception {
            jdbc.givesKey("TAG_ID", 2);

            assertThat(repository.createDietryTag("Vegan")).isEqualTo(2);
            verify(jdbc.statement()).setString(1, "Vegan");
        }

        @Test
        @DisplayName("An insert that fails gives the key 0")
        void failedInsertGivesZero() {
            jdbc.fails();

            assertThat(repository.createUserImage(1)).isZero();
            assertThat(repository.createProfileSettings(1)).isZero();
            assertThat(repository.createInventoryList(1)).isZero();
            assertThat(repository.createShoppingList(1)).isZero();
            assertThat(repository.createCurrentShoppingList(9, 1)).isZero();
            assertThat(repository.createOldShoppingList(9, 1)).isZero();
            assertThat(repository.createDietryTag("Vegan")).isZero();
        }

        @Test
        @DisplayName("An insert gives the key 0 when the database gives no key")
        void insertWithoutKeyGivesZero() {
            jdbc.givesKeys(Collections.singletonMap("OTHER_ID", "not-a-number"));

            assertThat(repository.createUserImage(1)).isZero();
        }
    }

    @Nested
    @DisplayName("findByEmailAndPassword")
    class FindByEmailAndPassword {

        @Test
        @DisplayName("gives the user when the password fits the hash value")
        void givesUserForCorrectPassword() {
            User stored = storedUser("secret");
            jdbc.givesObject(stored);

            assertThat(repository.findByEmailAndPassword("a@b.c", "secret")).isSameAs(stored);
            assertThat(jdbc.lastArgs()).containsExactly("a@b.c");
        }

        @Test
        @DisplayName("throws when the password is wrong")
        void throwsForWrongPassword() {
            jdbc.givesObject(storedUser("secret"));

            assertThatThrownBy(() -> repository.findByEmailAndPassword("a@b.c", "wrong"))
                    .isInstanceOf(FTAuthException.class)
                    .hasMessage("Invalid EMAIL/PASSWORD");
        }

        @Test
        @DisplayName("throws when the password is null")
        void throwsForNullPassword() {
            jdbc.givesObject(storedUser("secret"));

            assertThatThrownBy(() -> repository.findByEmailAndPassword("a@b.c", null))
                    .isInstanceOf(FTAuthException.class);
        }

        @Test
        @DisplayName("throws when the email address is not in the database")
        void throwsForUnknownEmail() {
            jdbc.failsWith(new EmptyResultDataAccessException(1));

            assertThatThrownBy(() -> repository.findByEmailAndPassword("a@b.c", "secret"))
                    .isInstanceOf(FTAuthException.class);
        }

        @Test
        @DisplayName("throws when the database gives no user")
        void throwsWhenDatabaseGivesNoUser() {
            jdbc.givesObject(null);

            assertThatThrownBy(() -> repository.findByEmailAndPassword("a@b.c", "secret"))
                    .isInstanceOf(FTAuthException.class);
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("existsByEmail gives the user of the email address")
        void existsByEmailGivesUser() {
            User stored = storedUser("secret");
            jdbc.givesObject(stored);

            assertThat(repository.existsByEmail("a@b.c")).isSameAs(stored);
        }

        @Test
        @DisplayName("existsByEmail gives null when the database is not available")
        void existsByEmailGivesNullOnError() {
            jdbc.fails();

            assertThat(repository.existsByEmail("a@b.c")).isNull();
        }

        @Test
        @DisplayName("getCountByEmail gives the number of users")
        void getCountByEmailGivesCount() {
            jdbc.givesObject(1);

            assertThat(repository.getCountByEmail("a@b.c")).isEqualTo(1);
        }

        @Test
        @DisplayName("getCountByEmail gives 0 when the database is not available")
        void getCountByEmailGivesZeroOnError() {
            jdbc.fails();

            assertThat(repository.getCountByEmail("a@b.c")).isZero();
        }

        @Test
        @DisplayName("getUserById gives the user")
        void getUserByIdGivesUser() {
            User stored = storedUser("secret");
            jdbc.givesObject(stored);

            assertThat(repository.getUserById(1)).isSameAs(stored);
            assertThat(jdbc.lastArgs()).containsExactly(1);
        }

        @Test
        @DisplayName("getUserById gives null when there is no such user")
        void getUserByIdGivesNullOnError() {
            jdbc.failsWith(new EmptyResultDataAccessException(1));

            assertThat(repository.getUserById(1)).isNull();
        }

        @Test
        @DisplayName("getUserSettingsById gives null when there are no settings")
        void getUserSettingsGivesNullOnError() {
            jdbc.failsWith(new EmptyResultDataAccessException(1));

            assertThat(repository.getUserSettingsById(1)).isNull();
        }

        @Test
        @DisplayName("getUserSettingsById gives the settings")
        void getUserSettingsGivesSettings() {
            Settings stored = new Settings(3, true, false, true);
            jdbc.givesObject(stored);

            assertThat(repository.getUserSettingsById(1)).isSameAs(stored);
        }

        @Test
        @DisplayName("getImageById gives null when there is no image")
        void getImageByIdGivesNullOnError() {
            jdbc.failsWith(new EmptyResultDataAccessException(1));

            assertThat(repository.getImageById(1)).isNull();
        }

        @Test
        @DisplayName("getImageById gives the image")
        void getImageByIdGivesImage() {
            Image stored = new Image(7, "Profilbild");
            jdbc.givesObject(stored);

            assertThat(repository.getImageById(1)).isSameAs(stored);
        }

        @Test
        @DisplayName("getUserDietryTags gives the food preferences of the user")
        void getUserDietryTagsGivesTags() {
            jdbc.givesRows(Arrays.asList(new Tag(1, "Vegan"), new Tag(2, "Lactose")));

            Optional<java.util.ArrayList<Tag>> tags = repository.getUserDietryTags(1);

            assertThat(tags).isPresent();
            assertThat(tags.get()).hasSize(2);
        }

        @Test
        @DisplayName("getUserDietryTags gives an empty list when the database is not available")
        void getUserDietryTagsGivesEmptyListOnError() {
            jdbc.fails();

            assertThat(repository.getUserDietryTags(1).get()).isEmpty();
        }

        @Test
        @DisplayName("isUserLoggedIn gives the login status")
        void isUserLoggedInGivesStatus() {
            jdbc.givesObject(Boolean.TRUE);

            assertThat(repository.isUserLoggedIn(1)).isTrue();
        }

        @Test
        @DisplayName("isUserLoggedIn gives false when the database is not available")
        void isUserLoggedInGivesFalseOnError() {
            jdbc.fails();

            assertThat(repository.isUserLoggedIn(1)).isFalse();
        }
    }

    @Nested
    @DisplayName("The write operations")
    class WriteOperations {

        @Test
        @DisplayName("Each operation gives the number of rows that changed")
        void operationsGiveChangedRows() {
            jdbc.givesChangedRows(1);

            assertThat(repository.allowPushNotification(1, true)).isEqualTo(1);
            assertThat(repository.allowReminder(1, true)).isEqualTo(1);
            assertThat(repository.allowSuggestion(1, true)).isEqualTo(1);
            assertThat(repository.updateDietryTag(1, "Vegan")).isEqualTo(1);
            assertThat(repository.updateUserInformation("A", "B", "a@b.c", 1)).isEqualTo(1);
            assertThat(repository.updateUserLoggedIn(1, true)).isEqualTo(1);
            assertThat(repository.createUserTag(1, new Tag(2, "Vegan"), 2)).isEqualTo(1);
        }

        @Test
        @DisplayName("Each operation gives 0 when the database is not available")
        void operationsGiveZeroOnError() {
            jdbc.fails();

            assertThat(repository.allowPushNotification(1, true)).isZero();
            assertThat(repository.allowReminder(1, true)).isZero();
            assertThat(repository.allowSuggestion(1, true)).isZero();
            assertThat(repository.updateDietryTag(1, "Vegan")).isZero();
            assertThat(repository.updateUserInformation("A", "B", "a@b.c", 1)).isZero();
            assertThat(repository.updateUserLoggedIn(1, true)).isZero();
            assertThat(repository.createUserTag(1, new Tag(2, "Vegan"), 2)).isZero();
        }

        @Test
        @DisplayName("The permissions go to the database in the order permission, user")
        void permissionsUseCorrectOrder() {
            repository.allowPushNotification(1, true);

            assertThat(jdbc.lastArgs()).containsExactly(true, 1);
            assertThat(jdbc.lastSql()).contains("PUSH_NOTIFICATIONS");
        }

        @Test
        @DisplayName("createUserTag writes the two foreign keys")
        void createUserTagWritesForeignKeys() {
            repository.createUserTag(1, new Tag(2, "Vegan"), 2);

            assertThat(jdbc.lastArgs()).containsExactly(1, 2);
            assertThat(jdbc.lastSql()).contains("ET_USER_TAG");
        }

        @Test
        @DisplayName("updateUserImage converts the Base64 string into bytes")
        void updateUserImageConvertsBase64() {
            repository.updateUserImage(new Image(7, "Profilbild", "url", "RmVlZE1l"));

            assertThat(jdbc.lastArgs()).hasSize(3);
            assertThat(jdbc.lastArgs().get(0)).isEqualTo("url");
            assertThat((byte[]) jdbc.lastArgs().get(1))
                    .isEqualTo("FeedMe".getBytes(StandardCharsets.UTF_8));
            assertThat(jdbc.lastArgs().get(2)).isEqualTo(7);
        }

        @Test
        @DisplayName("updateUserImage accepts an image without data")
        void updateUserImageAcceptsImageWithoutData() {
            assertThat(repository.updateUserImage(new Image(7, "Profilbild", "url", null)))
                    .isEqualTo(1);
            assertThat(jdbc.lastArgs().get(1)).isNull();
        }
    }

    @Nested
    @DisplayName("The row mappers")
    class RowMappers {

        @Test
        @DisplayName("The mapper of the user reads all columns")
        void userMapperReadsAllColumns() throws Exception {
            repository.getUserById(1);
            Map<String, Object> row = new HashMap<>();
            row.put("USER_ID", 1);
            row.put("FIRST_NAME", "Janina");
            row.put("LAST_NAME", "Mattes");
            row.put("EMAIL", "a@b.c");
            row.put("PASSWORD", "hash");

            User user = jdbc.mapRow(row);

            assertThat(user).isEqualTo(new User(1, "Janina", "Mattes", "a@b.c", "hash"));
        }

        @Test
        @DisplayName("The mapper of the image converts the bytes into a Base64 string")
        void imageMapperConvertsBytes() throws Exception {
            repository.getImageById(1);
            Map<String, Object> row = new HashMap<>();
            row.put("IMAGE_ID", 7);
            row.put("IMAGE_NAME", "Profilbild");
            row.put("IMAGE_URL", "url");
            row.put("IMAGE_BYTES", "FeedMe".getBytes(StandardCharsets.UTF_8));

            Image image = jdbc.mapRow(row);

            assertThat(image.getBase64ImageString()).isEqualTo("RmVlZE1l");
            assertThat(image.getImageId()).isEqualTo(7);
        }

        @Test
        @DisplayName("The mapper of the settings reads the three permissions")
        void settingsMapperReadsPermissions() throws Exception {
            repository.getUserSettingsById(1);
            Map<String, Object> row = new HashMap<>();
            row.put("SETTINGS_ID", 3);
            row.put("PUSH_NOTIFICATIONS", true);
            row.put("REMINDER_PRODUCT_EXPIRATION", false);
            row.put("SUGGEST_RECIPES", true);

            Settings settings = jdbc.mapRow(row);

            assertThat(settings).isEqualTo(new Settings(3, true, false, true));
        }

        @Test
        @DisplayName("The mapper of the tag reads the key and the name")
        void tagMapperReadsFields() throws Exception {
            repository.getUserDietryTags(1);
            Map<String, Object> row = new HashMap<>();
            row.put("TAG_ID", 2);
            row.put("LABEL", "Vegan");

            Tag tag = jdbc.mapRow(row);

            assertThat(tag).isEqualTo(new Tag(2, "Vegan"));
        }
    }
}
