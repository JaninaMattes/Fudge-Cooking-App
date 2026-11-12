package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.foodtracker.foodtrackerapi.exceptions.FTAuthException;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;

/**
 * Tests for {@link UserServiceImpl}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository);
    }

    @Nested
    @DisplayName("validateUser")
    class ValidateUser {

        @Test
        @DisplayName("writes the email address with small letters")
        void writesEmailWithSmallLetters() {
            User user = new User(1, "Janina", "Mattes", "a@b.c", "hash");
            when(userRepository.findByEmailAndPassword("a@b.c", "secret")).thenReturn(user);

            assertThat(service.validateUser("A@B.C", "secret")).isSameAs(user);
        }

        @Test
        @DisplayName("accepts an email address that is null")
        void acceptsNullEmail() {
            service.validateUser(null, "secret");

            verify(userRepository).findByEmailAndPassword(null, "secret");
        }
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("makes the user with the empty lists and the default settings")
        void makesUserWithDefaultValues() {
            when(userRepository.getCountByEmail("a@b.c")).thenReturn(0);
            when(userRepository.createUser("Janina", "Mattes", "a@b.c", "secret")).thenReturn(42);
            when(userRepository.createShoppingList(42)).thenReturn(9);
            User created = new User(42, "Janina", "Mattes", "a@b.c", "hash");
            when(userRepository.getUserById(42)).thenReturn(created);

            assertThat(service.registerUser("Janina", "Mattes", "A@B.C", "secret"))
                    .isSameAs(created);

            InOrder order = Mockito.inOrder(userRepository);
            order.verify(userRepository).createUserImage(42);
            order.verify(userRepository).createProfileSettings(42);
            order.verify(userRepository).createInventoryList(42);
            order.verify(userRepository).createShoppingList(42);
            order.verify(userRepository).createCurrentShoppingList(9, 42);
            order.verify(userRepository).createOldShoppingList(9, 42);
        }

        @Test
        @DisplayName("makes no shopping list when the database gives no key")
        void makesNoShoppingListWithoutKey() {
            when(userRepository.getCountByEmail(anyString())).thenReturn(0);
            when(userRepository.createUser(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(42);
            when(userRepository.createShoppingList(42)).thenReturn(0);

            service.registerUser("Janina", "Mattes", "a@b.c", "secret");

            verify(userRepository, never()).createCurrentShoppingList(anyInt(), anyInt());
            verify(userRepository, never()).createOldShoppingList(anyInt(), anyInt());
        }

        @Test
        @DisplayName("makes no default values when the user was not created")
        void makesNoDefaultValuesWithoutUser() {
            when(userRepository.getCountByEmail(anyString())).thenReturn(0);
            when(userRepository.createUser(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(0);

            service.registerUser("Janina", "Mattes", "a@b.c", "secret");

            verify(userRepository, never()).createUserImage(anyInt());
        }

        @ParameterizedTest
        @DisplayName("throws when the email address has a wrong form")
        @NullSource
        @ValueSource(strings = { "", "not-an-email", "@b.c", "a@" })
        void throwsForInvalidEmail(String email) {
            assertThatThrownBy(() -> service.registerUser("A", "B", email, "secret"))
                    .isInstanceOf(FTAuthException.class)
                    .hasMessage("Invalid email format");

            verify(userRepository, never()).createUser(anyString(), anyString(), anyString(),
                    anyString());
        }

        @Test
        @DisplayName("throws when the email address is already in use")
        void throwsForEmailInUse() {
            when(userRepository.getCountByEmail("a@b.c")).thenReturn(1);

            assertThatThrownBy(() -> service.registerUser("A", "B", "a@b.c", "secret"))
                    .isInstanceOf(FTAuthException.class)
                    .hasMessage("Email already in use");
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("reads the image, the settings and the food preferences")
        void readsImageSettingsAndPreferences() {
            User user = new User(1, "Janina", "Mattes", "a@b.c", "hash");
            Image image = new Image(7, "Profilbild");
            Settings settings = new Settings(3);
            ArrayList<Tag> tags = new ArrayList<>(Arrays.asList(new Tag(1, "Vegan")));
            when(userRepository.getUserById(1)).thenReturn(user);
            when(userRepository.getImageById(1)).thenReturn(image);
            when(userRepository.getUserSettingsById(1)).thenReturn(settings);
            when(userRepository.getUserDietryTags(1)).thenReturn(Optional.of(tags));

            User result = service.getUserById(1);

            assertThat(result.getUserImage()).isSameAs(image);
            assertThat(result.getUserSettings()).isSameAs(settings);
            assertThat(result.getDietaryPreferences()).isSameAs(tags);
        }

        @Test
        @DisplayName("accepts a user without an image and without settings")
        void acceptsUserWithoutImageAndSettings() {
            User user = new User(1, "Janina", "Mattes", "a@b.c", "hash");
            when(userRepository.getUserById(1)).thenReturn(user);
            when(userRepository.getImageById(1)).thenReturn(null);
            when(userRepository.getUserSettingsById(1)).thenReturn(null);
            when(userRepository.getUserDietryTags(1)).thenReturn(Optional.empty());

            User result = service.getUserById(1);

            assertThat(result.getUserImage()).isNull();
            assertThat(result.getUserSettings()).isNull();
            assertThat(result.getDietaryPreferences()).isEmpty();
        }

        @Test
        @DisplayName("gives null for the key 0 and for a key that is null")
        void givesNullForEmptyKey() {
            assertThat(service.getUserById(0)).isNull();
            assertThat(service.getUserById(null)).isNull();

            verify(userRepository, never()).getUserById(anyInt());
        }

        @Test
        @DisplayName("gives null when there is no such user")
        void givesNullForUnknownUser() {
            when(userRepository.getUserById(1)).thenReturn(null);

            assertThat(service.getUserById(1)).isNull();
            verify(userRepository, never()).getImageById(anyInt());
        }
    }

    @Nested
    @DisplayName("The other operations")
    class OtherOperations {

        @Test
        @DisplayName("existsByEmail gives true when the database gives a user")
        void existsByEmailGivesTrueForKnownUser() {
            when(userRepository.existsByEmail("a@b.c"))
                    .thenReturn(new User(1, "A", "B", "a@b.c", "hash"));

            assertThat(service.existsByEmail("a@b.c")).isTrue();
        }

        @Test
        @DisplayName("existsByEmail gives false when the database gives no user")
        void existsByEmailGivesFalseForUnknownUser() {
            when(userRepository.existsByEmail("a@b.c")).thenReturn(null);

            assertThat(service.existsByEmail("a@b.c")).isFalse();
        }

        @Test
        @DisplayName("createDietryTag makes the tag and links it to the user")
        void createDietryTagMakesAndLinksTag() {
            Tag tag = new Tag(null, "Vegan");
            when(userRepository.createDietryTag("Vegan")).thenReturn(2);
            when(userRepository.createUserTag(1, tag, 2)).thenReturn(1);

            assertThat(service.createDietryTag(1, tag)).isEqualTo(1);
        }

        @Test
        @DisplayName("updateUserLoggedIn accepts a status that is null")
        void updateUserLoggedInAcceptsNull() {
            service.updateUserLoggedIn(1, null);

            verify(userRepository).updateUserLoggedIn(1, false);
        }

        @Test
        @DisplayName("updateUserLoggedIn writes the status")
        void updateUserLoggedInWritesStatus() {
            service.updateUserLoggedIn(1, true);

            verify(userRepository).updateUserLoggedIn(1, true);
        }

        @Test
        @DisplayName("Each operation gives the answer of the repository")
        void operationsGiveAnswerOfRepository() {
            when(userRepository.allowPushNotification(1, true)).thenReturn(1);
            when(userRepository.allowReminder(1, true)).thenReturn(1);
            when(userRepository.allowSuggestion(1, true)).thenReturn(1);
            when(userRepository.updateDietryTag(1, "Vegan")).thenReturn(1);
            when(userRepository.updateUserInformation("A", "B", "a@b.c", 1)).thenReturn(1);
            when(userRepository.isUserLoggedIn(1)).thenReturn(true);
            Image image = new Image(7, "Profilbild");
            when(userRepository.updateUserImage(image)).thenReturn(1);

            assertThat(service.allowPushNotification(1, true)).isEqualTo(1);
            assertThat(service.allowReminder(1, true)).isEqualTo(1);
            assertThat(service.allowSuggestion(1, true)).isEqualTo(1);
            assertThat(service.updateDietryTag(1, "Vegan")).isEqualTo(1);
            assertThat(service.updateUserInformation("A", "B", "a@b.c", 1)).isEqualTo(1);
            assertThat(service.isUserLoggedIn(1)).isTrue();
            assertThat(service.updateUserImage(image)).isEqualTo(1);
        }

        @Test
        @DisplayName("The permissions can also be taken away")
        void permissionsCanBeTakenAway() {
            service.allowPushNotification(1, false);
            service.allowReminder(1, false);
            service.allowSuggestion(1, false);

            verify(userRepository).allowPushNotification(1, false);
            verify(userRepository).allowReminder(1, false);
            verify(userRepository).allowSuggestion(eq(1), eq(false));
        }
    }
}
