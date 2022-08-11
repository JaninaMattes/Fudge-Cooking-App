package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User validateUser(String email, String password) throws FTAuthException {
        if (email != null)
            email = email.toLowerCase();
        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password) throws FTAuthException {
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if (email != null)
            email = email.toLowerCase();
        if (!pattern.matcher(email).matches())
            throw new FTAuthException("Invalid email format");
        Integer count = userRepository.getCountByEmail(email);
        if (count > 0)
            throw new FTAuthException("Email already in use");
        Integer userId = userRepository.createUser(firstName, lastName, email, password);
        if (userId != 0) {
            // create default values and associate them with user
            createDefaultValuesForUser(userId);
        }
        return userRepository.getUserById(userId);
    }

    @Override
    public User getUserById(Integer userId) {
        User user = null;
        if (userId != 0) {
            user = userRepository.getUserById(userId);
            Image image = userRepository.getImageById(userId);
            Settings settings = userRepository.getUserSettingsById(userId);
            if (image != null) {
                user.setUserImage(image);
            }
            if (settings != null) {
                user.setUserSettings(settings);
            }
            Optional<ArrayList<Tag>> dietaryPreferences = userRepository.getUserDietryTags(userId);
            if (!dietaryPreferences.isEmpty() && dietaryPreferences.get() != null) {
                user.setDietaryPreferences(dietaryPreferences.get());
            }
        }
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        boolean exists = false;
        if (userRepository.existsByEmail(email) != null) {
            exists = true;
        }
        return exists;
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
    public Integer updateUserInformation(String firstName, String lastName, String email, Integer userId) {
        return userRepository.updateUserInformation(firstName, lastName, email, userId);
    }

    @Override
    public Integer updateUserImage(Image image) {
        return userRepository.updateUserImage(image);
    }

    @Override
    public Integer updateUserLoggedIn(Integer userId, Boolean loggedIn) {
        return userRepository.updateUserLoggedIn(userId, loggedIn);
    }

    @Override
    public Boolean isUserLoggedIn(int userId) {
        return userRepository.isUserLoggedIn(userId);
    }

    // Helper function
    private void createDefaultValuesForUser(Integer userId) {
        // create default image
        userRepository.createUserImage(userId);
        // create default settings
        userRepository.createProfileSettings(userId);
        // create inventory list
        userRepository.createInventoryList(userId);
        // create default shoppinglists
        Integer shopId = userRepository.createShoppingList(userId);
        if (shopId != 0) {
            userRepository.createCurrentShoppingList(shopId, userId);
            userRepository.createOldShoppingList(shopId, userId);
        }
    }
}
