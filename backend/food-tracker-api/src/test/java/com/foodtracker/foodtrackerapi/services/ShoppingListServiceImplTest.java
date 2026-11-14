package com.foodtracker.foodtrackerapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.foodtracker.foodtrackerapi.models.CurrentShoppingList;
import com.foodtracker.foodtrackerapi.models.OldShoppingList;
import com.foodtracker.foodtrackerapi.models.Product;
import com.foodtracker.foodtrackerapi.repositories.ShoppingListRepository;

/**
 * Tests for {@link ShoppingListServiceImpl}.
 *
 * @author Janina Mattes
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShoppingListServiceImplTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    private ShoppingListServiceImpl service;

    private static Product product(Integer id, String name) {
        return new Product(id, name, Date.valueOf("2022-02-02"), "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        service = new ShoppingListServiceImpl(shoppingListRepository);
    }

    @Nested
    @DisplayName("createProductForCurrentShoppingList")
    class CreateProduct {

        @Test
        @DisplayName("stores the product with an empty tag and a default image")
        void storesProductWithTagAndImage() {
            Product product = product(null, "Milk");
            when(shoppingListRepository.createProductInCurrentShoppingList(1, product))
                    .thenReturn(42);
            when(shoppingListRepository.createDietryTag("")).thenReturn(2);

            assertThat(service.createProductForCurrentShoppingList(1, product)).isEqualTo(42);

            verify(shoppingListRepository).createProductTag(42, 2);
            verify(shoppingListRepository).createProductImage(42, "Product image", "");
        }

        @Test
        @DisplayName("stores no tag and no image when the product was not created")
        void storesNothingWhenProductWasNotCreated() {
            Product product = product(null, "Milk");
            when(shoppingListRepository.createProductInCurrentShoppingList(1, product))
                    .thenReturn(0);

            assertThat(service.createProductForCurrentShoppingList(1, product)).isZero();

            verify(shoppingListRepository, never()).createProductTag(anyInt(), anyInt());
            verify(shoppingListRepository, never()).createProductImage(anyInt(), any(), any());
        }
    }

    @Nested
    @DisplayName("The read operations")
    class ReadOperations {

        @Test
        @DisplayName("The current list holds the products of the database")
        void currentListHoldsProducts() {
            List<Product> products = Collections.singletonList(product(42, "Milk"));
            when(shoppingListRepository.getAllProductsInCurrentShoppingList(1))
                    .thenReturn(Optional.of(products));

            CurrentShoppingList list = service.getCurrentShoppingListByID(1)
                    .orElseThrow(AssertionError::new);

            assertThat(list.getShoppingListId()).isEqualTo(1);
            assertThat(list.getShoppingList()).isSameAs(products);
        }

        @Test
        @DisplayName("The old list holds the products of the database")
        void oldListHoldsProducts() {
            List<Product> products = Collections.singletonList(product(42, "Milk"));
            when(shoppingListRepository.getAllProductsInOldShoppingList(1))
                    .thenReturn(Optional.of(products));

            OldShoppingList list = service.getOldShoppingListByID(1)
                    .orElseThrow(AssertionError::new);

            assertThat(list.getShoppingList()).isSameAs(products);
        }

        @Test
        @DisplayName("A list is empty when the database gives no answer")
        void listIsEmptyWithoutAnswer() {
            when(shoppingListRepository.getAllProductsInCurrentShoppingList(1))
                    .thenReturn(Optional.empty());
            when(shoppingListRepository.getAllProductsInOldShoppingList(1))
                    .thenReturn(Optional.empty());

            assertThat(service.getCurrentShoppingListByID(1).get().getShoppingList()).isEmpty();
            assertThat(service.getOldShoppingListByID(1).get().getShoppingList()).isEmpty();
        }
    }

    @Nested
    @DisplayName("The update of all products")
    class UpdateAllProducts {

        @Test
        @DisplayName("The current list uses the statement of the current list")
        void currentListUsesItsOwnStatement() {
            // The endpoint of the current list changed the old list before.
            ArrayList<Product> products = new ArrayList<>(Arrays.asList(
                    product(42, "Milk"), product(43, "Bread")));
            when(shoppingListRepository.updateProductInCurrentShoppingList(eq(1), any()))
                    .thenReturn(1);

            assertThat(service.updateAllItemsCurrentShoppingList(1, products)).isEqualTo(2);

            verify(shoppingListRepository, never()).updateProductInOldShoppingList(anyInt(),
                    any());
        }

        @Test
        @DisplayName("The old list uses the statement of the old list")
        void oldListUsesItsOwnStatement() {
            ArrayList<Product> products = new ArrayList<>(
                    Collections.singletonList(product(42, "Milk")));
            when(shoppingListRepository.updateProductInOldShoppingList(eq(1), any()))
                    .thenReturn(1);

            assertThat(service.updateAllItemsOldShoppingList(1, products)).isEqualTo(1);

            verify(shoppingListRepository, never()).updateProductInCurrentShoppingList(anyInt(),
                    any());
        }

        @Test
        @DisplayName("Each list gives 0 for a list that is empty or null")
        void givesZeroForEmptyList() {
            assertThat(service.updateAllItemsCurrentShoppingList(1, new ArrayList<>())).isZero();
            assertThat(service.updateAllItemsOldShoppingList(1, new ArrayList<>())).isZero();
            assertThat(service.updateAllItemsCurrentShoppingList(1, null)).isZero();
            assertThat(service.updateAllItemsOldShoppingList(1, null)).isZero();
        }
    }

    @Nested
    @DisplayName("The remove operations")
    class RemoveOperations {

        @Test
        @DisplayName("The remove from the current list reads the key of the current list")
        void removeFromCurrentListReadsItsKey() {
            when(shoppingListRepository.getCurrentShoppingListId(1)).thenReturn(11);
            when(shoppingListRepository.removeProductFromCurShoppingList(42, 11)).thenReturn(1);

            assertThat(service.removeProductInCurrentShoppingList(1, 42)).isEqualTo(1);
        }

        @Test
        @DisplayName("The remove from the old list reads the key of the old list")
        void removeFromOldListReadsItsKey() {
            when(shoppingListRepository.getOldShoppingListId(1)).thenReturn(13);
            when(shoppingListRepository.removeProductFromOldShoppingList(42, 13)).thenReturn(1);

            assertThat(service.removeProductInOldShoppingList(1, 42)).isEqualTo(1);
        }

        @Test
        @DisplayName("A remove gives 0 when the user has no such list")
        void removeGivesZeroWithoutList() {
            when(shoppingListRepository.getCurrentShoppingListId(1)).thenReturn(0);
            when(shoppingListRepository.getOldShoppingListId(1)).thenReturn(0);

            assertThat(service.removeProductInCurrentShoppingList(1, 42)).isZero();
            assertThat(service.removeProductInOldShoppingList(1, 42)).isZero();

            verify(shoppingListRepository, never()).removeProductFromCurShoppingList(anyInt(),
                    anyInt());
            verify(shoppingListRepository, never()).removeProductFromOldShoppingList(anyInt(),
                    anyInt());
        }
    }

    @Nested
    @DisplayName("The operations that the service gives to the repository")
    class DelegatedOperations {

        @Test
        @DisplayName("Each operation gives the answer of the repository")
        void operationsGiveAnswerOfRepository() {
            Product product = product(42, "Milk");
            when(shoppingListRepository.addProductInOldShoppingList(1, 42)).thenReturn(1);
            when(shoppingListRepository.addProductInCurShoppingList(1, 42)).thenReturn(1);
            when(shoppingListRepository.updateProductInCurrentShoppingList(1, product))
                    .thenReturn(1);
            when(shoppingListRepository.updateProductInOldShoppingList(1, product)).thenReturn(1);
            when(shoppingListRepository.deleteProductInCurrentShoppingList(1, 42)).thenReturn(1);
            when(shoppingListRepository.deleteProductInOldShoppingList(1, 42)).thenReturn(1);

            assertThat(service.addProductInOldShoppingList(1, 42)).isEqualTo(1);
            assertThat(service.addProductInCurrentShoppingList(1, 42)).isEqualTo(1);
            assertThat(service.updateProductInCurrentShoppingList(1, product)).isEqualTo(1);
            assertThat(service.updateProductInOldShoppingList(1, product)).isEqualTo(1);
            assertThat(service.deleteProductInCurrentShoppingList(1, 42)).isEqualTo(1);
            assertThat(service.deleteProductInOldShoppingList(1, 42)).isEqualTo(1);
        }
    }
}
