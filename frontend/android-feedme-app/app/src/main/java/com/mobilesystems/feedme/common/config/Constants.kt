package com.mobilesystems.feedme.common.config

import com.mobilesystems.feedme.BuildConfig

/**
 * The values that the client reads at each start.
 *
 * The address of the REST API comes from the build type. See the block
 * `buildTypes` in `app/build.gradle`:
 *
 * - a **debug** build and the demo talk to `http://10.0.2.2:80`, which is the
 *   host of an emulator and therefore the API of `docker compose up`;
 * - a **release** build talks to the server of the university.
 */
object Constants {
    /** Address of the REST API. */
    val BASE_URL: String = BuildConfig.API_BASE_URL

    const val CONNECT_TIMEOUT = 10000L
    const val READ_TIMEOUT = 10000L
    const val WRITE_TIMEOUT = 10000L
}
