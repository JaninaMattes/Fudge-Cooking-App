package com.foodtracker.foodtrackerapi.repositories;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

import java.util.ArrayList;
import java.util.Optional;

public interface UserRepository {

    Integer createUser(String firstName, String lastName, String email, String password) throws FTAuthException;

    Integer createUserImage(Integer userId);

    Integer createDietryTag(String label);

    Integer createUserTag(Integer userId, Tag dietryTag, Integer tagId);

    Integer createProfileSettings(Integer userId);
    
    Integer createOldShoppingList(Integer shopId, Integer userId);

    Integer createInventoryList(Integer userId);

    Integer createShoppingList(Integer userId);

    Integer createCurrentShoppingList(Integer shopId, Integer userId);

    User findByEmailAndPassword(String email, String password) throws FTAuthException;

    User existsByEmail(String email);

    Integer updateUserInformation(String firstName, String lastName, String email, Integer userId);

    Integer updateUserLoggedIn(int userId, boolean loggedIn);

    Integer updateUserImage(Image image);

    Boolean isUserLoggedIn(Integer userId);

    Integer getCountByEmail(String email);

    User getUserById(int userId);

    Optional<ArrayList<Tag>> getUserDietryTags(Integer userId);

    Integer allowPushNotification(int userId, boolean allow);

    Integer allowReminder(int userId, boolean allow);

    Integer allowSuggestion(int userId, boolean allow);

    Integer updateDietryTag(int userId, String type);

    Image getImageById(int userId);

    Settings getUserSettingsById(int userId);
}
