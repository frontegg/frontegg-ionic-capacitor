package com.frontegg.demo.utils

import java.util.regex.Pattern
import androidx.test.uiautomator.By

/**
 * Performs a full email+password login flow against the Frontegg hosted login.
 *
 * 1. Taps the local "Login" button on the Ionic login page.
 * 2. Fills in email and password on the Frontegg hosted web view.
 * 3. Waits for the authenticated state (Logout button visible).
 */
fun UiTestInstrumentation.performLogin(
    email: String = Env.loginEmail,
    password: String = Env.loginPassword
) {
    tapLoginButton()

    inputTextByIndex(0, email)
    clickByText("Continue", timeout = 3_000)
    inputTextByIndex(1, password)
    clickByText("Sign in")

    // Wait for the authenticated state — the profile tab shows a "Logout" button.
    val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
    waitForView(By.text(logoutPattern), timeout = 30_000)
        ?: throw Exception("Login did not complete — Logout button not found")
}
