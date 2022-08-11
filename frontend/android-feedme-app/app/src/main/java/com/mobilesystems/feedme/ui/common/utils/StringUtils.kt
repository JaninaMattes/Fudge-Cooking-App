package com.mobilesystems.feedme.ui.common.utils

import android.util.Log
import com.mobilesystems.feedme.domain.model.Product
import java.util.*

/**
 * Shorten the incoming values as abbreviation for recipe name strings.
 */
fun abbreviateString(text: String, maxLength: Int): String{
    var result = text
    try{
        if(result.length >= maxLength){
            result = text.substring(0, maxLength-2) + ".." // shorten
        }
    }catch (e: Exception){
        e.printStackTrace()
    }
    return result
}

/**
 * Abbreviation for ingredient units.
 */
fun abbreviateUnitString(text: String): String{
    // split into unit and quantity values
    val quantity = text.filter { it.isDigit() || it == '.' }
    var unit = text.filter { it.isLetter() }.trim()

    try{
        // switch case
        when (unit) {
            "Medium" ->
                unit = "M"
            "Large" ->
                unit = "L"
            "Tablespoon" ->
                unit = "Tbsp"
            "Tablespoons" ->
                unit = "Tbsp"
            "Teaspoon" ->
                unit = "Tsp"
            "Teaspoons" ->
                unit = "Tsp"
            "Ounce" ->
                unit = "Oz"
            "Ounces" ->
                unit = "Oz"
            "Serving" ->
                unit = "Srv"
            "Servings" ->
                unit = "Srv"
            "Pound" ->
                unit = "Lb"
            else -> {
                Log.d("String Utils", "No string matches.")
            }
        }
    }catch (e: Exception){
        e.printStackTrace()
    }
    return "$quantity $unit"
}

/**
 * Check if recipe ingredient has a match with products in inventory list.
 * Some ingredients are composite words e.g. Fresh Basil, French Cream Cheese etc.
 */
fun containsSubstring(products: List<Product>?, ingredient: Product): Boolean{
    var isContained = false
    if(products != null){
        // split multiple words in string
        val wordArray = splitSentenceRegex(ingredient.productName)
        for(word in wordArray){
            // whilst it is not contained search
            if(!isContained) {
                val filtered = products.filter { p ->
                    p.productName.contains(
                        word,
                        true
                    )
                }
                if (filtered.isNotEmpty()) {
                    isContained = true
                }
            }
        }
    }
    return isContained
}

/**
 * Function to split words in a sentence into list of words
 */
fun splitSentenceRegex(text: String): List<String>{
    return text.split("\\s+".toRegex()).map { word ->
        word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
    }
}

/**
 * Helper-function to capitalize words.
 */
fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
    it.replaceFirstChar { p ->
        if (p.isLowerCase()) p.titlecase(Locale.getDefault()) else p.toString()
    }
}

/**
 * Helper-function to check if String is empty.
 * Returns `true` if this string is empty or consists solely of whitespace characters.
 */
fun CharSequence.isBlank(): Boolean = length == 0 || indices.all { this[it].isWhitespace() }
