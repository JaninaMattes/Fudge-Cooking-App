package com.foodtracker.foodtrackerapi.services;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

public interface UserService {

    User validateUser(String email, String password) throws FTAuthException;

    User registerUser(String firstName, String lastName, String email, String password) throws FTAuthException;

    Integer createDietryTag(int userId, Tag tag);

    User getUserById(Integer userId);

    Integer updateUserLoggedIn(Integer userId, Boolean loggedIn);

    Integer updateUserInformation(String firstName, String lastName, String email, Integer userId);

    Integer updateUserImage(Image image);
    
    Integer updateDietryTag(int id, String type);

    Integer allowPushNotification(int id, boolean allow);

    Integer allowReminder(int id, boolean allow);

    Integer allowSuggestion(int id, boolean allow);

    Boolean isUserLoggedIn(int userId);

    boolean existsByEmail(String email);
}
