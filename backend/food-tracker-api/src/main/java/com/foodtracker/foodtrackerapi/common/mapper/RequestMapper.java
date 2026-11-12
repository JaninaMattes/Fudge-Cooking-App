package com.foodtracker.foodtrackerapi.common.mapper;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.foodtracker.foodtrackerapi.common.utils.DateUtils;
import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * <p>
 * Converts the JSON body of a request into the model objects of the
 * application. The REST controllers receive a {@code Map<String, Object>},
 * because the Android client sends a different set of fields for each endpoint.
 * </p>
 *
 * <p>
 * All three controllers (products, shopping list and user) had their own copy of
 * this code. The copies were not equal, which caused different errors for the
 * same input. This class holds the one version that all controllers use.
 * </p>
 *
 * <p>
 * Each method accepts a {@code null} map and a map without the expected keys.
 * The client must not be able to stop the server with an incomplete body.
 * </p>
 *
 * @author Janina Mattes
 */
public final class RequestMapper {

    /**
     * Expiration date for a product that the client sends without a valid date.
     * The product stays visible in the inventory list, and the user can correct
     * the date later.
     */
    public static final LocalDate DEFAULT_EXPIRATION_DATE = LocalDate.of(2022, 2, 2);

    private RequestMapper() {
        // Utility class. Do not make an instance of it.
    }

    /**
     * Reads a product from the body of a request.
     *
     * @param productMap body of the request. It can be {@code null}.
     * @return the product. Fields that the body does not hold are {@code null}.
     */
    public static Product toProduct(Map<String, Object> productMap) {
        Map<String, Object> source = orEmpty(productMap);
        Date expirationDate = toSqlDate(asString(source, "expirationDate"));
        return new Product(
                asInteger(source, "productId"),
                asString(source, "productName"),
                expirationDate,
                asString(source, "quantity"),
                asString(source, "manufacturer"),
                asString(source, "nutritionValue"),
                toImage(asMap(source, "productImage")),
                toTags(asMapList(source, "productTags")));
    }

    /**
     * Reads a list of products from the body of a request.
     *
     * @param productMaps list of products. It can be {@code null}.
     * @return the products. The list is empty if there is no product.
     */
    public static ArrayList<Product> toProducts(List<Map<String, Object>> productMaps) {
        ArrayList<Product> products = new ArrayList<>();
        if (productMaps != null) {
            for (Map<String, Object> productMap : productMaps) {
                products.add(toProduct(productMap));
            }
        }
        return products;
    }

    /**
     * Reads an image from the body of a request.
     *
     * @param imageMap image part of the body. It can be {@code null}.
     * @return the image. Fields that the body does not hold are {@code null}.
     */
    public static Image toImage(Map<String, Object> imageMap) {
        Map<String, Object> source = orEmpty(imageMap);
        return new Image(
                asInteger(source, "imageId"),
                asString(source, "imageName"),
                asString(source, "imageUrl"),
                asString(source, "imageBytes"));
    }

    /**
     * Reads a dietary tag from the body of a request.
     *
     * @param tagMap tag part of the body. It can be {@code null}.
     * @return the tag. Fields that the body does not hold are {@code null}.
     */
    public static Tag toTag(Map<String, Object> tagMap) {
        Map<String, Object> source = orEmpty(tagMap);
        return new Tag(asInteger(source, "tagId"), asString(source, "label"));
    }

    /**
     * Reads a list of dietary tags from the body of a request.
     *
     * @param tagMaps list of tags. It can be {@code null}.
     * @return the tags. The list is empty if there is no tag.
     */
    public static ArrayList<Tag> toTags(List<Map<String, Object>> tagMaps) {
        ArrayList<Tag> tags = new ArrayList<>();
        if (tagMaps != null) {
            for (Map<String, Object> tagMap : tagMaps) {
                tags.add(toTag(tagMap));
            }
        }
        return tags;
    }

    /**
     * Reads an expiration date for the database.
     *
     * @param dateStr date in the ISO format, for example 2022-02-02. It can be
     *                {@code null}.
     * @return the date, or {@link #DEFAULT_EXPIRATION_DATE} if the string is not
     *         a valid ISO date
     */
    public static Date toSqlDate(String dateStr) {
        return DateUtils.toSqlDate(dateStr, DEFAULT_EXPIRATION_DATE);
    }

    /**
     * Converts the switch value of the client into a boolean. The client sends
     * 1 for "on" and 0 for "off".
     *
     * @param value value of the client. It can be {@code null}.
     * @return {@code true} only if the value is 1
     */
    public static Boolean toBoolean(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    /**
     * Gives the value of a key as a text.
     *
     * @param source map to read. It can be {@code null}.
     * @param key    key to read
     * @return the text, or {@code null} if the key has no text value
     */
    public static String asString(Map<String, Object> source, String key) {
        Object value = orEmpty(source).get(key);
        return value instanceof String ? (String) value : null;
    }

    /**
     * Gives the value of a key as a whole number. A number that the JSON parser
     * read as a {@code Long} or a {@code Double} is converted.
     *
     * @param source map to read. It can be {@code null}.
     * @param key    key to read
     * @return the number, or {@code null} if the key has no number value
     */
    public static Integer asInteger(Map<String, Object> source, String key) {
        Object value = orEmpty(source).get(key);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * Gives the value of a key as a nested map.
     *
     * @param source map to read. It can be {@code null}.
     * @param key    key to read
     * @return the nested map, or an empty map if the key has no map value
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asMap(Map<String, Object> source, String key) {
        Object value = orEmpty(source).get(key);
        return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
    }

    /**
     * Gives the value of a key as a list of nested maps.
     *
     * @param source map to read. It can be {@code null}.
     * @param key    key to read
     * @return the list, or an empty list if the key has no list value
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> asMapList(Map<String, Object> source, String key) {
        Object value = orEmpty(source).get(key);
        return value instanceof List ? (List<Map<String, Object>>) value : Collections.emptyList();
    }

    private static Map<String, Object> orEmpty(Map<String, Object> source) {
        return source != null ? source : Collections.emptyMap();
    }
}
