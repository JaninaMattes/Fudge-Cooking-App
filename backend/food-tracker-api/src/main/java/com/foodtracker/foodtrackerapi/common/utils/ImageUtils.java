package com.foodtracker.foodtrackerapi.common.utils;

import org.apache.tomcat.util.codec.binary.Base64;

/**
 * Utility class to convert images from byte array to base64 encoded string
 * and also vice versa.
 * 
 * @author Janina Mattes
 */
public class ImageUtils {
    
    public static String bytesToBase64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    public static byte[] base64ToBytes(String base64Str) {
        return Base64.decodeBase64(base64Str);
    }
}
