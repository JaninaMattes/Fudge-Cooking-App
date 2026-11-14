package com.mobilesystems.feedme.ui.common.utils

import com.google.common.truth.Truth.assertThat
import com.mobilesystems.feedme.domain.model.Product
import org.junit.Test

/**
 * Tests for the functions of StringUtils.kt.
 */
class StringUtilsTest {
    private fun product(name: String) =
        Product(
            productId = 1,
            productName = name,
            expirationDate = "02.02.2022",
            labels = null,
            quantity = "1 Package",
            manufacturer = "Dairy",
            nutritionValue = "250 kcal",
            productImage = null,
        )

    // --- abbreviateString ------------------------------------------------

    @Test
    fun `a short text stays as it is`() {
        assertThat(abbreviateString("Pizza", 20)).isEqualTo("Pizza")
    }

    @Test
    fun `a long text gets two points at the end`() {
        assertThat(abbreviateString("Egg and cucumber salad", 10)).isEqualTo("Egg and ..")
    }

    @Test
    fun `the result is never longer than the highest number of characters`() {
        val result = abbreviateString("Egg and cucumber salad", 10)

        assertThat(result.length).isAtMost(10)
    }

    @Test
    fun `a text of the length of the limit becomes shorter`() {
        assertThat(abbreviateString("abcde", 5)).isEqualTo("abc..")
    }

    @Test
    fun `an empty text stays empty`() {
        assertThat(abbreviateString("", 10)).isEmpty()
    }

    @Test
    fun `a limit that is too small gives the text back`() {
        // Two points and no letter help nobody.
        assertThat(abbreviateString("Pizza", 2)).isEqualTo("Pizza")
        assertThat(abbreviateString("Pizza", 0)).isEqualTo("Pizza")
    }

    // --- abbreviateUnitString --------------------------------------------

    @Test
    fun `a long unit becomes short`() {
        assertThat(abbreviateUnitString("200.0 Tablespoons")).isEqualTo("200.0 Tbsp")
        assertThat(abbreviateUnitString("2 Teaspoons")).isEqualTo("2 Tsp")
        assertThat(abbreviateUnitString("1 Serving")).isEqualTo("1 Srv")
        assertThat(abbreviateUnitString("3 Ounces")).isEqualTo("3 Oz")
        assertThat(abbreviateUnitString("1 Pound")).isEqualTo("1 Lb")
        assertThat(abbreviateUnitString("1 Medium")).isEqualTo("1 M")
        assertThat(abbreviateUnitString("1 Large")).isEqualTo("1 L")
    }

    @Test
    fun `the letter case of the unit does not matter`() {
        assertThat(abbreviateUnitString("2 TEASPOONS")).isEqualTo("2 Tsp")
        assertThat(abbreviateUnitString("2 teaspoons")).isEqualTo("2 Tsp")
    }

    @Test
    fun `an unknown unit stays as it is`() {
        assertThat(abbreviateUnitString("200.0 Grams")).isEqualTo("200.0 Grams")
    }

    @Test
    fun `a value without a unit gives the amount`() {
        assertThat(abbreviateUnitString("200.0")).isEqualTo("200.0")
    }

    @Test
    fun `an empty value gives an empty text`() {
        assertThat(abbreviateUnitString("")).isEmpty()
    }

    // --- splitSentenceRegex ----------------------------------------------

    @Test
    fun `a sentence becomes a list of words`() {
        assertThat(splitSentenceRegex("Fresh Basil")).containsExactly("Fresh", "Basil")
    }

    @Test
    fun `a comma and a point at the end of a word go away`() {
        assertThat(splitSentenceRegex("Basil, Cream."))
            .containsExactly("Basil", "Cream")
    }

    // --- containsSubstring -----------------------------------------------

    @Test
    fun `a product of the list fits an ingredient of two words`() {
        val products = listOf(product("Basil"), product("Milk"))

        assertThat(containsSubstring(products, product("Fresh Basil"))).isTrue()
    }

    @Test
    fun `the letter case does not matter for the search`() {
        val products = listOf(product("basil"))

        assertThat(containsSubstring(products, product("Fresh BASIL"))).isTrue()
    }

    @Test
    fun `a product that is not in the list gives false`() {
        val products = listOf(product("Milk"))

        assertThat(containsSubstring(products, product("Fresh Basil"))).isFalse()
    }

    @Test
    fun `a list that is empty or null gives false`() {
        assertThat(containsSubstring(null, product("Basil"))).isFalse()
        assertThat(containsSubstring(emptyList(), product("Basil"))).isFalse()
    }

    @Test
    fun `an ingredient without a name gives false`() {
        // An empty word must not fit each product of the list.
        assertThat(containsSubstring(listOf(product("Milk")), product(""))).isFalse()
    }

    // --- capitalizeWords -------------------------------------------------

    @Test
    fun `each word gets a capital letter`() {
        assertThat("fresh basil".capitalizeWords()).isEqualTo("Fresh Basil")
    }

    @Test
    fun `a word that already has a capital letter stays as it is`() {
        assertThat("Fresh BASIL".capitalizeWords()).isEqualTo("Fresh BASIL")
    }

    @Test
    fun `an empty text stays empty after the change`() {
        assertThat("".capitalizeWords()).isEmpty()
    }
}
