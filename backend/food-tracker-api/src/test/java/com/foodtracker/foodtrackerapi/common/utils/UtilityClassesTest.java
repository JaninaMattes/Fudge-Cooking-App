package com.foodtracker.foodtrackerapi.common.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.foodtracker.foodtrackerapi.common.mapper.RequestMapper;
import com.foodtracker.foodtrackerapi.common.rest.JsonResponses;

/**
 * <p>
 * Tests that each utility class holds static methods only. Such a class must
 * have one private constructor, so that nobody makes an instance of it.
 * </p>
 *
 * @author Janina Mattes
 */
class UtilityClassesTest {

    @ParameterizedTest
    @DisplayName("A utility class has one private constructor only")
    @ValueSource(classes = {
            Attempt.class,
            DateUtils.class,
            ImageUtils.class,
            WordUtils.class,
            RequestMapper.class,
            JsonResponses.class
    })
    void utilityClassHasPrivateConstructor(Class<?> utilityClass) throws Exception {
        Constructor<?>[] constructors = utilityClass.getDeclaredConstructors();

        assertThat(constructors).hasSize(1);
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
        assertThat(Modifier.isFinal(utilityClass.getModifiers())).isTrue();

        // Make one instance, so that the constructor counts as tested.
        constructors[0].setAccessible(true);
        assertThat(constructors[0].newInstance()).isNotNull();
    }
}
