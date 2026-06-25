package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.Env
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.delay
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.tapLoginButton
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

/**
 * P2: Verifies Google social login via Chrome Custom Tabs.
 *
 * Mirrors frontegg-android-kotlin's LoginViaGoogleTest. The flow:
 * 1. Tap Login on the Ionic login page.
 * 2. On the Frontegg hosted login, tap the Google social button.
 * 3. Chrome Custom Tab opens with Google's sign-in.
 * 4. Authenticate with Google test credentials.
 * 5. Return to the app — Logout button appears.
 *
 * Requires GOOGLE_EMAIL and GOOGLE_PASSWORD instrumentation arguments.
 * Skipped if credentials are not provided.
 */
class GoogleSocialLoginTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun success_login_with_google() {
        // Skip if Google credentials not provided.
        assumeTrue(
            "GOOGLE_EMAIL not set — skipping Google social login test",
            Env.googleEmail.isNotEmpty()
        )
        assumeTrue(
            "GOOGLE_PASSWORD not set — skipping Google social login test",
            Env.googlePassword.isNotEmpty()
        )

        // 1. Open the Frontegg hosted login.
        instrumentation.tapLoginButton()

        // 2. Tap the Google social login button on the Frontegg hosted page.
        //    The button may be labeled "Continue with Google" or show a Google icon.
        val googleButton = instrumentation.waitForView(
            By.text(Pattern.compile(".*google.*", Pattern.CASE_INSENSITIVE)),
            timeout = 10_000
        ) ?: throw Exception("Google social login button not found on hosted login page")
        googleButton.click()

        // 3. Chrome Custom Tab opens. Wait for Google's sign-in page.
        delay(3_000) // allow Chrome Custom Tab to open

        // Handle "Choose an account" / sign-in form.
        // First, try to find an email input field.
        val emailField = instrumentation.waitForView(
            By.clazz(android.widget.EditText::class.java),
            timeout = 10_000
        )
        if (emailField != null) {
            emailField.text = Env.googleEmail
            instrumentation.clickByText("Next", timeout = 5_000)

            delay(2_000)

            // Password field.
            val passwordField = instrumentation.waitForView(
                By.clazz(android.widget.EditText::class.java),
                timeout = 10_000
            )
            if (passwordField != null) {
                passwordField.text = Env.googlePassword
                instrumentation.clickByText("Next", timeout = 5_000)
            }
        }

        // 4. Handle potential consent screens ("Accept & continue", "No thanks").
        delay(2_000)
        instrumentation.clickByText("Accept & continue", timeout = 3_000)
        delay(1_000)
        instrumentation.clickByText("No thanks", timeout = 3_000)

        // 5. Verify authenticated state — Logout button appears.
        val logoutPattern = Pattern.compile("logout", Pattern.CASE_INSENSITIVE)
        instrumentation.waitForView(By.text(logoutPattern), timeout = 30_000)
            ?: throw Exception("Google login did not complete — Logout button not found")

        // 6. Cleanup.
        instrumentation.logout()
    }
}
