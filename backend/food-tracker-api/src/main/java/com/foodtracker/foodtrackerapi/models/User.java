package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * One user of the application with the profile data. The password is the hash
 * value that BCrypt made. The clear password is never stored.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Primary key in the table ET_USERS. */
    private Integer userId;

    /** First name of the user. */
    private String firstName;

    /** Family name of the user. */
    private String lastName;

    /** Email address. The application uses it as the login name. */
    private String email;

    /** Password of the user as a BCrypt hash value. */
    private String password;

    /** Notification settings of the user. */
    private Settings userSettings;

    /** Profile image of the user. */
    private Image userImage;

    /** Food preferences of the user, for example "Vegan". */
    private ArrayList<Tag> dietaryPreferences = new ArrayList<>();

    /**
     * Makes a user with the login data only. The row mapper of the database
     * uses this constructor, because one row of ET_USERS holds these values
     * only.
     *
     * @param userId    primary key of the user
     * @param firstName first name of the user
     * @param lastName  family name of the user
     * @param email     email address of the user
     * @param password  BCrypt hash value of the password
     */
    public User(Integer userId, String firstName, String lastName, String email, String password) {
        this(userId, firstName, lastName, email, password, null, null, new ArrayList<>());
    }
}
