package com.frontegg.ionic

import com.frontegg.android.FronteggAuth
import com.frontegg.android.models.User
import com.frontegg.android.utils.JWTHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

// Wraps native session APIs that Java cannot call directly: suspend functions and Kotlin Result callbacks.
internal object SessionBridge {
    private const val MIN_ACCESS_TOKEN_VALIDITY_SECONDS = 15
    private const val SETTLE_TIMEOUT_MILLIS = 20_000L
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
    fun getOrRefreshAccessToken(auth: FronteggAuth, callback: (String?, Exception?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = try {
                resolveValidAccessToken(auth)
            } catch (exception: Exception) {
                callback(null, exception)
                return@launch
            }
            callback(accessToken, null)
        }
    }

    private suspend fun resolveValidAccessToken(auth: FronteggAuth): String? {
        withTimeoutOrNull(SETTLE_TIMEOUT_MILLIS) {
            while (auth.initializing.value || auth.refreshingToken.value) {
                delay(SETTLE_POLL_INTERVAL_MILLIS)
            }
        }
        if (auth.refreshToken.value == null) {
            return null
        }

        val currentAccessToken = auth.accessToken.value
        if (currentAccessToken != null && secondsUntilExpiry(currentAccessToken) > MIN_ACCESS_TOKEN_VALIDITY_SECONDS) {
            return currentAccessToken
        }

        if (auth.refreshTokenAndWait(force = false)) {
            return auth.accessToken.value
        }
        if (auth.refreshToken.value == null) {
            return null
        }
        throw IllegalStateException("Failed to refresh the access token")
    }

    private fun secondsUntilExpiry(accessToken: String): Long {
        val expiresAtSeconds = runCatching { JWTHelper.decode(accessToken).exp }.getOrDefault(0L)
        return expiresAtSeconds - System.currentTimeMillis() / 1000
    }
}
