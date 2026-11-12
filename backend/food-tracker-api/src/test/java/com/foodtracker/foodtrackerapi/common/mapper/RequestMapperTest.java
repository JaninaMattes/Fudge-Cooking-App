package com.foodtracker.foodtrackerapi.common.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Tag;

/**
 * Tests for {@link RequestMapper}.
 *
 * @author Janina Mattes
 */
class RequestMapperTest {

    private static Map<String, Object> tagMap(int tagId, String label) {
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("tagId", tagId);
        tag.put("label", label);
        return tag;
    }

    private static Map<String, Object> fullProductMap() {
        Map<String, Object> image = new LinkedHashMap<>();
        image.put("imageId", 7);
        image.put("imageName", "Milk");
        image.put("imageUrl", "http://example.org/milk.png");
        image.put("imageBytes", "RmVlZE1l");

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("productId", 42);
        product.put("productName", "Milk");
        product.put("expirationDate", "2022-02-02");
        product.put("quantity", "1 Package");
        product.put("manufacturer", "Dairy Ltd");
        product.put("nutritionValue", "250 kcal");
        product.put("productImage", image);
        product.put("productTags", Arrays.asList(tagMap(1, "Vegan"), tagMap(2, "Lactose")));
        return product;
    }

    @Nested
    @DisplayName("toProduct")
    class ToProduct {

        @Test
        @DisplayName("reads all fields of a complete body")
        void readsCompleteBody() {
            Product product = RequestMapper.toProduct(fullProductMap());

            assertThat(product.getProductId()).isEqualTo(42);
            assertThat(product.getProductName()).isEqualTo("Milk");
            assertThat(product.getExpirationDate()).isEqualTo(Date.valueOf("2022-02-02"));
            assertThat(product.getQuantity()).isEqualTo("1 Package");
            assertThat(product.getManufacturer()).isEqualTo("Dairy Ltd");
            assertThat(product.getNutritionValue()).isEqualTo("250 kcal");
            assertThat(product.getProductImage().getImageId()).isEqualTo(7);
            assertThat(product.getProductTags()).extracting(Tag::getLabel)
                    .containsExactly("Vegan", "Lactose");
        }

        @Test
        @DisplayName("accepts a body without the image and without the tags")
        void acceptsBodyWithoutImageAndTags() {
            Map<String, Object> source = new HashMap<>();
            source.put("productName", "Bread");

            Product product = RequestMapper.toProduct(source);

            assertThat(product.getProductName()).isEqualTo("Bread");
            assertThat(product.getProductId()).isNull();
            assertThat(product.getProductTags()).isEmpty();
            assertThat(product.getProductImage()).isNotNull();
            assertThat(product.getProductImage().getImageId()).isNull();
        }

        @Test
        @DisplayName("accepts a body that is null")
        void acceptsNullBody() {
            Product product = RequestMapper.toProduct(null);

            assertThat(product).isNotNull();
            assertThat(product.getProductName()).isNull();
            assertThat(product.getExpirationDate())
                    .isEqualTo(Date.valueOf(RequestMapper.DEFAULT_EXPIRATION_DATE));
        }

        @Test
        @DisplayName("uses the default date when the date is not valid")
        void usesDefaultDateForInvalidDate() {
            Map<String, Object> source = new HashMap<>();
            source.put("expirationDate", "02.02.2022");

            Product product = RequestMapper.toProduct(source);

            assertThat(product.getExpirationDate())
                    .isEqualTo(Date.valueOf(RequestMapper.DEFAULT_EXPIRATION_DATE));
        }
    }

    @Nested
    @DisplayName("toProducts")
    class ToProducts {

        @Test
        @DisplayName("reads each product of the list")
        void readsEachProduct() {
            List<Map<String, Object>> source = Arrays.asList(fullProductMap(), fullProductMap());

            assertThat(RequestMapper.toProducts(source)).hasSize(2);
        }

        @Test
        @DisplayName("gives an empty list when there is no product")
        void givesEmptyListForNull() {
            assertThat(RequestMapper.toProducts(null)).isEmpty();
            assertThat(RequestMapper.toProducts(Collections.emptyList())).isEmpty();
        }
    }

    @Nested
    @DisplayName("toImage")
    class ToImage {

        @Test
        @DisplayName("reads all fields of a complete body")
        void readsCompleteBody() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("imageId", 7);
            source.put("imageName", "Profilbild");
            source.put("imageUrl", "http://example.org/user.png");
            source.put("imageBytes", "RmVlZE1l");

            Image image = RequestMapper.toImage(source);

            assertThat(image.getImageId()).isEqualTo(7);
            assertThat(image.getImageName()).isEqualTo("Profilbild");
            assertThat(image.getImageUrl()).isEqualTo("http://example.org/user.png");
            assertThat(image.getBase64ImageString()).isEqualTo("RmVlZE1l");
        }

        @Test
        @DisplayName("accepts a body that is null")
        void acceptsNullBody() {
            Image image = RequestMapper.toImage(null);

            assertThat(image).isNotNull();
            assertThat(image.getImageId()).isNull();
        }
    }

    @Nested
    @DisplayName("toTag and toTags")
    class ToTags {

        @Test
        @DisplayName("toTag reads the key and the name")
        void toTagReadsFields() {
            Tag tag = RequestMapper.toTag(tagMap(3, "Vegetarian"));

            assertThat(tag.getTagId()).isEqualTo(3);
            assertThat(tag.getLabel()).isEqualTo("Vegetarian");
        }

        @Test
        @DisplayName("toTag accepts a body that is null")
        void toTagAcceptsNullBody() {
            assertThat(RequestMapper.toTag(null).getTagId()).isNull();
        }

        @Test
        @DisplayName("toTags reads each tag of the list")
        void toTagsReadsEachTag() {
            assertThat(RequestMapper.toTags(Arrays.asList(tagMap(1, "A"), tagMap(2, "B"))))
                    .hasSize(2);
        }

        @Test
        @DisplayName("toTags gives an empty list when there is no tag")
        void toTagsGivesEmptyListForNull() {
            assertThat(RequestMapper.toTags(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Reading of single values")
    class SingleValues {

        @Test
        @DisplayName("asString gives the text of the key")
        void asStringGivesText() {
            assertThat(RequestMapper.asString(Collections.singletonMap("a", "b"), "a"))
                    .isEqualTo("b");
        }

        @Test
        @DisplayName("asString gives null when the value is not a text")
        void asStringGivesNullForOtherType() {
            assertThat(RequestMapper.asString(Collections.singletonMap("a", 1), "a")).isNull();
            assertThat(RequestMapper.asString(null, "a")).isNull();
            assertThat(RequestMapper.asString(Collections.emptyMap(), "a")).isNull();
        }

        @Test
        @DisplayName("asInteger gives the number of the key")
        void asIntegerGivesNumber() {
            assertThat(RequestMapper.asInteger(Collections.singletonMap("a", 1), "a"))
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("asInteger converts a number of another type")
        void asIntegerConvertsOtherNumbers() {
            assertThat(RequestMapper.asInteger(Collections.singletonMap("a", 2L), "a"))
                    .isEqualTo(2);
            assertThat(RequestMapper.asInteger(Collections.singletonMap("a", 3.7D), "a"))
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("asInteger gives null when the value is not a number")
        void asIntegerGivesNullForOtherType() {
            assertThat(RequestMapper.asInteger(Collections.singletonMap("a", "b"), "a")).isNull();
            assertThat(RequestMapper.asInteger(null, "a")).isNull();
        }

        @Test
        @DisplayName("asMap gives the nested map of the key")
        void asMapGivesNestedMap() {
            Map<String, Object> nested = Collections.singletonMap("b", "c");

            assertThat(RequestMapper.asMap(Collections.singletonMap("a", nested), "a"))
                    .isEqualTo(nested);
        }

        @Test
        @DisplayName("asMap gives an empty map when the value is not a map")
        void asMapGivesEmptyMapForOtherType() {
            assertThat(RequestMapper.asMap(Collections.singletonMap("a", "b"), "a")).isEmpty();
            assertThat(RequestMapper.asMap(null, "a")).isEmpty();
        }

        @Test
        @DisplayName("asMapList gives the list of the key")
        void asMapListGivesList() {
            List<Map<String, Object>> nested = Collections.singletonList(tagMap(1, "A"));

            assertThat(RequestMapper.asMapList(Collections.singletonMap("a", nested), "a"))
                    .isEqualTo(nested);
        }

        @Test
        @DisplayName("asMapList gives an empty list when the value is not a list")
        void asMapListGivesEmptyListForOtherType() {
            assertThat(RequestMapper.asMapList(Collections.singletonMap("a", "b"), "a")).isEmpty();
            assertThat(RequestMapper.asMapList(null, "a")).isEmpty();
        }

        @Test
        @DisplayName("toBoolean gives true only for the value 1")
        void toBooleanGivesTrueForOne() {
            assertThat(RequestMapper.toBoolean(1)).isTrue();
            assertThat(RequestMapper.toBoolean(0)).isFalse();
            assertThat(RequestMapper.toBoolean(2)).isFalse();
            assertThat(RequestMapper.toBoolean(null)).isFalse();
        }

        @Test
        @DisplayName("toSqlDate uses the default date for a value that is not a date")
        void toSqlDateUsesDefaultDate() {
            assertThat(RequestMapper.toSqlDate(null))
                    .isEqualTo(Date.valueOf(RequestMapper.DEFAULT_EXPIRATION_DATE));
            assertThat(RequestMapper.toSqlDate("2021-12-24"))
                    .isEqualTo(Date.valueOf("2021-12-24"));
        }
    }
}
