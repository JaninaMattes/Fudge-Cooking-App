package com.foodtracker.foodtrackerapi.resources;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodtracker.foodtrackerapi.common.security.TokenService;
import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.services.UserService;

/**
 * <p>
 * The endpoints for the login, the registration and the profile of a user.
 * </p>
 *
 * @author Janina Mattes
 */
@RestController
@RequestMapping("/api/users")
public class UserResource {

    /** Name of the field that holds the token in the answer. */
    static final String TOKEN_FIELD = "token";

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * Makes the resource.
     *
     * @param userService   service of the user data
     * @param tokenService service that writes the token of the login
     */
    public UserResource(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * Logs a user in.
     *
     * @param userMap body with the fields email and password
     * @return a JWT token for the next requests
     */
    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> loginUser(
            @RequestBody Map<String, Object> userMap) {
        User user = userService.validateUser(
                RequestMapper.asString(userMap, "email"),
                RequestMapper.asString(userMap, "password"));
        return new ResponseEntity<>(generateJWTToken(user), HttpStatus.OK);
    }

    /**
     * Makes a user.
     *
     * @param bodyObject body with the fields firstName, lastName, email and
     *                   password
     * @return a JWT token for the next requests
     */
    @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestBody Map<String, Object> bodyObject) {
        User user = userService.registerUser(
                RequestMapper.asString(bodyObject, "firstName"),
                RequestMapper.asString(bodyObject, "lastName"),
                RequestMapper.asString(bodyObject, "email"),
                RequestMapper.asString(bodyObject, "password"));
        return new ResponseEntity<>(generateJWTToken(user), HttpStatus.OK);
    }

    /**
     * Tells if an email address is free. The registration form asks this
     * question before the user sends the form.
     *
     * @param bodyObject body with the field email
     * @return the status 200 if the address is free, and the status 400 if the
     *         address is already in use
     */
    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> existsByEmail(@RequestBody Map<String, Object> bodyObject) {
        String email = RequestMapper.asString(bodyObject, "email");
        if (userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
        return new ResponseEntity<>("Email is available!", HttpStatus.OK);
    }

    /**
     * Reads the profile of a user.
     *
     * @param userId key of the user
     * @return the user with the image, the settings and the food preferences
     */
    @GetMapping(path = "/{userId}", produces = "application/json")
    public ResponseEntity<User> getUserById(@PathVariable("userId") Integer userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    /**
     * Reads the login status of a user.
     *
     * @param userId key of the user
     * @return {@code true} if the user is logged in
     */
    @GetMapping(path = "/isloggedin/{userId}", produces = "application/json")
    public ResponseEntity<Boolean> isUserLoggedIn(@PathVariable("userId") Integer userId) {
        return new ResponseEntity<>(userService.isUserLoggedIn(userId), HttpStatus.OK);
    }

    /**
     * Makes a food preference of a user.
     *
     * @param map body with the fields userId and dietryTag
     * @return the number of rows that the operation wrote
     */
    @PutMapping(path = "/create/dietrytag", produces = "application/json")
    public Integer createDietryTag(@RequestBody Map<String, Object> map) {
        Integer userId = RequestMapper.asInteger(map, "userId");
        Tag tag = RequestMapper.toTag(RequestMapper.asMap(map, "dietryTag"));
        return userService.createDietryTag(userId, tag);
    }

    /**
     * Logs a user in.
     *
     * @param map body with the fields userId and loginStatus
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/login", produces = "application/json")
    public Integer logUserIn(@RequestBody Map<String, Object> map) {
        return setLoginStatus(map);
    }

    /**
     * Logs a user out.
     *
     * @param map body with the fields userId and loginStatus
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/logout", produces = "application/json")
    public Integer logUserOut(@RequestBody Map<String, Object> map) {
        return setLoginStatus(map);
    }

    /**
     * Changes the profile data of a user.
     *
     * @param userMap body with the fields firstName, lastName, email and userId
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/userprofile", produces = "application/json")
    public ResponseEntity<Integer> updateUserProfileInformation(
            @RequestBody Map<String, Object> userMap) {
        Integer result = userService.updateUserInformation(
                RequestMapper.asString(userMap, "firstName"),
                RequestMapper.asString(userMap, "lastName"),
                RequestMapper.asString(userMap, "email"),
                RequestMapper.asInteger(userMap, "userId"));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Changes the profile image of a user.
     *
     * @param userMap body with the fields imageId, imageName, imageUrl and
     *                imageBytes
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/image", produces = "application/json")
    public ResponseEntity<Integer> updateUserProfileImage(
            @RequestBody Map<String, Object> userMap) {
        Integer result = userService.updateUserImage(RequestMapper.toImage(userMap));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Writes the permission to send push notifications.
     *
     * @param map body with the fields userId and allow
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/pushNotification", produces = "application/json")
    public ResponseEntity<Integer> allowPushNotification(@RequestBody Map<String, Object> map) {
        Integer result = userService.allowPushNotification(
                RequestMapper.asInteger(map, "userId"),
                RequestMapper.toBoolean(RequestMapper.asInteger(map, "allow")));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Writes the permission to send a reminder before a product expires.
     *
     * @param map body with the fields userId and allow
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/reminder", produces = "application/json")
    public ResponseEntity<Integer> allowReminder(@RequestBody Map<String, Object> map) {
        Integer result = userService.allowReminder(
                RequestMapper.asInteger(map, "userId"),
                RequestMapper.toBoolean(RequestMapper.asInteger(map, "allow")));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Writes the permission to suggest products for the shopping list.
     *
     * @param map body with the fields userId and allow
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/suggestion", produces = "application/json")
    public ResponseEntity<Integer> allowSuggestion(@RequestBody Map<String, Object> map) {
        Integer result = userService.allowSuggestion(
                RequestMapper.asInteger(map, "userId"),
                RequestMapper.toBoolean(RequestMapper.asInteger(map, "allow")));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Changes the name of a food preference.
     *
     * @param map body with the fields userId and type
     * @return the number of rows that changed
     */
    @PutMapping(path = "/update/dietrytag", produces = "application/json")
    public Integer updateDietryTag(@RequestBody Map<String, Object> map) {
        return userService.updateDietryTag(
                RequestMapper.asInteger(map, "userId"),
                RequestMapper.asString(map, "type"));
    }

    /**
     * Makes a JWT token for a user. The client sends the token with each
     * request that needs a login.
     *
     * @param user user that logged in
     * @return a map with the field "token"
     */
    private Map<String, String> generateJWTToken(User user) {
        return Collections.singletonMap(TOKEN_FIELD, tokenService.issue(user));
    }

    private Integer setLoginStatus(Map<String, Object> map) {
        return userService.updateUserLoggedIn(
                RequestMapper.asInteger(map, "userId"),
                RequestMapper.toBoolean(RequestMapper.asInteger(map, "loginStatus")));
    }
}
