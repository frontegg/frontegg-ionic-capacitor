package com.frontegg.ionic

import android.app.Activity
import com.frontegg.android.FronteggAuth

/**
 * The native step-up APIs take a Kotlin Duration (an inline value class), which gives them
 * mangled JVM names that Java cannot call. These wrappers expose them to FronteggNativePlugin.
 *
 * `maxAge` is honored on iOS but not yet forwarded here; null checks ACR/AMR without the
 * freshness window.
 */
internal object StepUpBridge {
    @JvmStatic
    fun isSteppedUp(auth: FronteggAuth): Boolean = auth.isSteppedUp(null)

    @JvmStatic
    fun stepUp(auth: FronteggAuth, activity: Activity, callback: (Exception?) -> Unit) {
        auth.stepUp(activity, null, callback)
    }
}
