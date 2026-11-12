package com.foodtracker.foodtrackerapi.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.foodtracker.foodtrackerapi.common.utils.DateUtils;

/**
 * Tests for {@link Inventorylist}.
 *
 * @author Janina Mattes
 */
class InventorylistTest {

    private Inventorylist inventorylist;

    private static Product productThatExpiresIn(String name, int days) {
        Date expirationDate = DateUtils.addDays(DateUtils.startOfToday(), days);
        return new Product(1, name, expirationDate, "1 Package", "Dairy", "250 kcal");
    }

    @BeforeEach
    void setUp() {
        inventorylist = new Inventorylist();
        inventorylist.setInventoryId(1);
    }

    @Test
    @DisplayName("addProductToInventoryList puts the product on the list")
    void addProductPutsProductOnList() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));

        assertThat(inventorylist.getInventoryList()).hasSize(1);
    }

    @Test
    @DisplayName("removeProductFromInventoryList takes the product off the list")
    void removeProductTakesProductOffList() {
        Product product = productThatExpiresIn("Milk", 2);
        inventorylist.addProductToInventoryList(product);

        assertThat(inventorylist.removeProductFromInventoryList(product)).isTrue();
        assertThat(inventorylist.getInventoryList()).isEmpty();
    }

    @Test
    @DisplayName("removeProductFromInventoryList gives false for a product that is not on the list")
    void removeProductGivesFalseForUnknownProduct() {
        assertThat(inventorylist.removeProductFromInventoryList(productThatExpiresIn("Milk", 2)))
                .isFalse();
    }

    @Test
    @DisplayName("findProductByName finds the product that has the name")
    void findProductByNameFindsProduct() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));

        assertThat(inventorylist.findProductByName("Milk")).isPresent();
        assertThat(inventorylist.findProductByName("Bread")).isEmpty();
    }

    @Test
    @DisplayName("findProductByName accepts a product without a name")
    void findProductByNameAcceptsProductWithoutName() {
        inventorylist.addProductToInventoryList(new Product(1, null, null, null, null, null));

        assertThat(inventorylist.findProductByName("Milk")).isEmpty();
    }

    @Test
    @DisplayName("filterProductInInventoryList gives null for a product that is not on the list")
    void deprecatedFilterGivesNullForUnknownProduct() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));

        assertThat(inventorylist.filterProductInInventoryList("Milk")).isNotNull();
        assertThat(inventorylist.filterProductInInventoryList("Bread")).isNull();
    }

    @Test
    @DisplayName("updateProductExpirationByName writes the new date")
    void updateExpirationWritesNewDate() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));
        Date newDate = java.sql.Date.valueOf("2030-01-01");

        boolean changed = inventorylist.updateProductExpirationByName("Milk", newDate);

        assertThat(changed).isTrue();
        assertThat(inventorylist.getInventoryList()).hasSize(1);
        assertThat(inventorylist.getInventoryList().get(0).getExpirationDate())
                .isEqualTo(newDate);
    }

    @Test
    @DisplayName("updateProductExpirationByName gives false for a product that is not on the list")
    void updateExpirationGivesFalseForUnknownProduct() {
        assertThat(inventorylist.updateProductExpirationByName("Bread", new Date())).isFalse();
    }

    @Test
    @DisplayName("getProductsByExpirationPeriod gives the products of the period only")
    void expirationPeriodGivesProductsOfPeriod() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));
        inventorylist.addProductToInventoryList(productThatExpiresIn("Yoghurt", 10));
        inventorylist.addProductToInventoryList(productThatExpiresIn("Old bread", -2));

        List<Product> expiring = inventorylist.getProductsByExpirationPeriod(3);

        assertThat(expiring).extracting(Product::getProductName).containsExactly("Milk");
    }

    @Test
    @DisplayName("getProductsByExpirationPeriod accepts a product without a date")
    void expirationPeriodAcceptsProductWithoutDate() {
        inventorylist.addProductToInventoryList(new Product(1, "Milk", null, null, null, null));

        assertThat(inventorylist.getProductsByExpirationPeriod(3)).isEmpty();
    }

    @Test
    @DisplayName("getProductsByExpirationPeriod accepts a period that is null")
    void expirationPeriodAcceptsNull() {
        inventorylist.addProductToInventoryList(productThatExpiresIn("Milk", 2));

        assertThat(inventorylist.getProductsByExpirationPeriod(null)).isEmpty();
    }

    @Test
    @DisplayName("Two lists with the same products are equal")
    void equalValuesAreEqual() {
        Product product = productThatExpiresIn("Milk", 2);
        Inventorylist first = new Inventorylist(1, new ArrayList<>());
        Inventorylist second = new Inventorylist(1, new ArrayList<>());
        first.addProductToInventoryList(product);
        second.addProductToInventoryList(product);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(new Inventorylist(2, new ArrayList<>()));
        assertThat(first.toString()).contains("Milk");
    }
}
