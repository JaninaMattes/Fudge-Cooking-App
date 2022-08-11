package com.foodtracker.foodtrackerapi.common.utils;

public class WordUtils {

    public static String capitalizeString(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') { // You can add other
                                                                                                  // chars here
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    public static String removeHtmlTags(String string){
        return string.replaceAll("\\<[^>]*>","");
    }
    
}
