package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.foodtracker.foodtrackerapi.common.utils.DateUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * The products that a user has at home. The list also answers the questions
 * that the dashboard asks, for example which products expire in the next three
 * days.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventorylist {

    /** Primary key in the table ET_INVENTORYLIST. */
    private Integer inventoryId;

    /** Products that are on the list. */
    private ArrayList<Product> inventoryList = new ArrayList<>();

    /**
     * Puts a product on the list.
     *
     * @param product product to put on the list
     */
    public void addProductToInventoryList(Product product) {
        this.inventoryList.add(product);
    }

    /**
     * Takes a product off the list.
     *
     * @param product product to take off the list
     * @return {@code true} if the product was on the list
     */
    public boolean removeProductFromInventoryList(Product product) {
        return this.inventoryList.remove(product);
    }

    /**
     * Sets a new expiration date for the product that has the given name.
     *
     * @param name           name of the product
     * @param expirationDate new expiration date
     * @return {@code true} if a product with this name is on the list
     */
    public boolean updateProductExpirationByName(String name, Date expirationDate) {
        Optional<Product> product = findProductByName(name);
        product.ifPresent(found -> found.setExpirationDate(expirationDate));
        return product.isPresent();
    }

    /**
     * Gives the products that expire in the next number of days.
     *
     * @param days length of the period in days
     * @return the products that expire in this period. The list is empty if
     *         there is no such product.
     */
    public List<Product> getProductsByExpirationPeriod(Integer days) {
        Date start = DateUtils.startOfToday();
        Date end = DateUtils.addDays(start, days != null ? days : 0);
        return filterInventoryListBy(start, end);
    }

    /**
     * Searches for a product by its name.
     *
     * @param productName name of the product
     * @return the product, or an empty result if the list has no product with
     *         this name
     */
    public Optional<Product> findProductByName(String productName) {
        return this.inventoryList.stream()
                .filter(product -> product.getProductName() != null)
                .filter(product -> product.getProductName().equals(productName))
                .findFirst();
    }

    /**
     * Searches for a product by its name.
     *
     * @param productName name of the product
     * @return the product, or {@code null} if the list has no product with this
     *         name
     * @deprecated Use {@link #findProductByName(String)}. It cannot give
     *             {@code null}.
     */
    @Deprecated
    public Product filterProductInInventoryList(String productName) {
        return findProductByName(productName).orElse(null);
    }

    private List<Product> filterInventoryListBy(Date start, Date end) {
        return this.inventoryList.stream()
                .filter(product -> product.getExpirationDate() != null)
                .filter(product -> product.getExpirationDate().before(end))
                .filter(product -> product.getExpirationDate().after(start))
                .collect(Collectors.toList());
    }
}
