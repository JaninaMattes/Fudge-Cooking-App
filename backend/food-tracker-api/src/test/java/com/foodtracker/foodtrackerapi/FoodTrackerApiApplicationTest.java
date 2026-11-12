package com.foodtracker.foodtrackerapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.filter.CorsFilter;

/**
 * <p>
 * Tests for {@link FoodTrackerApiApplication}.
 * </p>
 *
 * <p>
 * The test does not start the Spring container, because the container needs a
 * PostgreSQL database. The build must run without a database. The test
 * therefore proves the configuration of the beans only.
 * </p>
 *
 * @author Janina Mattes
 */
class FoodTrackerApiApplicationTest {

    @Test
    @DisplayName("The CORS filter runs before all other filters")
    void corsFilterRunsFirst() {
        FilterRegistrationBean<CorsFilter> registration =
                new FoodTrackerApiApplication().corsFilter();

        assertThat(registration.getFilter()).isNotNull();
        assertThat(registration.getOrder()).isZero();
    }

    @Test
    @DisplayName("The CORS filter permits the requests of the Android client")
    void corsFilterPermitsClientRequests() {
        FilterRegistrationBean<CorsFilter> registration =
                new FoodTrackerApiApplication().corsFilter();

        assertThat(registration.getFilter()).isInstanceOf(CorsFilter.class);
        assertThat(registration.getUrlPatterns()).isEmpty();
    }
}
