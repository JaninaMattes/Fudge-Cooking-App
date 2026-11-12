package com.foodtracker.foodtrackerapi.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CurrentShoppingList} and {@link OldShoppingList}, and for
 * the common part that both classes inherit.
 *
 * @author Janina Mattes
 */
class ShoppinglistTest {

    private static Product product(int id, String name) {
        return new Product(id, name, null, "1 Package", "Dairy", "250 kcal");
    }

    @Test
    @DisplayName("A new list is empty")
    void newListIsEmpty() {
        CurrentShoppingList list = new CurrentShoppingList(1);

        assertThat(list.getShoppingListId()).isEqualTo(1);
        assertThat(list.getShoppingList()).isEmpty();
    }

    @Test
    @DisplayName("The constructor with products keeps the products")
    void constructorKeepsProducts() {
        List<Product> products = Arrays.asList(product(1, "Milk"), product(2, "Bread"));

        OldShoppingList list = new OldShoppingList(1, products);

        assertThat(list.getShoppingList()).hasSize(2);
    }

    @Test
    @DisplayName("addProduct puts the product on the list")
    void addProductPutsProductOnList() {
        CurrentShoppingList list = new CurrentShoppingList(1, new ArrayList<>());

        list.addProduct(product(1, "Milk"));

        assertThat(list.getShoppingList()).hasSize(1);
    }

    @Test
    @DisplayName("removeProduct takes the product off the list")
    void removeProductTakesProductOffList() {
        Product milk = product(1, "Milk");
        CurrentShoppingList list = new CurrentShoppingList(1, new ArrayList<>());
        list.addProduct(milk);

        assertThat(list.removeProduct(milk)).isTrue();
        assertThat(list.removeProduct(milk)).isFalse();
        assertThat(list.getShoppingList()).isEmpty();
    }

    @Test
    @DisplayName("setShoppingList writes the new products")
    void setShoppingListWritesProducts() {
        OldShoppingList list = new OldShoppingList(1);

        list.setShoppingList(Arrays.asList(product(1, "Milk")));
        list.setShoppingListId(7);

        assertThat(list.getShoppingList()).hasSize(1);
        assertThat(list.getShoppingListId()).isEqualTo(7);
    }

    @Test
    @DisplayName("Two current lists with the same products are equal")
    void equalValuesAreEqual() {
        CurrentShoppingList first = new CurrentShoppingList(1,
                Arrays.asList(product(1, "Milk")));
        CurrentShoppingList second = new CurrentShoppingList(1,
                Arrays.asList(product(1, "Milk")));

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(new CurrentShoppingList(2));
        assertThat(first.toString()).contains("Milk");
    }

    @Test
    @DisplayName("A current list is not equal to an old list")
    void currentListIsNotEqualToOldList() {
        CurrentShoppingList current = new CurrentShoppingList(1, new ArrayList<>());
        OldShoppingList old = new OldShoppingList(1, new ArrayList<>());

        assertThat(current).isNotEqualTo(old);
        assertThat(old).isNotEqualTo(current);
    }
}
