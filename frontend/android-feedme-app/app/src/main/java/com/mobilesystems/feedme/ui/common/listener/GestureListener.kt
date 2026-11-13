package com.mobilesystems.feedme.ui.common.listener

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

/**
 * Reads a swipe of the user on a list or on a card.
 *
 * Android 13 changed the signature of the methods: `onDown` receives an event
 * that is never null, and the first event of `onFling` can be null when the
 * swipe starts outside the view.
 */
open class GestureListener : GestureDetector.SimpleOnGestureListener() {

    private companion object {
        /** Shortest way of a finger that counts as a swipe, in pixels. */
        const val SWIPE_THRESHOLD = 100

        /** Lowest speed of a finger that counts as a swipe. */
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (event1 == null) {
            return false
        }

        val diffX = event2.x - event1.x
        val diffY = event2.y - event1.y

        return if (abs(diffX) > abs(diffY)) {
            handleHorizontalSwipe(diffX, velocityX)
        } else {
            handleVerticalSwipe(diffY, velocityY)
        }
    }

    /** The user moved the finger to the right. */
    open fun onSwipeRight() = Unit

    /** The user moved the finger to the left. */
    open fun onSwipeLeft() = Unit

    /** The user moved the finger up. */
    open fun onSwipeTop() = Unit

    /** The user moved the finger down. */
    open fun onSwipeBottom() = Unit

    private fun handleHorizontalSwipe(diffX: Float, velocityX: Float): Boolean {
        if (abs(diffX) <= SWIPE_THRESHOLD || abs(velocityX) <= SWIPE_VELOCITY_THRESHOLD) {
            return false
        }
        if (diffX > 0) onSwipeRight() else onSwipeLeft()
        return true
    }

    private fun handleVerticalSwipe(diffY: Float, velocityY: Float): Boolean {
        if (abs(diffY) <= SWIPE_THRESHOLD || abs(velocityY) <= SWIPE_VELOCITY_THRESHOLD) {
            return false
        }
        if (diffY > 0) onSwipeBottom() else onSwipeTop()
        return true
    }
}
