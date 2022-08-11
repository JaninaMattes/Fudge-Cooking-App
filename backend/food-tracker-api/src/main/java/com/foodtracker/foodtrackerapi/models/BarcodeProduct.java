package com.foodtracker.foodtrackerapi.models;

/**
 * <p>
 * The BarcodeProduct is a wrapper class wraps the incoming values from the used
 * Edamam API.
 * </p>
 * For more information and test see <a href="
 * https://developer.edamam.com/food-database-api-docs">documentation</a>
 * 
 * @author Janina Mattes
 */
public class BarcodeProduct {

    private String foodId;
    private String label;
    private String nutrients;
    private String brand;
    private String category;
    private String categoryLabel;
    
    public BarcodeProduct(){}
    /**
     * @param foodId
     * @param label
     * @param nutrients
     * @param brand
     * @param category
     * @param categoryLabel
     */
    public BarcodeProduct(String foodId, String label, String nutrients, String brand, String category,
            String categoryLabel) {
        this.foodId = foodId;
        this.label = label;
        this.nutrients = nutrients;
        this.brand = brand;
        this.category = category;
        this.categoryLabel = categoryLabel;
    }

    /**
     * @return the foodId
     */
    public String getFoodId() {
        return foodId;
    }

    /**
     * @param foodId the foodId to set
     */
    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the nutrients
     */
    public String getNutrients() {
        return nutrients;
    }

    /**
     * @param nutrients the nutrients to set
     */
    public void setNutrients(String nutrients) {
        this.nutrients = nutrients;
    }

    /**
     * @return the brand
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @param brand the brand to set
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the categoryLabel
     */
    public String getCategoryLabel() {
        return categoryLabel;
    }

    /**
     * @param categoryLabel the categoryLabel to set
     */
    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((brand == null) ? 0 : brand.hashCode());
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((categoryLabel == null) ? 0 : categoryLabel.hashCode());
        result = prime * result + ((foodId == null) ? 0 : foodId.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((nutrients == null) ? 0 : nutrients.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BarcodeProduct)) {
            return false;
        }
        BarcodeProduct other = (BarcodeProduct) obj;
        if (brand == null) {
            if (other.brand != null) {
                return false;
            }
        } else if (!brand.equals(other.brand)) {
            return false;
        }
        if (category == null) {
            if (other.category != null) {
                return false;
            }
        } else if (!category.equals(other.category)) {
            return false;
        }
        if (categoryLabel == null) {
            if (other.categoryLabel != null) {
                return false;
            }
        } else if (!categoryLabel.equals(other.categoryLabel)) {
            return false;
        }
        if (foodId == null) {
            if (other.foodId != null) {
                return false;
            }
        } else if (!foodId.equals(other.foodId)) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (nutrients == null) {
            if (other.nutrients != null) {
                return false;
            }
        } else if (!nutrients.equals(other.nutrients)) {
            return false;
        }
        return true;
    }
    
    
}
