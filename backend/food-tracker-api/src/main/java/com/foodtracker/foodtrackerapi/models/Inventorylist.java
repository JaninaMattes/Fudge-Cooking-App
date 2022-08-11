package com.foodtracker.foodtrackerapi.models;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.foodtracker.foodtrackerapi.common.utils.DateUtils;

public class Inventorylist {

    private Integer inventoryId;
    private ArrayList<Product> inventoryList = new ArrayList<Product>();

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }

    public ArrayList<Product> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(ArrayList<Product> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public void addProductToInventoryList(Product product) {
        this.inventoryList.add(product);
    }

    public void removeProductFromInventoryList(Product product) {
        this.inventoryList.remove(product);
    }

    public void updateProductExpirationByName(String name, Date expirationDate) {
        Product product = this.filterProductInInventoryList(name);
        product.setExpirationDate(expirationDate);
        inventoryList.add(product);
    }

    public List<Product> getProductsByExpirationPeriod(Integer days) {
        Date current = this.getCurrentDate();
        Date endDate = DateUtils.addDays(current, days);
        return this.filterInventoryListBy(current, endDate);
    }

    public Product filterProductInInventoryList(String productName){
        return this.inventoryList.stream()
                .filter(p -> p.getProductName().equals(productName))
                .findAny()
                .orElse(null);
    }

    private List<Product> filterInventoryListBy(Date beginning, Date end){
        return this.inventoryList.stream()
                .filter(p -> p.getExpirationDate().before(end) && p.getExpirationDate().after(beginning))
                .collect(Collectors.toList());
    }

    private Date getCurrentDate(){
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate localDate = LocalDate.now();
        return Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventorylist)) return false;
        if (!super.equals(o)) return false;
        Inventorylist that = (Inventorylist) o;
        return Objects.equals(inventoryId, that.inventoryId) && Objects.equals(inventoryList, that.inventoryList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inventoryId, inventoryList);
    }

    @Override
    public String toString() {
        return "Inventarylist{" +
                "inventoryId=" + inventoryId +
                ", inventaryList=" + inventoryList +
                '}';
    }
}
