package com.foodtracker.foodtrackerapi.common.utils;

/**
 * <p>
 * Makes the text that comes from the external recipe APIs ready to show. The
 * external text has a mixed letter case and can hold HTML tags.
 * </p>
 *
 * @author Janina Mattes
 */
public final class WordUtils {

    /** Characters that start a new word. */
    private static final String WORD_SEPARATORS = ".'";

    private WordUtils() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Writes the first letter of each word with a capital letter and all other
     * letters with a small letter. A space, a point and an apostrophe start a
     * new word.
     *
     * @param text text to change. It can be {@code null}.
     * @return the changed text, or {@code null} if the text is {@code null}
     */
    public static String capitalizeString(String text) {
        if (text == null) {
            return null;
        }
        char[] chars = text.toLowerCase().toCharArray();
        boolean atWordStart = true;
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (atWordStart && Character.isLetter(current)) {
                chars[i] = Character.toUpperCase(current);
                atWordStart = false;
            } else if (Character.isWhitespace(current) || WORD_SEPARATORS.indexOf(current) >= 0) {
                atWordStart = true;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * Removes all HTML tags from the text.
     *
     * @param text text to clean. It can be {@code null}.
     * @return the text without HTML tags, or {@code null} if the text is
     *         {@code null}
     */
    public static String removeHtmlTags(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<[^>]*>", "");
    }
}
