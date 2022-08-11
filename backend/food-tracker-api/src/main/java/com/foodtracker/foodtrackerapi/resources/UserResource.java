package com.foodtracker.foodtrackerapi.resources;

import com.foodtracker.foodtrackerapi.Constants;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.services.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User Rest Controller class to map incoming paths to REST ressource.
 * Tutorial: https://www.baeldung.com/spring-pathvariable
 * 
 * @author Janina Mattes
 */

@RestController
@RequestMapping("/api/users")
public class UserResource {

    @Autowired
    UserService userService;

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody Map<String, Object> userMap) {
        String email = (String) userMap.get("email");
        String password = (String) userMap.get("password");
        User user = userService.validateUser(email, password);
        return new ResponseEntity<>(generateJWTToken(user), HttpStatus.OK);
    }

    @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody Map<String, Object> bodyObject) {

        Map<String, Object> userMap = Collections.emptyMap();
        if (MapUtils.isNotEmpty(bodyObject)) {
            userMap = bodyObject;
        }

        String firstName = (String) userMap.get("firstName");
        String lastName = (String) userMap.get("lastName");
        String email = (String) userMap.get("email");
        String password = (String) userMap.get("password");

        User user = userService.registerUser(firstName, lastName, email, password);
        return new ResponseEntity<>(generateJWTToken(user), HttpStatus.OK);
    }

    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> existsByEmail(@RequestBody Map<String, Object> bodyObject) {
        Map<String, Object> userMap = Collections.emptyMap();
        if (MapUtils.isNotEmpty(bodyObject)) {
            userMap = bodyObject;
        }
        String email = (String) userMap.get("email");
        if (userService.existsByEmail(email)) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }
        return new ResponseEntity<>("Email is available!", HttpStatus.OK);
    }

    @GetMapping(path = "/{userId}", produces = "application/json")
    public ResponseEntity<User> getUserById(@PathVariable("userId") Integer userId) {
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(path = "/isloggedin/{userId}", produces = "application/json")
    public ResponseEntity<Boolean> isUserLoggedIn(@PathVariable("userId") Integer userId) {
        Boolean isLoggedIn = userService.isUserLoggedIn(userId);
        return new ResponseEntity<>(isLoggedIn, HttpStatus.OK);
    }

    @PutMapping(path = "/create/dietrytag", produces = "application/json")
    public Integer createDietryTag(@RequestBody Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        Map<String, Object> dietryTag = (Map<String, Object>) map.get("dietryTag");
        Tag tag = mapToTag(dietryTag);
        return userService.createDietryTag(userId, tag);
    }

    @PutMapping(path = "/update/login", produces = "application/json")
    public Integer logUserIn(@RequestBody Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        Integer login = (Integer) map.get("loginStatus");
        return userService.updateUserLoggedIn(userId, mapIntegerToBool(login));
    }

    @PutMapping(path = "/update/logout", produces = "application/json")
    public Integer logUserOut(@RequestBody Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        Integer logout = (Integer) map.get("loginStatus");
        return userService.updateUserLoggedIn(userId, mapIntegerToBool(logout));
    }

    @PutMapping(path = "/update/userprofile", produces = "application/json")
    public ResponseEntity<Integer> updateUserProfileInformation(@RequestBody Map<String, Object> userMap) {
        String firstName = (String) userMap.get("firstName");
        String lastName = (String) userMap.get("lastName");
        String email = (String) userMap.get("email");
        Integer userId = (Integer) userMap.get("userId");
        Integer result = userService.updateUserInformation(firstName, lastName, email, userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(path = "/update/image", produces = "application/json")
    public ResponseEntity<Integer> updateUserProfileImage(@RequestBody Map<String, Object> userMap) {
        Integer result = userService.updateUserImage(mapToImage(userMap));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(path = "/update/pushNotification", produces = "application/json")
    public ResponseEntity<Integer> allowPushNotification(@RequestBody Map<String, Object> map) {
        Integer userId = (Integer) map.get("userId");
        Integer allow = (Integer) map.get("allow");
        Integer result = userService.allowPushNotification(userId, mapIntegerToBool(allow));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(path = "/update/reminder", produces = "application/json")
    public ResponseEntity<Integer> allowReminder(@RequestBody Map<String, Object> map) {
        Integer id = (Integer) map.get("userId");
        Integer allow = (Integer) map.get("allow");
        Integer result = userService.allowReminder(id, mapIntegerToBool(allow));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(path = "/update/suggestion", produces = "application/json")
    public ResponseEntity<Integer> allowSuggestion(@RequestBody Map<String, Object> map) {
        Integer id = (Integer) map.get("userId");
        Integer allow = (Integer) map.get("allow");
        Integer result = userService.allowSuggestion(id, mapIntegerToBool(allow));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(path = "/update/dietrytag", produces = "application/json")
    public Integer updateDietryTag(@RequestBody Map<String, Object> map) {
        Integer id = (Integer) map.get("userId");
        String type = (String) map.get("type");
        return userService.updateDietryTag(id, type);
    }

    /**
     * Generate JWT token for basic authentification.
     * 
     * @param user
     * @return
     * @author Janina Mattes
     */
    private Map<String, String> generateJWTToken(User user) {
        long timestamp = System.currentTimeMillis();
        String token = Jwts.builder().signWith(SignatureAlgorithm.HS256, Constants.API_SECRET_KEY)
                .setIssuedAt(new Date(timestamp))
                .setExpiration(new Date(timestamp + Constants.TOKEN_VALIDITY))
                .claim("userId", user.getUserId())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .compact();
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return map;
    }

    /**
     * Map from JSONObject to Java POJO.
     * 
     * @param tag
     * @return
     */
    private Tag mapToTag(Map<String, Object> tag) {
        Integer tagId = (Integer) tag.get("tagId");
        String label = (String) tag.get("label");
        return new Tag(tagId, label);
    }

    /**
     * 
     * <p>
     * Maps the incomming map<string, obj> to an Image Java POJO object.
     * </p>
     * 
     * @param image
     * @return Image
     * 
     * @author Janina Mattes
     */
    private Image mapToImage(Map<String, Object> image) {
        // map incomming map<string, obj> to an image
        Integer imageId = (Integer) image.get("imageId");
        String imageName = (String) image.get("imageName");
        String imageUrl = (String) image.get("imageUrl");
        String base64String = (String) image.get("imageBytes");
        // base64 encoding
        return new Image(imageId, imageName, imageUrl, base64String);
    }

    private Boolean mapIntegerToBool(Integer allow) {
        Boolean bool = false;
        if (allow == 1) {
            bool = true;
        }
        return bool;
    }
}
