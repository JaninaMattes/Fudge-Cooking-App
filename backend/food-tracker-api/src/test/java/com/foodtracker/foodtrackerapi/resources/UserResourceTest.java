package com.foodtracker.foodtrackerapi.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.foodtracker.foodtrackerapi.common.config.ApiProperties;
import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * Tests for {@link UserResource}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserResourceTest {

    private static final String SECRET_KEY = "test-secret-key";

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private UserResource resource;

    @BeforeEach
    void setUp() {
        ApiProperties apiProperties = new ApiProperties();
        apiProperties.setSecretKey(SECRET_KEY);
        apiProperties.setTokenValidityMillis(60_000L);
        resource = new UserResource(userService, apiProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(resource).build();
    }

    private static User user() {
        return new User(42, "Janina", "Mattes", "a@b.c", "hash");
    }

    @Test
    @DisplayName("The login gives a token that holds the data of the user")
    void loginGivesTokenWithUserData() throws Exception {
        when(userService.validateUser("a@b.c", "secret")).thenReturn(user());

        String body = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.c\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        assertThat(claims.get("userId")).isEqualTo(42);
        assertThat(claims.get("email")).isEqualTo("a@b.c");
        assertThat(claims.get("firstName")).isEqualTo("Janina");
        assertThat(claims.get("lastName")).isEqualTo("Mattes");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("The login gives the status 401 for wrong login data")
    void loginGivesUnauthorizedForWrongData() throws Exception {
        when(userService.validateUser(anyString(), anyString()))
                .thenThrow(new FTAuthException("Invalid EMAIL/PASSWORD"));

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.c\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("The registration gives a token for the new user")
    void registrationGivesToken() throws Exception {
        when(userService.registerUser("Janina", "Mattes", "a@b.c", "secret"))
                .thenReturn(user());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Janina\",\"lastName\":\"Mattes\","
                        + "\"email\":\"a@b.c\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("The registration accepts a body without fields")
    void registrationAcceptsEmptyBody() throws Exception {
        when(userService.registerUser(null, null, null, null)).thenReturn(user());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("The test of an email address gives the status 200 when the address is free")
    void emailTestGivesOkForFreeAddress() throws Exception {
        when(userService.existsByEmail("a@b.c")).thenReturn(false);

        mockMvc.perform(post("/api/users/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.c\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("The test of an email address gives the status 400 when the address is in use")
    void emailTestGivesBadRequestForAddressInUse() throws Exception {
        when(userService.existsByEmail("a@b.c")).thenReturn(true);

        mockMvc.perform(post("/api/users/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.c\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("The profile endpoint gives the user")
    void profileGivesUser() throws Exception {
        when(userService.getUserById(42)).thenReturn(user());

        mockMvc.perform(get("/api/users/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.firstName").value("Janina"));
    }

    @Test
    @DisplayName("The login status endpoint gives the status of the user")
    void loginStatusGivesStatus() throws Exception {
        when(userService.isUserLoggedIn(42)).thenReturn(true);

        mockMvc.perform(get("/api/users/isloggedin/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("The endpoint for a food preference reads the nested tag")
    void dietryTagEndpointReadsNestedTag() throws Exception {
        when(userService.createDietryTag(anyInt(), any(Tag.class))).thenReturn(1);

        mockMvc.perform(put("/api/users/create/dietrytag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"dietryTag\":{\"tagId\":2,\"label\":\"Vegan\"}}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Tag> tag = ArgumentCaptor.forClass(Tag.class);
        verify(userService).createDietryTag(anyInt(), tag.capture());
        assertThat(tag.getValue()).isEqualTo(new Tag(2, "Vegan"));
    }

    @Test
    @DisplayName("The login endpoint reads the value 1 as true")
    void loginEndpointReadsOneAsTrue() throws Exception {
        mockMvc.perform(put("/api/users/update/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"loginStatus\":1}"))
                .andExpect(status().isOk());

        verify(userService).updateUserLoggedIn(42, true);
    }

    @Test
    @DisplayName("The logout endpoint reads the value 0 as false")
    void logoutEndpointReadsZeroAsFalse() throws Exception {
        mockMvc.perform(put("/api/users/update/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"loginStatus\":0}"))
                .andExpect(status().isOk());

        verify(userService).updateUserLoggedIn(42, false);
    }

    @Test
    @DisplayName("The logout endpoint accepts a body without a status")
    void logoutEndpointAcceptsBodyWithoutStatus() throws Exception {
        mockMvc.perform(put("/api/users/update/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42}"))
                .andExpect(status().isOk());

        verify(userService).updateUserLoggedIn(42, false);
    }

    @Test
    @DisplayName("The profile endpoint writes the new data of the user")
    void profileEndpointWritesNewData() throws Exception {
        when(userService.updateUserInformation("Janina", "Mattes", "a@b.c", 42)).thenReturn(1);

        mockMvc.perform(put("/api/users/update/userprofile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Janina\",\"lastName\":\"Mattes\","
                        + "\"email\":\"a@b.c\",\"userId\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("The image endpoint reads the image of the body")
    void imageEndpointReadsImage() throws Exception {
        when(userService.updateUserImage(any(Image.class))).thenReturn(1);

        mockMvc.perform(put("/api/users/update/image")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"imageId\":7,\"imageName\":\"Profilbild\",\"imageUrl\":\"url\","
                        + "\"imageBytes\":\"RmVlZE1l\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<Image> image = ArgumentCaptor.forClass(Image.class);
        verify(userService).updateUserImage(image.capture());
        assertThat(image.getValue()).isEqualTo(new Image(7, "Profilbild", "url", "RmVlZE1l"));
    }

    @Test
    @DisplayName("Each permission endpoint writes the permission of the user")
    void permissionEndpointsWritePermission() throws Exception {
        when(userService.allowPushNotification(anyInt(), anyBoolean())).thenReturn(1);
        when(userService.allowReminder(anyInt(), anyBoolean())).thenReturn(1);
        when(userService.allowSuggestion(anyInt(), anyBoolean())).thenReturn(1);

        mockMvc.perform(put("/api/users/update/pushNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"allow\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/update/reminder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"allow\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/update/suggestion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"allow\":1}"))
                .andExpect(status().isOk());

        verify(userService).allowPushNotification(42, true);
        verify(userService).allowReminder(42, false);
        verify(userService).allowSuggestion(42, true);
    }

    @Test
    @DisplayName("The endpoint for a food preference writes the new name")
    void dietryTagEndpointWritesNewName() throws Exception {
        when(userService.updateDietryTag(42, "Vegan")).thenReturn(1);

        mockMvc.perform(put("/api/users/update/dietrytag")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":42,\"type\":\"Vegan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}
