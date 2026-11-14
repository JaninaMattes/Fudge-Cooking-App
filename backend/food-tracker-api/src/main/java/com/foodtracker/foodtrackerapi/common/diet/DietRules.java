package com.foodtracker.foodtrackerapi.common.diet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.foodtracker.foodtrackerapi.common.utils.IngredientMatcher;
import com.foodtracker.foodtrackerapi.models.Ingredient;

/**
 * The rules of the dietary requirements, read from a file of the project.
 *
 * <p>A requirement that the file names is a <strong>hard</strong> rule: a
 * recipe that holds one of its forbidden ingredients is never suggested
 * (FR-3.1). A requirement that the file does not name is a preference, such
 * as "Mediterranean" or "Low Carb"; it hides no recipe, and the recommender
 * of FR-R1 will use it to rank one recipe above another.
 *
 * <p>The rules live in {@code diet-rules.properties} and ship with the
 * application, so no suggestion needs a request to the internet (N-14).
 *
 * @author Janina Mattes
 */
public final class DietRules {

    /** The file of the rules on the class path. */
    public static final String DEFAULT_RESOURCE = "diet-rules.properties";

    /** End of the key that holds the forbidden ingredients. */
    private static final String FORBIDS = ".forbids";

    /** End of the key that holds the exceptions. */
    private static final String ALLOWS = ".allows";

    private final Map<String, List<String>> forbidden;

    private final Map<String, List<String>> allowed;

    /**
     * Makes the rules out of the pairs of a properties file.
     *
     * @param properties keys such as {@code vegan.forbids} and
     *                   {@code vegan.allows}
     */
    public DietRules(Properties properties) {
        Map<String, List<String>> forbids = new HashMap<>();
        Map<String, List<String>> allows = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (key.endsWith(FORBIDS)) {
                forbids.put(nameOfRequirement(key, FORBIDS), wordsOf(value));
            } else if (key.endsWith(ALLOWS)) {
                allows.put(nameOfRequirement(key, ALLOWS), wordsOf(value));
            }
        }
        this.forbidden = Collections.unmodifiableMap(forbids);
        this.allowed = Collections.unmodifiableMap(allows);
    }

    /**
     * Reads the rules from a file of the class path.
     *
     * @param resource name of the file, for example {@link #DEFAULT_RESOURCE}
     * @return the rules of that file
     * @throws IOException          when the file cannot be read
     * @throws IllegalStateException when the file is not on the class path
     */
    public static DietRules fromClasspath(String resource) throws IOException {
        try (InputStream stream = streamOf(resource);
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            properties.load(reader);
            return new DietRules(properties);
        }
    }

    /**
     * Opens a file of the class path.
     *
     * @param resource name of the file
     * @return the open file
     */
    private static InputStream streamOf(String resource) {
        InputStream stream = DietRules.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalStateException("The file " + resource
                    + " is not on the class path");
        }
        return stream;
    }

    /**
     * Says whether a requirement hides a recipe at all.
     *
     * @param requirement label of the requirement, for example "Vegan"
     * @return {@code true} when the file names forbidden ingredients for it
     */
    public boolean isHard(String requirement) {
        return forbidden.containsKey(keyOf(requirement));
    }

    /**
     * Says whether a recipe may reach a user.
     *
     * @param requirements the requirements of the user, may be {@code null}
     * @param ingredients  the ingredients of the recipe, may be {@code null}
     * @return {@code true} when no requirement is broken
     */
    public boolean fits(Collection<String> requirements, Collection<Ingredient> ingredients) {
        return conflicts(requirements, ingredients).isEmpty();
    }

    /**
     * Gives the ingredients of a recipe that break a requirement of the user.
     *
     * <p>The answer carries the names as the recipe writes them, so that a
     * log entry or a mark on the screen can say why a recipe was left out.
     *
     * @param requirements the requirements of the user, may be {@code null}
     * @param ingredients  the ingredients of the recipe, may be {@code null}
     * @return the names that break a rule, each one once
     */
    public List<String> conflicts(Collection<String> requirements,
            Collection<Ingredient> ingredients) {
        List<String> found = new ArrayList<>();
        if (requirements == null || ingredients == null) {
            return found;
        }
        for (String requirement : requirements) {
            String key = keyOf(requirement);
            List<String> forbids = forbidden.get(key);
            if (forbids != null) {
                collectConflicts(forbids, allowed.getOrDefault(key, List.of()),
                        ingredients, found);
            }
        }
        return found;
    }

    /**
     * Adds every ingredient that breaks one rule to the answer.
     *
     * @param forbids     the forbidden words of the requirement
     * @param allows      the words that stay allowed
     * @param ingredients the ingredients of the recipe
     * @param found       the answer, which the method fills
     */
    private static void collectConflicts(List<String> forbids, List<String> allows,
            Collection<Ingredient> ingredients, List<String> found) {
        for (Ingredient ingredient : ingredients) {
            String name = ingredient == null ? ""
                    : IngredientMatcher.normalise(ingredient.getIngredientName());
            if (!name.isEmpty() && !holdsAny(name, allows) && holdsAny(name, forbids)
                    && !found.contains(ingredient.getIngredientName())) {
                found.add(ingredient.getIngredientName());
            }
        }
    }

    /**
     * Says whether a name holds one of the words.
     *
     * @param name  the name of an ingredient, already in the plain form
     * @param words the words to look for
     * @return {@code true} when one word stands in the name
     */
    private static boolean holdsAny(String name, List<String> words) {
        for (String word : words) {
            if (name.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gives the name of the requirement that a key belongs to.
     *
     * @param key the key of the file, for example {@code no-pig.forbids}
     * @param end the end of the key
     * @return the name of the requirement
     */
    private static String nameOfRequirement(String key, String end) {
        return keyOf(key.substring(0, key.length() - end.length()));
    }

    /**
     * Brings the label of a requirement into the form of the keys.
     *
     * @param label the label, for example "No Pig"
     * @return the label in lower case and with a dash for a space
     */
    private static String keyOf(String label) {
        return label == null ? ""
                : label.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    /**
     * Reads a list of words that a comma separates.
     *
     * @param value the value of one key, may be empty
     * @return the words, in the plain form of the comparison
     */
    private static List<String> wordsOf(String value) {
        List<String> words = new ArrayList<>();
        for (String part : value.split(",")) {
            String word = IngredientMatcher.normalise(part);
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }
}
