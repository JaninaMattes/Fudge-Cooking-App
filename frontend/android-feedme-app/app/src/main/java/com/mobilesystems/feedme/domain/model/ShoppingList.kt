package com.mobilesystems.feedme.domain.model

import android.os.Parcelable

abstract class ShoppingList(
    @Transient
    open val shoppingListId: Int,
    @Transient
    open val shoppingList: List<Product>? = null
): Parcelable
