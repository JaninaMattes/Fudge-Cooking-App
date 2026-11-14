package com.foodtracker.foodtrackerapi.common.diet;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DietRulesConfiguration}.
 *
 * @author Janina Mattes
 */
class DietRulesConfigurationTest {

    @Test
    @DisplayName("the bean reads the rules of the project")
    void theBeanReadsTheRulesOfTheProject() throws IOException {
        DietRules rules = new DietRulesConfiguration().dietRules();

        assertThat(rules.isHard("Vegan")).isTrue();
    }
}
