package com.mobilesystems.feedme.ui.common.utils

import android.content.Context
import com.mobilesystems.feedme.R
import kotlin.math.abs

/**
 * Writes the number of days until a product expires as a text for the user.
 *
 * The function uses the plural forms of the Android framework, so the text is
 * correct for one day and for more than one day. The first version of the
 * application wrote "-2 days" for a product that was already bad.
 *
 * @param context context that holds the texts
 * @param daysLeft days until the product expires. A negative number means
 *                 that the product is already bad.
 * @return the text for the user, for example "3 days left" or "2 days ago"
 */
fun expiryLabel(context: Context, daysLeft: Long): String {
    val days = abs(daysLeft).toInt()
    return when {
        daysLeft < 0L -> context.resources.getQuantityString(
            R.plurals.expiry_days_ago, days, days
        )
        daysLeft == 0L -> context.getString(R.string.expiry_status_today)
        else -> context.resources.getQuantityString(R.plurals.expiry_days_left, days, days)
    }
}
