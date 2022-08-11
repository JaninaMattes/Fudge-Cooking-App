package com.mobilesystems.feedme.domain.model

enum class FoodType(val label: String) {

    VEGAN("Vegan"),
    VEGETARIAN("Vegetarian"),
    LOW_CARB("Low Carb"),
    FAST_FOOD("Fast Food"),
    MEDITERRANEAN("Mediterranean"),
    AMERICAN("Ame"),
    ASIAN("Asian"),
    FITNESS("Fitness"),
    PALEO("Paleo");

    // custom properties
    var printableLabel : String? = null

    // custom method
    fun customToString(): String {
        return "[${label}] -> $printableLabel"
    }

    // Workaround to select a Foodtype
    companion object {
        fun from(label: String): FoodType? = values().find { it.label == label }
    }
}