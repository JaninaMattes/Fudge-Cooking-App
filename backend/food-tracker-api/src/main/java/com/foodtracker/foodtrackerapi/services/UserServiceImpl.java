package com.foodtracker.foodtrackerapi.services;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;

/**
 * <p>
 * The login, the registration and the profile of a user.
 * </p>
 *
 * @author Janina Mattes
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    /** Simple test of an email address: one part, an at sign, one more part. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(.+)@(.+)$");

    /** Value that the repositories give when a key is not available. */
    private static final Integer NO_ID = 0;

    private final UserRepository userRepository;

    /**
     * Makes the service.
     *
     * @param userRepository repository of the user data
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User validateUser(String email, String password) throws FTAuthException {
        return userRepository.findByEmailAndPassword(toLowerCase(email), password);
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password)
            throws FTAuthException {
        String normalizedEmail = toLowerCase(email);
        if (normalizedEmail == null || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new FTAuthException("Invalid email format");
        }
        if (userRepository.getCountByEmail(normalizedEmail) > 0) {
            throw new FTAuthException("Email already in use");
        }
        Integer userId = userRepository.createUser(firstName, lastName, normalizedEmail, password);
        if (!NO_ID.equals(userId)) {
            createDefaultValuesForUser(userId);
        }
        return userRepository.getUserById(userId);
    }

    @Override
    public User getUserById(Integer userId) {
        if (userId == null || NO_ID.equals(userId)) {
            return null;
        }
        User user = userRepository.getUserById(userId);
        if (user == null) {
            return null;
        }
        Image image = userRepository.getImageById(userId);
        if (image != null) {
            user.setUserImage(image);
        }
        Settings settings = userRepository.getUserSettingsById(userId);
        if (settings != null) {
            user.setUserSettings(settings);
        }
        userRepository.getUserDietryTags(userId).ifPresent(user::setDietaryPreferences);
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email) != null;
    }

    @Override
    public Integer createDietryTag(int userId, Tag dietryTag) {
        Integer tagId = userRepository.createDietryTag(dietryTag.getLabel());
        return userRepository.createUserTag(userId, dietryTag, tagId);
    }

    @Override
    public Integer allowPushNotification(int id, boolean allow) {
        return userRepository.allowPushNotification(id, allow);
    }

    @Override
    public Integer allowReminder(int id, boolean allow) {
        return userRepository.allowReminder(id, allow);
    }

    @Override
    public Integer allowSuggestion(int id, boolean allow) {
        return userRepository.allowSuggestion(id, allow);
    }

    @Override
    public Integer updateDietryTag(int id, String type) {
        return userRepository.updateDietryTag(id, type);
    }

    @Override
    public Integer updateUserInformation(String firstName, String lastName, String email,
            Integer userId) {
        return userRepository.updateUserInformation(firstName, lastName, email, userId);
    }

    @Override
    public Integer updateUserImage(Image image) {
        return userRepository.updateUserImage(image);
    }

    @Override
    public Integer updateUserLoggedIn(Integer userId, Boolean loggedIn) {
        return userRepository.updateUserLoggedIn(userId, Boolean.TRUE.equals(loggedIn));
    }

    @Override
    public Boolean isUserLoggedIn(int userId) {
        return userRepository.isUserLoggedIn(userId);
    }

    /**
     * Makes the empty lists and the default settings that a new user needs. The
     * user interface shows these lists directly after the registration.
     *
     * @param userId key of the new user
     */
    private void createDefaultValuesForUser(Integer userId) {
        userRepository.createUserImage(userId);
        userRepository.createProfileSettings(userId);
        userRepository.createInventoryList(userId);
        Integer shoppingListId = userRepository.createShoppingList(userId);
        if (!NO_ID.equals(shoppingListId)) {
            userRepository.createCurrentShoppingList(shoppingListId, userId);
            userRepository.createOldShoppingList(shoppingListId, userId);
        }
    }

    private static String toLowerCase(String email) {
        return email != null ? email.toLowerCase() : null;
    }
}
