package com.frontegg.ionic

import android.app.Activity
import com.frontegg.android.FronteggAuth
import kotlin.time.Duration.Companion.seconds

/**
 * The native step-up APIs take a Kotlin Duration (an inline value class), which gives them
 * mangled JVM names that Java cannot call. These wrappers expose them to FronteggNativePlugin
 * and convert `maxAge` from seconds; null checks ACR/AMR without a freshness window.
 */
internal object StepUpBridge {
    @JvmStatic
    fun isSteppedUp(auth: FronteggAuth, maxAgeSeconds: Double?): Boolean =
        auth.isSteppedUp(maxAgeSeconds?.seconds)

    @JvmStatic
    fun stepUp(auth: FronteggAuth, activity: Activity, maxAgeSeconds: Double?, callback: (Exception?) -> Unit) {
        auth.stepUp(activity, maxAgeSeconds?.seconds, callback)
    }
}
