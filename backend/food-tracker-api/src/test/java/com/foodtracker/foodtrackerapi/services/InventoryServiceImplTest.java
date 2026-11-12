package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.foodtracker.foodtrackerapi.models.Image;
import com.foodtracker.foodtrackerapi.models.Inventorylist;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.models.Settings;
import com.foodtracker.foodtrackerapi.models.Tag;
import com.foodtracker.foodtrackerapi.repositories.InventoryRepository;
import com.foodtracker.foodtrackerapi.repositories.UserRepository;

/**
 * Tests for {@link InventoryServiceImpl}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository productRepository;

    @Mock
    private UserRepository userRepository;

    private InventoryServiceImpl service;

    private static Product product(Integer id, String name) {
        return new Product(id, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    private static Product productWithTags(Integer id, String... labels) {
        ArrayList<Tag> tags = new ArrayList<>();
        for (String label : labels) {
            tags.add(new Tag(null, label));
        }
        return new Product(id, "Milk", Date.valueOf("2022-02-02"), "1 Package", "Dairy",
                "250 kcal", tags);
    }

    @BeforeEach
    void setUp() {
        service = new InventoryServiceImpl(productRepository, userRepository);
    }

    @Nested
    @DisplayName("addProductToInventoryList")
    class AddProduct {

        @Test
        @DisplayName("stores the product with its tags and its image")
        void storesProductWithTagsAndImage() {
            Product product = productWithTags(null, "Vegan", "Lactose");
            product.setProductImage(new Image(0, "Milk", "url", "RmVlZE1l"));
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);
            when(productRepository.createDietryTag(anyString())).thenReturn(2);

            assertThat(service.addProductToInventoryList(product, 1)).isEqualTo(42);

            verify(productRepository).createDietryTag("Vegan");
            verify(productRepository).createDietryTag("Lactose");
            verify(productRepository, org.mockito.Mockito.times(2)).createProductTag(eq(42),
                    eq(2));
            verify(productRepository).createProductImage(eq(42), any(Image.class));
        }

        @Test
        @DisplayName("stores one empty tag for a product without tags")
        void storesEmptyTagWithoutTags() {
            Product product = product(null, "Milk");
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);
            when(productRepository.createDietryTag("")).thenReturn(2);

            service.addProductToInventoryList(product, 1);

            verify(productRepository).createDietryTag("");
            verify(productRepository).createProductTag(42, 2);
        }

        @Test
        @DisplayName("stores one empty tag for a product whose list of tags is null")
        void storesEmptyTagForNullTagList() {
            Product product = product(null, "Milk");
            product.setProductTags(null);
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);
            when(productRepository.createDietryTag("")).thenReturn(2);

            service.addProductToInventoryList(product, 1);

            verify(productRepository).createProductTag(42, 2);
        }

        @Test
        @DisplayName("makes no link when the database gives no key for a tag of the product")
        void makesNoLinkWhenTagOfProductHasNoKey() {
            Product product = productWithTags(null, "Vegan");
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);
            when(productRepository.createDietryTag("Vegan")).thenReturn(0);

            service.addProductToInventoryList(product, 1);

            verify(productRepository, never()).createProductTag(anyInt(), anyInt());
        }

        @Test
        @DisplayName("makes no link when the database gives no key for the tag")
        void makesNoLinkWithoutTagKey() {
            Product product = product(null, "Milk");
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);
            when(productRepository.createDietryTag("")).thenReturn(0);

            service.addProductToInventoryList(product, 1);

            verify(productRepository, never()).createProductTag(anyInt(), anyInt());
        }

        @Test
        @DisplayName("stores a default image for a product without an image")
        void storesDefaultImageWithoutImage() {
            Product product = product(null, "Milk");
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(42);

            service.addProductToInventoryList(product, 1);

            ArgumentCaptor<Image> image = ArgumentCaptor.forClass(Image.class);
            verify(productRepository).createProductImage(eq(42), image.capture());
            assertThat(image.getValue().getImageName()).isEqualTo("Product Image");
            assertThat(image.getValue().getBase64ImageString()).isNull();
        }

        @Test
        @DisplayName("stores no tag and no image when the product was not created")
        void storesNothingWhenProductWasNotCreated() {
            Product product = product(null, "Milk");
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.createProductInInventoryList(product, 5)).thenReturn(0);

            assertThat(service.addProductToInventoryList(product, 1)).isZero();

            verify(productRepository, never()).createDietryTag(anyString());
            verify(productRepository, never()).createProductImage(anyInt(), any());
        }
    }

    @Nested
    @DisplayName("updateProductOnInventoryListById")
    class UpdateProduct {

        @Test
        @DisplayName("changes the image when the product has one")
        void changesImageOfProduct() {
            Product product = product(42, "Milk");
            product.setProductImage(new Image(7, "Milk", "url", "RmVlZE1l"));
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.updateProductInInventoryList(1, product, 5)).thenReturn(1);

            assertThat(service.updateProductOnInventoryListById(product, 1)).isEqualTo(1);

            verify(productRepository).updateProductImage(product.getProductImage());
        }

        @Test
        @DisplayName("changes no image when the image has no key")
        void changesNoImageWithoutKey() {
            Product product = product(42, "Milk");
            product.setProductImage(new Image(0, "Milk"));
            when(productRepository.getInventoryListId(1)).thenReturn(5);

            service.updateProductOnInventoryListById(product, 1);

            verify(productRepository, never()).updateProductImage(any());
        }

        @Test
        @DisplayName("changes no image when the product has none")
        void changesNoImageWithoutImage() {
            Product product = product(42, "Milk");
            when(productRepository.getInventoryListId(1)).thenReturn(5);

            service.updateProductOnInventoryListById(product, 1);

            verify(productRepository, never()).updateProductImage(any());
        }

        @Test
        @DisplayName("gives 0 when the user has no inventory list")
        void givesZeroWithoutInventoryList() {
            when(productRepository.getInventoryListId(1)).thenReturn(0);

            assertThat(service.updateProductOnInventoryListById(product(42, "Milk"), 1)).isZero();

            verify(productRepository, never()).updateProductInInventoryList(anyInt(), any(),
                    anyInt());
        }
    }

    @Nested
    @DisplayName("updateAllProductsOnInventoryListByID")
    class UpdateAllProducts {

        @Test
        @DisplayName("counts the rows of all products that have a key")
        void countsRowsOfProductsWithKey() {
            ArrayList<Product> products = new ArrayList<>(Arrays.asList(
                    product(42, "Milk"), product(43, "Bread"), product(0, "No key")));
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.updateProductInInventoryList(eq(1), any(), eq(5)))
                    .thenReturn(1);

            assertThat(service.updateAllProductsOnInventoryListByID(1, products)).isEqualTo(2);
        }

        @Test
        @DisplayName("gives 0 for a list that is empty or null")
        void givesZeroForEmptyList() {
            assertThat(service.updateAllProductsOnInventoryListByID(1, new ArrayList<>()))
                    .isZero();
            assertThat(service.updateAllProductsOnInventoryListByID(1, null)).isZero();

            verify(productRepository, never()).getInventoryListId(anyInt());
        }
    }

    @Nested
    @DisplayName("countExpiringProducts")
    class CountExpiringProducts {

        @Test
        @DisplayName("counts the products when the user gave permission")
        void countsProductsWithPermission() {
            when(userRepository.getUserSettingsById(1))
                    .thenReturn(new Settings(3, true, false, false));
            when(productRepository.countExpiringProducts(1, 3)).thenReturn(2);

            assertThat(service.countExpiringProducts(1, 3)).isEqualTo(2);
        }

        @Test
        @DisplayName("gives 0 when the user gave no permission")
        void givesZeroWithoutPermission() {
            when(userRepository.getUserSettingsById(1))
                    .thenReturn(new Settings(3, false, false, false));

            assertThat(service.countExpiringProducts(1, 3)).isZero();

            verify(productRepository, never()).countExpiringProducts(anyInt(), anyInt());
        }

        @Test
        @DisplayName("gives 0 when the user has no settings")
        void givesZeroWithoutSettings() {
            when(userRepository.getUserSettingsById(1)).thenReturn(null);

            assertThat(service.countExpiringProducts(1, 3)).isZero();
        }

        @Test
        @DisplayName("gives 0 when the permission is null")
        void givesZeroForNullPermission() {
            Settings settings = new Settings(3);
            settings.setAllowPushNotifications(null);
            when(userRepository.getUserSettingsById(1)).thenReturn(settings);

            assertThat(service.countExpiringProducts(1, 3)).isZero();
        }
    }

    @Nested
    @DisplayName("deleteProductOnInventoryListById")
    class DeleteProduct {

        @Test
        @DisplayName("deletes the tags, the image and then the product")
        void deletesTagsImageAndProduct() {
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.getProductTags(42)).thenReturn(Optional.of(
                    new ArrayList<>(Arrays.asList(new Tag(2, "Vegan")))));
            when(productRepository.deleteProductTagById(42, 2)).thenReturn(1);
            when(productRepository.deleteProductOnInventoryList(42)).thenReturn(1);

            assertThat(service.deleteProductOnInventoryListById(1, 42)).isEqualTo(1);

            verify(productRepository).deleteProductTagById(42, 2);
            verify(productRepository).deleteDietryTagById(2);
            verify(productRepository).deleteProductImage(42);
            verify(productRepository).deleteProductOnInventoryList(42);
        }

        @Test
        @DisplayName("deletes a product that has no tag")
        void deletesProductWithoutTag() {
            // The method deleted nothing before when the product had no tag.
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.getProductTags(42)).thenReturn(Optional.of(new ArrayList<>()));
            when(productRepository.deleteProductOnInventoryList(42)).thenReturn(1);

            assertThat(service.deleteProductOnInventoryListById(1, 42)).isEqualTo(1);

            verify(productRepository).deleteProductImage(42);
            verify(productRepository).deleteProductOnInventoryList(42);
        }

        @Test
        @DisplayName("keeps the dietary tag when the link stays in the database")
        void keepsTagWhenLinkStays() {
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.getProductTags(42)).thenReturn(Optional.of(
                    new ArrayList<>(Arrays.asList(new Tag(2, "Vegan")))));
            when(productRepository.deleteProductTagById(42, 2)).thenReturn(0);

            service.deleteProductOnInventoryListById(1, 42);

            verify(productRepository, never()).deleteDietryTagById(anyInt());
        }

        @Test
        @DisplayName("gives 0 when the user has no inventory list")
        void givesZeroWithoutInventoryList() {
            when(productRepository.getInventoryListId(1)).thenReturn(0);

            assertThat(service.deleteProductOnInventoryListById(1, 42)).isZero();

            verify(productRepository, never()).deleteProductOnInventoryList(anyInt());
        }

        @Test
        @DisplayName("accepts a product without tags in the answer of the database")
        void acceptsEmptyTagAnswer() {
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.getProductTags(42)).thenReturn(Optional.empty());
            when(productRepository.deleteProductOnInventoryList(42)).thenReturn(1);

            assertThat(service.deleteProductOnInventoryListById(1, 42)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("removeProductOnInventoryListById")
    class RemoveProduct {

        @Test
        @DisplayName("only clears the foreign key of the product")
        void clearsForeignKeyOnly() {
            when(productRepository.getInventoryListId(1)).thenReturn(5);
            when(productRepository.removeProductFromInventoryList(5, 42)).thenReturn(1);

            assertThat(service.removeProductOnInventoryListById(1, 42)).isEqualTo(1);
        }

        @Test
        @DisplayName("gives 0 for a user key that is 0 or null")
        void givesZeroForEmptyUserKey() {
            assertThat(service.removeProductOnInventoryListById(0, 42)).isZero();
            assertThat(service.removeProductOnInventoryListById(null, 42)).isZero();

            verify(productRepository, never()).getInventoryListId(anyInt());
        }

        @Test
        @DisplayName("gives 0 when the user has no inventory list")
        void givesZeroWithoutInventoryList() {
            when(productRepository.getInventoryListId(1)).thenReturn(0);

            assertThat(service.removeProductOnInventoryListById(1, 42)).isZero();
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("Each operation gives the answer of the repository")
        void operationsGiveAnswerOfRepository() {
            List<Product> expiring = Collections.singletonList(product(42, "Milk"));
            Optional<ArrayList<Product>> expiringList = Optional.of(new ArrayList<>(expiring));
            Optional<Inventorylist> inventoryList = Optional.of(new Inventorylist());
            when(productRepository.getProductWithShortExpDate(1)).thenReturn(expiring);
            when(productRepository.getExpiringProductsList(1, 3)).thenReturn(expiringList);
            when(productRepository.getInventoryList(1)).thenReturn(inventoryList);
            when(productRepository.updateProductImage(any())).thenReturn(1);

            assertThat(service.getProductWithShortExpDate(1)).isSameAs(expiring);
            assertThat(service.getAllProductWithExpDateUnder(1, 3)).isSameAs(expiringList);
            assertThat(service.getInventoryList(1)).isSameAs(inventoryList);
            assertThat(service.updateProductImage(new Image(7, "Milk"))).isEqualTo(1);
        }
    }
}
