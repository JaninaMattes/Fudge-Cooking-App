package com.foodtracker.foodtrackerapi.common.diet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.models.Ingredient;

/**
 * Tests for {@link DietRules}.
 *
 * <p>The happy path is one line: a vegan user never sees beef. The rest of
 * this class holds the cases that make such a rule hard — a word that stands
 * inside another word, a name that only looks forbidden, a requirement that
 * is a taste and not a rule, and a list that holds nothing.
 *
 * @author Janina Mattes
 */
class DietRulesTest {

    private static DietRules rules;

    @BeforeAll
    static void readTheRules() throws IOException {
        rules = DietRules.fromClasspath(DietRules.DEFAULT_RESOURCE);
    }

    private static Ingredient ingredient(String name) {
        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientName(name);
        return ingredient;
    }

    private static List<Ingredient> ingredients(String... names) {
        return Arrays.stream(names).map(DietRulesTest::ingredient).toList();
    }

    @Test
    @DisplayName("a vegan user never sees beef")
    void aVeganNeverSeesBeef() {
        assertThat(rules.fits(List.of("Vegan"), ingredients("Beef", "Onions"))).isFalse();
        assertThat(rules.conflicts(List.of("Vegan"), ingredients("Beef", "Onions")))
                .containsExactly("Beef");
    }

    @Test
    @DisplayName("a forbidden word inside a name counts: minced meat is meat")
    void aForbiddenWordInsideANameCounts() {
        assertThat(rules.fits(List.of("Vegetarian"), ingredients("Minced meat"))).isFalse();
        assertThat(rules.fits(List.of("Vegan"), ingredients("Wild boar"))).isFalse();
    }

    @Test
    @DisplayName("an exception wins: coconut milk and eggplant stay vegan")
    void anExceptionWins() {
        assertThat(rules.fits(List.of("Vegan"), ingredients("Coconut milk"))).isTrue();
        assertThat(rules.fits(List.of("Vegan"), ingredients("Eggplant"))).isTrue();
        assertThat(rules.fits(List.of("Vegan"), ingredients("Peanut butter"))).isTrue();
    }

    @Test
    @DisplayName("upper case and spaces around a name change nothing")
    void caseAndSpacesChangeNothing() {
        assertThat(rules.fits(List.of("Vegan"), ingredients("  CHICKEN "))).isFalse();
        assertThat(rules.fits(List.of("  vegan  "), ingredients("Chicken"))).isFalse();
    }

    @Test
    @DisplayName("a vegetarian may eat an egg, a vegan may not")
    void aVegetarianMayEatAnEgg() {
        assertThat(rules.fits(List.of("Vegetarian"), ingredients("Eggs", "Milk"))).isTrue();
        assertThat(rules.fits(List.of("Vegan"), ingredients("Eggs"))).isFalse();
    }

    @Test
    @DisplayName("a requirement with a space in its label works: No Pig")
    void aRequirementWithASpaceWorks() {
        assertThat(rules.isHard("No Pig")).isTrue();
        assertThat(rules.fits(List.of("No Pig"), ingredients("Bacon"))).isFalse();
        assertThat(rules.fits(List.of("No Pig"), ingredients("Chicken"))).isTrue();
    }

    @Test
    @DisplayName("several requirements hold at the same time")
    void severalRequirementsHoldTogether() {
        List<String> both = List.of("Vegetarian", "No Pig");

        assertThat(rules.conflicts(both, ingredients("Ham", "Onions")))
                .containsExactly("Ham");
        assertThat(rules.fits(both, ingredients("Onions", "Flour"))).isTrue();
    }

    @Test
    @DisplayName("a taste is not a rule: Mediterranean and Low Carb hide nothing")
    void aTasteIsNotARule() {
        assertThat(rules.isHard("Mediterranean")).isFalse();
        assertThat(rules.isHard("Low Carb")).isFalse();
        assertThat(rules.fits(List.of("Mediterranean", "Low Carb"),
                ingredients("Beef", "Flour"))).isTrue();
    }

    @Test
    @DisplayName("the same forbidden ingredient is named once")
    void theSameConflictIsNamedOnce() {
        assertThat(rules.conflicts(List.of("Vegan", "Vegetarian"),
                ingredients("Beef", "Beef"))).containsExactly("Beef");
    }

    @Test
    @DisplayName("a list that is null and a row without a name give no answer")
    void listsThatAreNullGiveNoAnswer() {
        assertThat(rules.conflicts(null, ingredients("Beef"))).isEmpty();
        assertThat(rules.conflicts(List.of("Vegan"), null)).isEmpty();
        assertThat(rules.conflicts(List.of("Vegan"),
                Arrays.asList(null, ingredient(null), ingredient("  ")))).isEmpty();
        assertThat(rules.conflicts(Collections.singletonList(null),
                ingredients("Beef"))).isEmpty();
        assertThat(rules.isHard(null)).isFalse();
    }

    @Test
    @DisplayName("a requirement without words forbids nothing")
    void aRequirementWithoutWordsForbidsNothing() {
        Properties properties = new Properties();
        properties.setProperty("empty.forbids", "  ,  ");
        properties.setProperty("empty.allows", "");
        properties.setProperty("noise", "this key belongs to no requirement");
        DietRules own = new DietRules(properties);

        assertThat(own.isHard("empty")).isTrue();
        assertThat(own.fits(List.of("empty"), ingredients("Beef"))).isTrue();
        assertThat(own.isHard("noise")).isFalse();
    }

    @Test
    @DisplayName("a file that is not on the class path stops the start")
    void aFileThatIsMissingStopsTheStart() {
        assertThatThrownBy(() -> DietRules.fromClasspath("no-such-rules.properties"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no-such-rules.properties");
    }

    @Test
    @DisplayName("the file of the project knows the requirements of the demo data")
    void theFileKnowsTheRequirementsOfTheDemoData() {
        assertThat(rules.isHard("Vegan")).isTrue();
        assertThat(rules.isHard("Vegetarian")).isTrue();
        assertThat(rules.isHard("Kosher")).isTrue();
    }
}
