package com.mobilesystems.feedme.common.logging

import android.util.Log

/*
 * One place for the log of the client.
 *
 * The first version called `e.printStackTrace()` in 20 places and
 * `Log.e("DataSource", ...)` in the data layer. `printStackTrace()` writes to
 * the standard output of the process, which Android drops, and each class used
 * its own name. A reader of a bug report therefore saw nothing or had to know
 * many names.
 *
 * All functions of this file write to Logcat under the one name "Fudge". A
 * reader follows the client with one command:
 *
 *     adb logcat -s Fudge
 *
 * The functions keep the behaviour of the first version: the application does
 * not stop, and the caller gets a default value.
 */

/** Name under which the client writes every line to Logcat. */
const val LOG_TAG = "Fudge"

/**
 * Writes a line that helps to find an error. The line is of interest during
 * the development only.
 *
 * @param message text of the line
 */
fun logDebug(message: String) {
    Log.d(LOG_TAG, message)
}

/**
 * Writes a line about a step of the application, for example the answer of the
 * server.
 *
 * @param message text of the line
 */
fun logInfo(message: String) {
    Log.i(LOG_TAG, message)
}

/**
 * Writes a line about a problem that the application can accept.
 *
 * @param message text of the line
 * @param error error that belongs to the line, if there is one
 */
fun logWarn(
    message: String,
    error: Throwable? = null,
) {
    if (error == null) {
        Log.w(LOG_TAG, message)
    } else {
        Log.w(LOG_TAG, message, error)
    }
}

/**
 * Writes an error to Logcat.
 *
 * @param description short text that says which operation failed, for example
 *                    "read the inventory list"
 * @param error error that the operation threw
 */
fun logError(
    description: String,
    error: Throwable,
) {
    Log.e(LOG_TAG, "Failed to $description: ${error.message}", error)
}

/**
 * Writes an error that threw no exception, for example a status code of the
 * server.
 *
 * @param description short text that says which operation failed
 * @param reason text that says why the operation failed
 */
fun logError(
    description: String,
    reason: String,
) {
    Log.e(LOG_TAG, "Failed to $description: $reason")
}

/**
 * Runs an operation and gives its result. If the operation throws, the
 * function writes the error to Logcat and gives the fallback value.
 *
 * @param T type of the result
 * @param description short text that says which operation failed
 * @param fallback value that the function gives when the operation throws
 * @param operation operation to run
 * @return the result of the operation, or the fallback value
 */
inline fun <T> attemptOrDefault(
    description: String,
    fallback: T,
    operation: () -> T,
): T =
    try {
        operation()
    } catch (error: Exception) {
        logError(description, error)
        fallback
    }
