package com.frontegg.ionic

import android.content.Context
import com.frontegg.android.FronteggAuth
import com.frontegg.android.models.User
import com.frontegg.android.utils.FronteggConstantsProvider
import com.frontegg.android.utils.JWTHelper
import com.frontegg.android.utils.NetworkGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// Wraps native session APIs that Java cannot call directly: suspend functions and Kotlin Result callbacks.
internal object SessionBridge {
    private const val MIN_ACCESS_TOKEN_VALIDITY_SECONDS = 15
    private const val SETTLE_TIMEOUT_MILLIS = 20_000L
    private const val REFRESH_SETTLE_TIMEOUT_MILLIS = 10_000L
    private const val SETTLE_POLL_INTERVAL_MILLIS = 200L

    @JvmStatic
    fun requestAuthorize(
        auth: FronteggAuth,
        refreshToken: String,
        deviceTokenCookie: String?,
        callback: (User?, Exception?) -> Unit,
    ) {
        auth.requestAuthorize(refreshToken, deviceTokenCookie) { result ->
            callback(result.getOrNull(), result.exceptionOrNull() as? Exception)
        }
    }

    @JvmStatic
    fun getOrRefreshAccessToken(auth: FronteggAuth, context: Context, callback: (String?, Exception?) -> Unit) {
        val applicationContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = try {
                resolveValidAccessToken(auth, applicationContext)
            } catch (exception: Exception) {
                callback(null, exception)
                return@launch
            }
            callback(accessToken, null)
        }
    }

    private suspend fun resolveValidAccessToken(auth: FronteggAuth, context: Context): String? {
        if (auth.isMultiRegion && auth.selectedRegion == null) {
            return null
        }
        val settled = waitWhile(SETTLE_TIMEOUT_MILLIS) { auth.initializing.value || auth.refreshingToken.value }
        if (!settled) {
            throw IllegalStateException("Timed out after 20 seconds waiting for initialization or token refresh to finish")
        }
        if (auth.refreshToken.value == null) {
            return null
        }

        val currentAccessToken = auth.accessToken.value
        if (isValidLongEnough(currentAccessToken)) {
            return currentAccessToken
        }

        val offlineModeEnabled = isOfflineModeEnabled(context)
        if (!NetworkGate.isNetworkLikelyGood(context)) {
            if (offlineModeEnabled) {
                return currentAccessToken
            }
            throw IllegalStateException("No network connection to refresh the access token")
        }

        if (refreshUntilValid(auth)) {
            return auth.accessToken.value
        }
        if (auth.refreshToken.value == null) {
            return null
        }
        if (offlineModeEnabled && !NetworkGate.isNetworkLikelyGood(context)) {
            return auth.accessToken.value
        }
        throw IllegalStateException("Failed to refresh the access token")
    }

    // A refresh started by the SDK's background job makes refreshTokenAndWait return true before the new token is set.
    private suspend fun refreshUntilValid(auth: FronteggAuth): Boolean {
        if (!auth.refreshTokenAndWait(force = false)) {
            return false
        }
        if (isValidLongEnough(auth.accessToken.value)) {
            return true
        }
        waitWhile(REFRESH_SETTLE_TIMEOUT_MILLIS) { auth.refreshingToken.value }
        if (isValidLongEnough(auth.accessToken.value)) {
            return true
        }
        return auth.refreshTokenAndWait(force = false) && isValidLongEnough(auth.accessToken.value)
    }

    private suspend fun waitWhile(timeoutMillis: Long, condition: () -> Boolean): Boolean {
        val finished = withTimeoutOrNull(timeoutMillis) {
            while (condition()) {
                delay(SETTLE_POLL_INTERVAL_MILLIS)
            }
            true
        }
        return finished != null
    }

    private fun isOfflineModeEnabled(context: Context): Boolean = runCatching {
        FronteggConstantsProvider.fronteggConstants(context).enableOfflineMode
    }.getOrDefault(false)

    private fun isValidLongEnough(accessToken: String?): Boolean {
        if (accessToken == null) {
            return false
        }
        val expiresAtSeconds = runCatching { JWTHelper.decode(accessToken).exp }.getOrDefault(0L)
        return expiresAtSeconds - System.currentTimeMillis() / 1000 > MIN_ACCESS_TOKEN_VALIDITY_SECONDS
    }
}
