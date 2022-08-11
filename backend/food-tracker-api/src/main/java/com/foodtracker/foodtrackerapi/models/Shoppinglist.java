package com.foodtracker.foodtrackerapi.models;

import java.util.ArrayList;
import java.util.List;

abstract class Shoppinglist {

    private Integer shoppingListId;
    private List<Product> shoppingList = new ArrayList<Product>();
    
    /**
     * @param shoppingListId
     */
    public Shoppinglist(Integer shoppingListId) {
        this.shoppingListId = shoppingListId;
    }

    
    /**
     * @param shoppingListId
     * @param shoppingList
     */
    public Shoppinglist(Integer shoppingListId, List<Product> shoppingList) {
        this.shoppingListId = shoppingListId;
        this.shoppingList = shoppingList;
    }


    /**
     * @return the shoppingListId
     */
    public Integer getShoppingListId() {
        return shoppingListId;
    }


    /**
     * @param shoppingListId the shoppingListId to set
     */
    public void setShoppingListId(Integer shoppingListId) {
        this.shoppingListId = shoppingListId;
    }


    /**
     * @return the shoppingList
     */
    public List<Product> getShoppingList() {
        return shoppingList;
    }


    /**
     * @param shoppingList the shoppingList to set
     */
    public void setShoppingList(List<Product> shoppingList) {
        this.shoppingList = shoppingList;
    }


    private void addProduct(Product product){
        this.shoppingList.add(product);
    }

    private void removeProduct(Product product){
        this.shoppingList.remove(product);
    }



    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((shoppingList == null) ? 0 : shoppingList.hashCode());
        result = prime * result + ((shoppingListId == null) ? 0 : shoppingListId.hashCode());
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
        if (!(obj instanceof Shoppinglist)) {
            return false;
        }
        Shoppinglist other = (Shoppinglist) obj;
        if (shoppingList == null) {
            if (other.shoppingList != null) {
                return false;
            }
        } else if (!shoppingList.equals(other.shoppingList)) {
            return false;
        }
        if (shoppingListId == null) {
            if (other.shoppingListId != null) {
                return false;
            }
        } else if (!shoppingListId.equals(other.shoppingListId)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "Shoppinglist [shoppingList=" + shoppingList + ", shoppingListId=" + shoppingListId + "]";
    }
    
    
}
