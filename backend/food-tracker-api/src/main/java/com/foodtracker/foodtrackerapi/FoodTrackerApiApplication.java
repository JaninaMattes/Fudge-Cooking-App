package com.foodtracker.foodtrackerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * <p>
 * Start class of the REST API. The API tracks the food of a user, keeps the
 * shopping lists and suggests recipes for the products that expire soon.
 * </p>
 *
 * <p>
 * Start the application with {@code mvn spring-boot:run}. The API then listens
 * on the port that {@code application.properties} gives.
 * </p>
 *
 * @author Janina Mattes
 */
@SpringBootApplication
public class FoodTrackerApiApplication {

    /**
     * Starts the Spring container.
     *
     * @param args arguments of the command line. Spring reads them as
     *             configuration values.
     */
    public static void main(String[] args) {
        SpringApplication.run(FoodTrackerApiApplication.class, args);
    }

    /**
     * Permits requests from the Android client. The client and the API are on
     * different hosts, and a browser or a WebView blocks such a request without
     * these headers.
     *
     * @return the registration of the CORS filter
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        source.registerCorsConfiguration("/**", config);
        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
