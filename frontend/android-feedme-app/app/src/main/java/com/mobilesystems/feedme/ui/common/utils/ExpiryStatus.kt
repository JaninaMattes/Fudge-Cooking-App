package com.mobilesystems.feedme.ui.common.utils

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.mobilesystems.feedme.R

/**
 * The state of a product, from the number of days until it expires.
 *
 * The user interface shows the state as a colour and as a short text. The user
 * then sees in one moment which products need attention, and does not have to
 * read and compare the dates.
 *
 * The first version of the application had two states only: red for three days
 * or fewer, black for all other products. A product that was already bad
 * therefore looked the same as a product that expires tomorrow, and the text
 * showed a negative number of days.
 *
 * @property colorRes colour that the user interface shows for the state
 * @property labelRes short text that the user interface shows for the state
 */
enum class ExpiryStatus(
    @ColorRes val colorRes: Int,
    @StringRes val labelRes: Int,
) {
    /** The product is bad. The user must throw it away. */
    EXPIRED(R.color.status_expired, R.string.expiry_status_expired),

    /** The product expires today. */
    TODAY(R.color.status_urgent, R.string.expiry_status_today),

    /** The product expires in three days or fewer. Cook it now. */
    URGENT(R.color.status_urgent, R.string.expiry_status_urgent),

    /** The product expires in one week or less. Plan a meal with it. */
    SOON(R.color.status_soon, R.string.expiry_status_soon),

    /** The product stays good for more than one week. */
    FRESH(R.color.status_fresh, R.string.expiry_status_fresh),
    ;

    /**
     * Tells if the state needs the attention of the user.
     *
     * @return `true` for a product that is bad or expires in three days or
     *         fewer
     */
    fun needsAttention(): Boolean = this == EXPIRED || this == TODAY || this == URGENT

    companion object {
        /** Highest number of days for the state [URGENT]. */
        const val URGENT_DAYS = 3L

        /** Highest number of days for the state [SOON]. */
        const val SOON_DAYS = 7L

        /**
         * Gives the state for a number of days.
         *
         * @param daysLeft days until the product expires. A negative number
         *                 means that the product is already bad.
         * @return the state of the product
         */
        fun fromDaysLeft(daysLeft: Long): ExpiryStatus =
            when {
                daysLeft < 0L -> EXPIRED
                daysLeft == 0L -> TODAY
                daysLeft <= URGENT_DAYS -> URGENT
                daysLeft <= SOON_DAYS -> SOON
                else -> FRESH
            }
    }
}
