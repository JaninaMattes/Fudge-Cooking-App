package com.mobilesystems.feedme.ui.common.listener

import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Future usage.
 */
class GestureListener: GestureDetector.SimpleOnGestureListener() {

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onDown(e: MotionEvent?): Boolean {
        return super.onDown(e)
    }

    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {

        val diffY = event2.y - event1.y
        val diffX = event2.x - event1.x

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
            }
        } else {
            if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeBottom()
                } else {
                    onSwipeTop()
                }
            }
        }
        return true
    }

    private fun onSwipeTop() {
        TODO("Not yet implemented")
    }

    private fun onSwipeBottom() {
        TODO("Not yet implemented")
    }

    private fun onSwipeLeft() {
        TODO("Not yet implemented")
    }

    private fun onSwipeRight() {
        TODO("Not yet implemented")
    }

}