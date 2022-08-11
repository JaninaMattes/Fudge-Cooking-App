package com.foodtracker.foodtrackerapi.models;

public class Ingredient {

    private Integer ingredientId;
    private String ingredientName;
    private String quantity;
    
    public Ingredient(){}
    
    /**
     * @param ingredientId
     * @param ingredientName
     * @param quantity
     */
    public Ingredient(Integer ingredientId, String ingredientName, String quantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
    }
    
    /**
     * @return the ingredientId
     */
    public Integer getIngredientId() {
        return ingredientId;
    }
    /**
     * @param ingredientId the ingredientId to set
     */
    public void setIngredientId(Integer ingredientId) {
        this.ingredientId = ingredientId;
    }
    /**
     * @return the ingredientName
     */
    public String getIngredientName() {
        return ingredientName;
    }
    /**
     * @param ingredientName the ingredientName to set
     */
    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
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

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ingredientId == null) ? 0 : ingredientId.hashCode());
        result = prime * result + ((ingredientName == null) ? 0 : ingredientName.hashCode());
        result = prime * result + ((quantity == null) ? 0 : quantity.hashCode());
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
        if (!(obj instanceof Ingredient)) {
            return false;
        }
        Ingredient other = (Ingredient) obj;
        if (ingredientId == null) {
            if (other.ingredientId != null) {
                return false;
            }
        } else if (!ingredientId.equals(other.ingredientId)) {
            return false;
        }
        if (ingredientName == null) {
            if (other.ingredientName != null) {
                return false;
            }
        } else if (!ingredientName.equals(other.ingredientName)) {
            return false;
        }
        if (quantity == null) {
            if (other.quantity != null) {
                return false;
            }
        } else if (!quantity.equals(other.quantity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Ingredient [ingredientId=" + ingredientId + ", ingredientName=" + ingredientName + ", quantity="
                + quantity + "]";
    }

}
