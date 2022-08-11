package com.foodtracker.foodtrackerapi.models;

public class Settings {

    private Integer settingsId;
    private Boolean allowPushNotifications = false; // per default on false
    private Boolean remindBeforeProductExpiration = false;
    private Boolean suggestProductsForShoppingList = false;

    /**
     * @param settingsId
     */
    public Settings(Integer settingsId) {
        this.settingsId = settingsId;
    }

    public Settings(Integer settingsId, Boolean allowPushNotications, Boolean remindBeforeProductExpiration, Boolean suggestProductsForShopping){
        this.settingsId = settingsId;
        this.allowPushNotifications = allowPushNotications;
        this.remindBeforeProductExpiration = remindBeforeProductExpiration;
        this.suggestProductsForShoppingList = suggestProductsForShopping;
    }


    /**
     * @return the settingsId
     */
    public Integer getSettingsId() {
        return settingsId;
    }


    /**
     * @param settingsId the settingsId to set
     */
    public void setSettingsId(Integer settingsId) {
        this.settingsId = settingsId;
    }


    /**
     * @return the allowPushNotifications
     */
    public Boolean getAllowPushNotifications() {
        return allowPushNotifications;
    }


    /**
     * @param allowPushNotifications the allowPushNotifications to set
     */
    public void setAllowPushNotifications(Boolean allowPushNotifications) {
        this.allowPushNotifications = allowPushNotifications;
    }


    /**
     * @return the remindBeforeProductExpiration
     */
    public Boolean getRemindBeforeProductExpiration() {
        return remindBeforeProductExpiration;
    }


    /**
     * @param remindBeforeProductExpiration the remindBeforeProductExpiration to set
     */
    public void setRemindBeforeProductExpiration(Boolean remindBeforeProductExpiration) {
        this.remindBeforeProductExpiration = remindBeforeProductExpiration;
    }


    /**
     * @return the suggestProductsForShoppingList
     */
    public Boolean getSuggestProductsForShoppingList() {
        return suggestProductsForShoppingList;
    }


    /**
     * @param suggestProductsForShoppingList the suggestProductsForShoppingList to set
     */
    public void setSuggestProductsForShoppingList(Boolean suggestProductsForShoppingList) {
        this.suggestProductsForShoppingList = suggestProductsForShoppingList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString() {
        return "Settings [allowPushNotifications=" + allowPushNotifications + ", remindBeforeProductExpiration="
                + remindBeforeProductExpiration + ", settingsId=" + settingsId + ", suggestProductsForShoppingList="
                + suggestProductsForShoppingList + "]";
    }

    
}
