package com.mobilesystems.feedme.ui.common.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [ExpiryStatus].
 *
 * The class holds the rule that decides which colour a product gets. The rule
 * must be correct, because the user reads the state of the food from the
 * colour only.
 */
class ExpiryStatusTest {
    @Test
    fun `a product of the past is expired`() {
        assertThat(ExpiryStatus.fromDaysLeft(-1)).isEqualTo(ExpiryStatus.EXPIRED)
        assertThat(ExpiryStatus.fromDaysLeft(-30)).isEqualTo(ExpiryStatus.EXPIRED)
    }

    @Test
    fun `a product of today has its own state`() {
        assertThat(ExpiryStatus.fromDaysLeft(0)).isEqualTo(ExpiryStatus.TODAY)
    }

    @Test
    fun `a product of the next three days is urgent`() {
        assertThat(ExpiryStatus.fromDaysLeft(1)).isEqualTo(ExpiryStatus.URGENT)
        assertThat(ExpiryStatus.fromDaysLeft(2)).isEqualTo(ExpiryStatus.URGENT)
        assertThat(ExpiryStatus.fromDaysLeft(ExpiryStatus.URGENT_DAYS))
            .isEqualTo(ExpiryStatus.URGENT)
    }

    @Test
    fun `a product of this week expires soon`() {
        assertThat(ExpiryStatus.fromDaysLeft(ExpiryStatus.URGENT_DAYS + 1))
            .isEqualTo(ExpiryStatus.SOON)
        assertThat(ExpiryStatus.fromDaysLeft(ExpiryStatus.SOON_DAYS))
            .isEqualTo(ExpiryStatus.SOON)
    }

    @Test
    fun `a product of the next weeks is fresh`() {
        assertThat(ExpiryStatus.fromDaysLeft(ExpiryStatus.SOON_DAYS + 1))
            .isEqualTo(ExpiryStatus.FRESH)
        assertThat(ExpiryStatus.fromDaysLeft(365)).isEqualTo(ExpiryStatus.FRESH)
    }

    @Test
    fun `only a product of the next three days needs attention`() {
        assertThat(ExpiryStatus.EXPIRED.needsAttention()).isTrue()
        assertThat(ExpiryStatus.TODAY.needsAttention()).isTrue()
        assertThat(ExpiryStatus.URGENT.needsAttention()).isTrue()
        assertThat(ExpiryStatus.SOON.needsAttention()).isFalse()
        assertThat(ExpiryStatus.FRESH.needsAttention()).isFalse()
    }

    @Test
    fun `each state has a colour and a text`() {
        ExpiryStatus.values().forEach { status ->
            assertThat(status.colorRes).isNotEqualTo(0)
            assertThat(status.labelRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `the states follow the number of days without a gap`() {
        // Each number of days must give exactly one state.
        val days = (-5L..15L).toList()
        val states = days.map { ExpiryStatus.fromDaysLeft(it) }

        assertThat(states).hasSize(days.size)
        assertThat(states).containsNoneIn(listOf<ExpiryStatus?>(null))
    }
}
