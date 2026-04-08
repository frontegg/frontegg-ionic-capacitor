package com.frontegg.demo.utils

import android.os.SystemClock
import androidx.test.uiautomator.By

fun delay(ms: Long = 1_000) = SystemClock.sleep(ms)

/**
 * Taps the visible "Login" button on the Ionic example's login page.
 *
 * Adapted from frontegg-android-kotlin: the native Kotlin demo auto-launches
 * hosted login on app start, so its helper just waits for the Frontegg
 * "Sign-in" web view. The Ionic example instead shows a local page with a
 * <ion-button>Login</ion-button>; the user has to tap it first, which then
 * triggers FronteggService.login() and opens the Frontegg hosted login.
 */
fun UiTestInstrumentation.tapLoginButton() {
    // The Ionic button label is "Login" (mixed case). Match case-insensitively
    // so this survives minor copy changes.
    if (!clickByTextIgnoreCase("Login", timeout = 15_000)) {
        throw Exception("'Login' button did not appear, objects: ${getAllObjects()}")
    }

    waitForView(By.text("Sign-in"), timeout = 15_000)
        ?: throw Exception("WebView was not loaded, objects: ${getAllObjects()}")
}

fun UiTestInstrumentation.logout() {
    clickByText("LOGOUT")

    waitForView(By.text("Not authenticated"))
        ?: throw Exception("Logout exception")
}
