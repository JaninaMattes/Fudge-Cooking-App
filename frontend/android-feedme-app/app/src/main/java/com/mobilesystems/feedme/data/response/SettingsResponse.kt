package com.mobilesystems.feedme.data.response

data class SettingsResponse(
    val settingsId: Int,
    val remindBeforeProductExpiration: Boolean? = null,
    val allowPushNotifications: Boolean? = null,
    val suggestProductsForShoppingList: Boolean? = null,
)