package com.mobilesystems.feedme.domain.model

/**
 * The Label class contains the expiration date of a product and its type.
 * Tutorial: https://www.baeldung.com/kotlin/enum
 *           https://kotlinlang.org/docs/enum-classes.html
 */
enum class Label(val label: String): IConsumptionLimit {

    WHITE_MEAT("Light meat"){
        override fun calculateExpirationDays() = 3
    },
    RED_MEAT("Red meat"){
        override fun calculateExpirationDays() = 5
    },
    SAUSAGES("Sausage"){
        override fun calculateExpirationDays() = 5
    },
    FISH("Fish"){
        override fun calculateExpirationDays() = 5
    },
    EGGS("Eggs"){
        override fun calculateExpirationDays() = 7
    },
    DAIRY("Diary product"){
        override fun calculateExpirationDays() = 7
    },
    VEGETABLE("Vegetables"){
        override fun calculateExpirationDays() = 10
    },
    FRUIT("Fruit"){
        override fun calculateExpirationDays() = 14
    },
    NUTS("Nuts"){
        override fun calculateExpirationDays() = 30
    },
    MUESLI("Cereals"){
        override fun calculateExpirationDays() = 30
    },
    DRY_PRODUCT("Dry product"){
        override fun calculateExpirationDays() = 30
    },
    FROZEN_MEAL("Frozen product"){
        override fun calculateExpirationDays() = 14
    },
    DRINK("Drinks"){
        override fun calculateExpirationDays() = 30
    },
    CEREAL_PRODUCT("Cereal product"){
        override fun calculateExpirationDays() = 30
    },
    OIL("Oil"){
        override fun calculateExpirationDays() = 30
    },
    BUTTER("Butter"){
        override fun calculateExpirationDays() = 30
    },
    BAKERY("Bakery product"){
        override fun calculateExpirationDays() = 4
    },
    NIBBLES("Munchies"){
        override fun calculateExpirationDays() = 14
    },
    CONFECTIONARY("Sweets"){
        override fun calculateExpirationDays() = 14
    },
    HONEY("Honey"){
        override fun calculateExpirationDays() = 30
    },
    SPICES("Spices"){
        override fun calculateExpirationDays() = 90
    },
    SALT("Salt"){
        override fun calculateExpirationDays() = 365
    },
    SUGGAR("Sugar"){
        override fun calculateExpirationDays() = 365
    };

    // custom properties
    var printableLabel : String? = null

    // custom method
    fun customToString(): String {
        return "[${label}] -> $printableLabel"
    }

    companion object {
        fun from(label: String): Label? = values().find { it.label == label }
    }
}

interface IConsumptionLimit {
    fun calculateExpirationDays(): Int
}