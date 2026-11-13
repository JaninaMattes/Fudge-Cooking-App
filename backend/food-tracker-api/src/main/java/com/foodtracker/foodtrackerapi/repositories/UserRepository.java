package com.foodtracker.foodtrackerapi.repositories;

import java.util.ArrayList;
import java.util.Optional;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * <p>
 * The database operations on the user data, the profile image, the notification
 * settings and the food preferences.
 * </p>
 *
 * @author Janina Mattes
 */
public interface UserRepository {

    /**
     * Makes a user. The method writes the password as a BCrypt hash value.
     *
     * @param firstName first name of the user
     * @param lastName  family name of the user
     * @param email     email address of the user
     * @param password  clear password of the user
     * @return the key of the new user
     * @throws FTAuthException if the database cannot make the user
     */
    Integer createUser(String firstName, String lastName, String email, String password)
            throws FTAuthException;

    /**
     * Makes the profile image of a new user.
     *
     * @param userId key of the user
     * @return the key of the new image, or 0 if the operation failed
     */
    Integer createUserImage(Integer userId);

    /**
     * Makes a dietary tag.
     *
     * @param label name of the tag
     * @return the key of the new tag, or 0 if the operation failed
     */
    Integer createDietryTag(String label);

    /**
     * Links a dietary tag to a user.
     *
     * @param userId    key of the user
     * @param dietryTag tag of the user. The method writes the key only.
     * @param tagId     key of the tag
     * @return the number of rows that the operation wrote
     */
    Integer createUserTag(Integer userId, Tag dietryTag, Integer tagId);

    /**
     * Makes the notification settings of a new user. All permissions are off.
     *
     * @param userId key of the user
     * @return the key of the new settings, or 0 if the operation failed
     */
    Integer createProfileSettings(Integer userId);

    /**
     * Makes the old shopping list of a new user.
     *
     * @param shopId key of the shopping list
     * @param userId key of the user
     * @return the key of the new list, or 0 if the operation failed
     */
    Integer createOldShoppingList(Integer shopId, Integer userId);

    /**
     * Makes the inventory list of a new user.
     *
     * @param userId key of the user
     * @return the key of the new list, or 0 if the operation failed
     */
    Integer createInventoryList(Integer userId);

    /**
     * Makes the shopping list of a new user. The table holds the key only and
     * reflects the gen-spec pattern of the database.
     *
     * @param userId key of the user
     * @return the key of the new list, or 0 if the operation failed
     */
    Integer createShoppingList(Integer userId);

    /**
     * Makes the current shopping list of a new user.
     *
     * @param shopId key of the shopping list
     * @param userId key of the user
     * @return the key of the new list, or 0 if the operation failed
     */
    Integer createCurrentShoppingList(Integer shopId, Integer userId);

    /**
     * Searches for a user and tests the password.
     *
     * @param email    email address of the user
     * @param password clear password of the user
     * @return the user
     * @throws FTAuthException if the email address or the password is wrong
     */
    User findByEmailAndPassword(String email, String password) throws FTAuthException;

    /**
     * Searches for a user by the email address.
     *
     * @param email email address of the user
     * @return the user, or {@code null} if there is no such user
     */
    User existsByEmail(String email);

    /**
     * Changes the profile data of a user.
     *
     * @param firstName first name of the user
     * @param lastName  family name of the user
     * @param email     email address of the user
     * @param userId    key of the user
     * @return the number of rows that changed
     */
    Integer updateUserInformation(String firstName, String lastName, String email, Integer userId);

    /**
     * Writes the login status of a user.
     *
     * @param userId   key of the user
     * @param loggedIn {@code true} for a login, {@code false} for a logout
     * @return the number of rows that changed
     */
    Integer updateUserLoggedIn(int userId, boolean loggedIn);

    /**
     * Changes the profile image of a user.
     *
     * @param image image with the new values
     * @return the number of rows that changed
     */
    Integer updateUserImage(Image image);

    /**
     * Reads the login status of a user.
     *
     * @param userId key of the user
     * @return {@code true} if the user is logged in
     */
    Boolean isUserLoggedIn(Integer userId);

    /**
     * Counts the users that have the given email address.
     *
     * @param email email address to search for
     * @return the number of users
     */
    Integer getCountByEmail(String email);

    /**
     * Reads a user by the key.
     *
     * @param userId key of the user
     * @return the user, or {@code null} if there is no such user
     */
    User getUserById(int userId);

    /**
     * Reads the food preferences of a user.
     *
     * @param userId key of the user
     * @return the preferences. The list is empty if the user has none.
     */
    Optional<ArrayList<Tag>> getUserDietryTags(Integer userId);

    /**
     * Writes the permission to send push notifications.
     *
     * @param userId key of the user
     * @param allow  {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowPushNotification(int userId, boolean allow);

    /**
     * Writes the permission to send a reminder before a product expires.
     *
     * @param userId key of the user
     * @param allow  {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowReminder(int userId, boolean allow);

    /**
     * Writes the permission to suggest products for the shopping list.
     *
     * @param userId key of the user
     * @param allow  {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowSuggestion(int userId, boolean allow);

    /**
     * Changes the name of a food preference of a user.
     *
     * @param userId key of the user
     * @param type   new name of the preference
     * @return the number of rows that changed
     */
    Integer updateDietryTag(int userId, String type);

    /**
     * Reads the profile image of a user.
     *
     * @param userId key of the user
     * @return the image, or {@code null} if the user has no image
     */
    Image getImageById(int userId);

    /**
     * Reads the notification settings of a user.
     *
     * @param userId key of the user
     * @return the settings, or {@code null} if the user has no settings
     */
    Settings getUserSettingsById(int userId);
}
