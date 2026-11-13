package com.mobilesystems.feedme.ui.common.utils

import com.mobilesystems.feedme.domain.model.Product
import java.util.Locale

/**
 * Makes the text of the external APIs ready for a small screen.
 *
 * The functions use no class of the Android framework. A unit test on the
 * development machine can therefore use them, and the tests need no emulator.
 */

/** Text that stands at the end of a name that the application shortened. */
private const val ELLIPSIS = ".."

/**
 * Short form of each unit that the external recipe API sends. The first
 * version of the application had a `when` block with eleven branches for the
 * same work. A map holds the same information in one place, and a new unit
 * needs one line.
 */
private val UNIT_ABBREVIATIONS: Map<String, String> = mapOf(
    "medium" to "M",
    "large" to "L",
    "small" to "S",
    "tablespoon" to "Tbsp",
    "tablespoons" to "Tbsp",
    "teaspoon" to "Tsp",
    "teaspoons" to "Tsp",
    "ounce" to "Oz",
    "ounces" to "Oz",
    "serving" to "Srv",
    "servings" to "Srv",
    "pound" to "Lb",
    "pounds" to "Lb"
)

/**
 * Shortens a text that is too long for its place on the screen.
 *
 * @param text text to shorten
 * @param maxLength highest number of characters of the result
 * @return the text, or the first characters of the text and two points
 */
fun abbreviateString(text: String, maxLength: Int): String {
    if (maxLength <= ELLIPSIS.length || text.length < maxLength) {
        return text
    }
    return text.take(maxLength - ELLIPSIS.length) + ELLIPSIS
}

/**
 * Shortens the unit of an ingredient, for example "200 Tablespoons" to
 * "200 Tbsp". A long unit does not fit in the list of the ingredients.
 *
 * @param text amount and unit, for example "200.0 Grams"
 * @return the amount and the short unit. The function gives the unit back
 *         without a change when the map holds no short form for it.
 */
fun abbreviateUnitString(text: String): String {
    val quantity = text.filter { it.isDigit() || it == '.' }
    val unit = text.filter { it.isLetter() }.trim()
    val abbreviation = UNIT_ABBREVIATIONS[unit.lowercase(Locale.ROOT)] ?: unit
    return "$quantity $abbreviation".trim()
}

/**
 * Tells if one of the products fits an ingredient of a recipe.
 *
 * The name of an ingredient often holds more than one word, for example
 * "Fresh Basil" or "French Cream Cheese". The function therefore tests each
 * word on its own, so that the product "Basil" fits the ingredient
 * "Fresh Basil".
 *
 * @param products products of the inventory list. The list can be `null`.
 * @param ingredient ingredient of the recipe
 * @return `true` when one product fits the ingredient
 */
fun containsSubstring(products: List<Product>?, ingredient: Product): Boolean {
    if (products.isNullOrEmpty()) {
        return false
    }
    return splitSentenceRegex(ingredient.productName).any { word ->
        word.isNotBlank() && products.any { product ->
            product.productName.contains(word, ignoreCase = true)
        }
    }
}

/**
 * Splits a text into its words and removes a comma or a point at the end of a
 * word.
 *
 * @param text text to split
 * @return the words of the text
 */
fun splitSentenceRegex(text: String): List<String> =
    text.split("\\s+".toRegex()).map { word ->
        word.replace("""^[,.]|[,.]$""".toRegex(), "")
    }

/**
 * Writes the first letter of each word with a capital letter.
 *
 * @return the changed text
 */
fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { letter ->
        if (letter.isLowerCase()) letter.titlecase(Locale.getDefault()) else letter.toString()
    }
}
