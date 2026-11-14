package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.foodtracker.foodtrackerapi.common.diet.DietRules;
import com.foodtracker.foodtrackerapi.models.Ingredient;
import com.foodtracker.foodtrackerapi.models.Recipe;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.models.User;

/**
 * Tests for {@link RecipeSuggestionServiceImpl}.
 *
 * <p>The rules themselves are tested in {@code DietRulesTest}. This class
 * tests what the service does around them: it reads the requirements of the
 * user, it keeps a recipe that fits, it drops a recipe that does not, and it
 * stays calm when the user or the list is missing.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecipeSuggestionServiceImplTest {

    private static final Integer USER_ID = 1;

    @Mock
    private UserService userService;

    private RecipeSuggestionServiceImpl service;

    @BeforeEach
    void setUp() throws IOException {
        DietRules rules = DietRules.fromClasspath(DietRules.DEFAULT_RESOURCE);
        service = new RecipeSuggestionServiceImpl(userService, rules);
    }

    private static Recipe recipe(String title, String... ingredientNames) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        for (String name : ingredientNames) {
            Ingredient ingredient = new Ingredient();
            ingredient.setIngredientName(name);
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        return recipe;
    }

    private void givenUserWith(String... labels) {
        User user = new User();
        ArrayList<Tag> tags = new ArrayList<>();
        for (String label : labels) {
            Tag tag = new Tag();
            tag.setLabel(label);
            tags.add(tag);
        }
        user.setDietaryPreferences(tags);
        when(userService.getUserById(USER_ID)).thenReturn(user);
    }

    @Test
    @DisplayName("a vegetarian user keeps the onion cake and loses the roast")
    void aVegetarianKeepsOnlyWhatFits() {
        givenUserWith("Vegetarian", "Low Carb");
        List<Recipe> all = List.of(
                recipe("Roast wild boar", "Wild boar", "Onions"),
                recipe("Simple onion cake", "Flour", "Onions"));

        List<Recipe> fitting = service.fitting(USER_ID, all);

        assertThat(fitting).extracting(Recipe::getTitle)
                .containsExactly("Simple onion cake");
    }

    @Test
    @DisplayName("a user with no hard requirement sees every recipe")
    void aUserWithoutAHardRequirementSeesEverything() {
        givenUserWith("Mediterranean", "Fitness");
        List<Recipe> all = List.of(recipe("Roast wild boar", "Wild boar"));

        assertThat(service.fitting(USER_ID, all)).isEqualTo(all);
    }

    @Test
    @DisplayName("a user that nobody can read sees every recipe, and the log says so")
    void anUnknownUserSeesEverything() {
        when(userService.getUserById(USER_ID)).thenReturn(null);
        List<Recipe> all = List.of(recipe("Roast wild boar", "Wild boar"));

        assertThat(service.fitting(USER_ID, all)).isEqualTo(all);
    }

    @Test
    @DisplayName("a user without any requirement sees every recipe")
    void aUserWithoutRequirementsSeesEverything() {
        User user = new User();
        user.setDietaryPreferences(null);
        when(userService.getUserById(USER_ID)).thenReturn(user);
        List<Recipe> all = List.of(recipe("Roast wild boar", "Wild boar"));

        assertThat(service.fitting(USER_ID, all)).isEqualTo(all);
    }

    @Test
    @DisplayName("a requirement without a label is left out")
    void aRequirementWithoutALabelIsLeftOut() {
        User user = new User();
        Tag withoutLabel = new Tag();
        user.setDietaryPreferences(new ArrayList<>(Arrays.asList(null, withoutLabel)));
        when(userService.getUserById(USER_ID)).thenReturn(user);
        List<Recipe> all = List.of(recipe("Roast wild boar", "Wild boar"));

        assertThat(service.fitting(USER_ID, all)).isEqualTo(all);
    }

    @Test
    @DisplayName("a list that is null gives an empty list")
    void aListThatIsNullGivesAnEmptyList() {
        assertThat(service.fitting(USER_ID, null)).isEmpty();
    }

    @Test
    @DisplayName("a row without a recipe does not stop the answer")
    void aRowWithoutARecipeDoesNotStopTheAnswer() {
        givenUserWith("Vegan");
        List<Recipe> all = Arrays.asList(null, recipe("Pumpkin soup", "Pumpkin"));

        assertThat(service.fitting(USER_ID, all)).hasSize(2);
    }

    @Test
    @DisplayName("an empty list stays empty")
    void anEmptyListStaysEmpty() {
        givenUserWith("Vegan");

        assertThat(service.fitting(USER_ID, List.of())).isEmpty();
    }
}
