package com.foodtracker.foodtrackerapi.common.diet;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gives the rules of the dietary requirements to the application.
 *
 * <p>The rules are read once, when the application starts. A file that is
 * missing or broken stops the start, because a suggestion that cannot read
 * the rules would promise a diet that it does not keep.
 *
 * @author Janina Mattes
 */
@Configuration
public class DietRulesConfiguration {

    /**
     * Reads {@link DietRules#DEFAULT_RESOURCE} of the class path.
     *
     * @return the rules of the dietary requirements
     * @throws IOException when the file cannot be read
     */
    @Bean
    public DietRules dietRules() throws IOException {
        return DietRules.fromClasspath(DietRules.DEFAULT_RESOURCE);
    }
}
