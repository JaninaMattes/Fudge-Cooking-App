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
 * The rules of the requirements of a user, read from a file of the project.
 *
 * <p>Two kinds of requirement, and they are answered by two methods:
 *
 * <ul>
 *   <li>A <strong>medical</strong> requirement — an allergy or an illness —
 *       is a veto. {@link #medicalConflicts(Collection, Collection)} answers
 *       it, and it answers for a label that the file does not know as well:
 *       such a label forbids the ingredient that it names itself, so a user
 *       can write "Celery" and never see celery again.</li>
 *   <li>A <strong>diet</strong> such as Vegan is a rule of the file.
 *       {@link #conflicts(Collection, Collection)} answers it. A label that
 *       the file does not name is a taste, and it hides nothing.</li>
 * </ul>
 *
 * <p>The rules live in {@code diet-rules.properties} and ship with the
 * application, so no suggestion needs a request to the internet (N-14). A
 * word that the file forbids and rescues at the same time stops the start of
 * the application, because such a contradiction would silently serve a user
 * what they cannot eat.
 *
 * @author Janina Mattes
 */
public final class DietRules {

    /** The file of the rules on the class path. */
    public static final String DEFAULT_RESOURCE = "diet-rules.properties";

    /** End of the key that holds the forbidden ingredients. */
    private static final String FORBIDS = ".forbids";

    /** End of the key that holds the exceptions of a rule. */
    private static final String DESPITE = ".despite";

    /** End of a label that names what it forbids, for example "Nut-free". */
    private static final String FREE_OF = "-free";

    private final Map<String, List<String>> forbidden;

    private final Map<String, List<String>> rescued;

    /**
     * Makes the rules out of the pairs of a properties file.
     *
     * @param properties keys such as {@code vegan.forbids} and
     *                   {@code vegan.despite}
     * @throws IllegalStateException when one requirement forbids and rescues
     *                               the same word
     */
    public DietRules(Properties properties) {
        Map<String, List<String>> forbids = new HashMap<>();
        Map<String, List<String>> despite = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (key.endsWith(FORBIDS)) {
                forbids.put(nameOfRequirement(key, FORBIDS), wordsOf(value));
            } else if (key.endsWith(DESPITE)) {
                despite.put(nameOfRequirement(key, DESPITE), wordsOf(value));
            }
        }
        refuseContradictions(forbids, despite);
        this.forbidden = Collections.unmodifiableMap(forbids);
        this.rescued = Collections.unmodifiableMap(despite);
    }

    /**
     * Stops the start when a requirement forbids and rescues one word.
     *
     * @param forbids the forbidden words of each requirement
     * @param despite the rescued words of each requirement
     */
    private static void refuseContradictions(Map<String, List<String>> forbids,
            Map<String, List<String>> despite) {
        for (Map.Entry<String, List<String>> rule : forbids.entrySet()) {
            for (String word : despite.getOrDefault(rule.getKey(), List.of())) {
                if (rule.getValue().contains(word)) {
                    throw new IllegalStateException("The requirement " + rule.getKey()
                            + " forbids '" + word + "' and rescues it at the same time."
                            + " An exception wins, so the rule would not hold.");
                }
            }
        }
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
     * Says whether the file holds a rule for a requirement.
     *
     * @param requirement label of the requirement, for example "Vegan"
     * @return {@code true} when the file names this requirement
     */
    public boolean isHard(String requirement) {
        return forbidden.containsKey(keyOf(requirement));
    }

    /**
     * Says whether a recipe may reach a user, by the diets of the user.
     *
     * @param requirements the diets of the user, may be {@code null}
     * @param ingredients  the ingredients of the recipe, may be {@code null}
     * @return {@code true} when no diet is broken
     */
    public boolean fits(Collection<String> requirements, Collection<Ingredient> ingredients) {
        return conflicts(requirements, ingredients).isEmpty();
    }

    /**
     * Gives the ingredients of a recipe that break a diet of the user.
     *
     * <p>A label that the file does not name is a taste and breaks nothing.
     * For an allergy use {@link #medicalConflicts(Collection, Collection)}.
     *
     * @param requirements the diets of the user, may be {@code null}
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
                collectConflicts(forbids, rescued.getOrDefault(key, List.of()),
                        ingredients, found);
            }
        }
        return found;
    }

    /**
     * Gives the ingredients of a recipe that a user cannot eat.
     *
     * <p>Every requirement counts here, also one that the file does not name:
     * such a label forbids the ingredient that it names itself. "Nut-free",
     * "Nuts" and "Nut" all forbid a nut, and a user who writes "Celery"
     * never sees celery again.
     *
     * @param requirements the medical requirements of the user, may be
     *                     {@code null}
     * @param ingredients  the ingredients of the recipe, may be {@code null}
     * @return the names that the user cannot eat, each one once
     */
    public List<String> medicalConflicts(Collection<String> requirements,
            Collection<Ingredient> ingredients) {
        List<String> found = new ArrayList<>();
        if (requirements == null || ingredients == null) {
            return found;
        }
        for (String requirement : requirements) {
            String key = keyOf(requirement);
            List<String> forbids = forbidden.get(key);
            if (forbids == null) {
                forbids = wordsOfLabel(requirement);
            }
            collectConflicts(forbids, rescued.getOrDefault(key, List.of()),
                    ingredients, found);
        }
        return found;
    }

    /**
     * Gives the word that a label forbids by its own name.
     *
     * @param label the label of a requirement, for example "Nut-free"
     * @return the one word, or an empty list when the label says nothing
     */
    private static List<String> wordsOfLabel(String label) {
        String plain = label == null ? "" : label.trim().toLowerCase(Locale.ROOT);
        if (plain.endsWith(FREE_OF)) {
            plain = plain.substring(0, plain.length() - FREE_OF.length());
        } else if (plain.endsWith(" free")) {
            plain = plain.substring(0, plain.length() - " free".length());
        }
        String word = IngredientMatcher.normalise(plain);
        return word.isEmpty() ? List.of() : List.of(word);
    }

    /**
     * Adds every ingredient that breaks one rule to the answer.
     *
     * @param forbids     the forbidden words of the requirement
     * @param despite     the words that a rule rescues
     * @param ingredients the ingredients of the recipe
     * @param found       the answer, which the method fills
     */
    private static void collectConflicts(List<String> forbids, List<String> despite,
            Collection<Ingredient> ingredients, List<String> found) {
        for (Ingredient ingredient : ingredients) {
            String name = ingredient == null ? ""
                    : IngredientMatcher.normalise(ingredient.getIngredientName());
            if (!name.isEmpty() && !holdsAny(name, despite) && holdsAny(name, forbids)
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
     * @param key the key of the file, for example {@code no-pork.forbids}
     * @param end the end of the key
     * @return the name of the requirement
     */
    private static String nameOfRequirement(String key, String end) {
        return keyOf(key.substring(0, key.length() - end.length()));
    }

    /**
     * Brings the label of a requirement into the form of the keys.
     *
     * @param label the label, for example "No pork"
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
