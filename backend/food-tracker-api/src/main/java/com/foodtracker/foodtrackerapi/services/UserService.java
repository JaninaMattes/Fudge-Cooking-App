package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * <p>
 * The login, the registration and the profile of a user.
 * </p>
 *
 * @author Janina Mattes
 */
public interface UserService {

    /**
     * Tests the login data of a user.
     *
     * @param email    email address of the user
     * @param password clear password of the user
     * @return the user
     * @throws FTAuthException if the email address or the password is wrong
     */
    User validateUser(String email, String password) throws FTAuthException;

    /**
     * Makes a user with the empty lists and the default settings.
     *
     * @param firstName first name of the user
     * @param lastName  family name of the user
     * @param email     email address of the user
     * @param password  clear password of the user
     * @return the new user
     * @throws FTAuthException if the email address has a wrong form or is
     *                         already in use
     */
    User registerUser(String firstName, String lastName, String email, String password)
            throws FTAuthException;

    /**
     * Makes a food preference of a user.
     *
     * @param userId key of the user
     * @param tag    preference to store
     * @return the number of rows that the operation wrote
     */
    Integer createDietryTag(int userId, Tag tag);

    /**
     * Reads a user with the image, the settings and the food preferences.
     *
     * @param userId key of the user
     * @return the user, or {@code null} if there is no such user
     */
    User getUserById(Integer userId);

    /**
     * Writes the login status of a user.
     *
     * @param userId   key of the user
     * @param loggedIn {@code true} for a login, {@code false} for a logout
     * @return the number of rows that changed
     */
    Integer updateUserLoggedIn(Integer userId, Boolean loggedIn);

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
     * Changes the profile image of a user.
     *
     * @param image image with the new values
     * @return the number of rows that changed
     */
    Integer updateUserImage(Image image);

    /**
     * Changes the name of a food preference.
     *
     * @param id   key of the user
     * @param type new name of the preference
     * @return the number of rows that changed
     */
    Integer updateDietryTag(int id, String type);

    /**
     * Writes the permission to send push notifications.
     *
     * @param id    key of the user
     * @param allow {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowPushNotification(int id, boolean allow);

    /**
     * Writes the permission to send a reminder before a product expires.
     *
     * @param id    key of the user
     * @param allow {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowReminder(int id, boolean allow);

    /**
     * Writes the permission to suggest products for the shopping list.
     *
     * @param id    key of the user
     * @param allow {@code true} if the user gives permission
     * @return the number of rows that changed
     */
    Integer allowSuggestion(int id, boolean allow);

    /**
     * Reads the login status of a user.
     *
     * @param userId key of the user
     * @return {@code true} if the user is logged in
     */
    Boolean isUserLoggedIn(int userId);

    /**
     * Tells if an email address is already in use.
     *
     * @param email email address to test
     * @return {@code true} if a user with this address exists
     */
    boolean existsByEmail(String email);
}
