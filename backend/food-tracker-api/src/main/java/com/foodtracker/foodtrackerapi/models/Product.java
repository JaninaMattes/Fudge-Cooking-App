package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;
import java.util.Date;

public class Product {

    private Integer productId;
    private String productName;
    private Date expirationDate;
    private String quantity;
    private String manufacturer;
    private String nutritionValue;
    private Image productImage = null;
    private ArrayList<Tag> productTags = new ArrayList<>();
    
    /**
     * @param productId
     * @param productName
     * @param expirationDate
     * @param quantity
     * @param manufacturer
     * @param nutritionValue
     */
    public Product(Integer productId, String productName, Date expirationDate, String quantity, String manufacturer,
            String nutritionValue) {
        this.productId = productId;
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.manufacturer = manufacturer;
        this.nutritionValue = nutritionValue;
    }


    /**
     * @param productId
     * @param productName
     * @param expirationDate
     * @param quantity
     * @param manufacturer
     * @param nutritionValue
     * @param productTags
     */
    public Product(Integer productId, String productName, Date expirationDate, String quantity, String manufacturer,
            String nutritionValue, ArrayList<Tag> productTags) {
        this.productId = productId;
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.manufacturer = manufacturer;
        this.nutritionValue = nutritionValue;
        this.productTags = productTags;
    }


    /**
     * @param productId
     * @param productName
     * @param expirationDate
     * @param quantity
     * @param manufacturer
     * @param nutritionValue
     * @param productImage
     * @param productTags
     */
    public Product(Integer productId, String productName, Date expirationDate, String quantity, String manufacturer,
            String nutritionValue, Image productImage, ArrayList<Tag> productTags) {
        this.productId = productId;
        this.productName = productName;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.manufacturer = manufacturer;
        this.nutritionValue = nutritionValue;
        this.productImage = productImage;
        this.productTags = productTags;
    }

    /**
     * @return the productId
     */
    public Integer getProductId() {
        return productId;
    }


    /**
     * @param productId the productId to set
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }


    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }


    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }


    /**
     * @return the expirationDate
     */
    public Date getExpirationDate() {
        return expirationDate;
    }


    /**
     * @param expirationDate the expirationDate to set
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }


    /**
     * @return the quantity
     */
    public String getQuantity() {
        return quantity;
    }


    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }


    /**
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }


    /**
     * @param manufacturer the manufacturer to set
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }


    /**
     * @return the nutritionValue
     */
    public String getNutritionValue() {
        return nutritionValue;
    }


    /**
     * @param nutritionValue the nutritionValue to set
     */
    public void setNutritionValue(String nutritionValue) {
        this.nutritionValue = nutritionValue;
    }


    /**
     * @return the productImage
     */
    public Image getProductImage() {
        return productImage;
    }


    /**
     * @param productImage the productImage to set
     */
    public void setProductImage(Image productImage) {
        this.productImage = productImage;
    }


    /**
     * @return the productTags
     */
    public ArrayList<Tag> getProductTags() {
        return productTags;
    }


    /**
     * @param productTags the productTags to set
     */
    public void setProductTags(ArrayList<Tag> productTags) {
        this.productTags = productTags;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "Product [expirationDate=" + expirationDate + ", manufacturer=" + manufacturer + ", nutritionValue="
                + nutritionValue + ", productId=" + productId + ", productImage=" + productImage + ", productName="
                + productName + ", productTags=" + productTags + ", quantity=" + quantity + "]";
    }
   
}
