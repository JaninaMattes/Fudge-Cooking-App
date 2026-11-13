package com.foodtracker.foodtrackerapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * The notification settings of one user. All settings are off when the
 * application makes the profile. The user must give permission first.
 * </p>
 *
 * @author Janina Mattes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    /** Primary key in the table ET_PROFILE_SETTINGS. */
    private Integer settingsId;

    /** Permission to send a push notification to the device. */
    private Boolean allowPushNotifications = false;

    /** Permission to remind the user before a product expires. */
    private Boolean remindBeforeProductExpiration = false;

    /** Permission to suggest products for the shopping list. */
    private Boolean suggestProductsForShoppingList = false;

    /**
     * Makes the settings of a new user. All permissions are off.
     *
     * @param settingsId primary key of the settings
     */
    public Settings(Integer settingsId) {
        this.settingsId = settingsId;
    }
}
