package com.frontegg.demo.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.widget.EditText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.By
import com.frontegg.android.services.CredentialManager
import org.junit.After
import org.junit.Before
import java.util.regex.Pattern

/**
 * Base class for the mock-server suites, mirroring the iOS MockServerTestCase.
 *
 * Tests share the app process, so the mock server's URL reaches the plugin through system
 * properties, and each launch clears the task so the plugin re-initializes the SDK against it.
 */
open class MockServerTestCase {

    protected lateinit var mock: LocalMockAuthServer
    protected lateinit var app: UiTestInstrumentation

    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun startMockServer() {
        check(isEmbeddedLoginEnabled()) {
            "The mock suites need embedded login; build with -PfronteggEmbeddedLogin"
        }
        // The mock answers HEAD with a body, which breaks the SDK's reused probe connections.
        System.setProperty(KEEP_ALIVE_PROPERTY, "false")
        mock = LocalMockAuthServer()
        mock.start()
        System.setProperty(E2E_BASE_URL_PROPERTY, mock.urlRoot())
        System.setProperty(E2E_CLIENT_ID_PROPERTY, mock.clientId)
        app = UiTestInstrumentation()
    }

    @After
    fun stopMockServer() {
        System.clearProperty(E2E_BASE_URL_PROPERTY)
        System.clearProperty(E2E_CLIENT_ID_PROPERTY)
        System.clearProperty(KEEP_ALIVE_PROPERTY)
        mock.shutdown()
    }

    protected fun launchApp(resetState: Boolean = true) {
        closeRunningActivities()
        if (resetState) {
            CredentialManager(targetContext).wipeAllStoredCredentials()
        }
        app.openApp(clearTask = true)
    }

    protected fun loginViaHostedMock(email: String = DEFAULT_EMAIL, password: String = DEFAULT_PASSWORD) {
        app.waitForView(By.text(labelPattern("Login")), timeout = 30_000)?.click()
            ?: throw AssertionError("Login button did not appear, objects: ${app.getAllObjects()}")

        app.waitForView(By.clazz(EditText::class.java), timeout = 30_000)
            ?: throw AssertionError("Mock login page did not load, objects: ${app.getAllObjects()}")
        app.inputTextByIndex(0, email)
        if (!app.clickByText("Continue")) {
            throw AssertionError("Continue button not found on the mock login page")
        }

        app.waitForView(By.text("Sign in"), timeout = 20_000)
            ?: throw AssertionError("Mock password page did not load, objects: ${app.getAllObjects()}")
        app.inputTextByIndex(1, password)
        app.clickByText("Sign in")
    }

    protected fun waitForAuthenticated(timeout: Long = 60_000) {
        app.waitForView(By.text(labelPattern("Logout")), timeout = timeout)
            ?: throw AssertionError("Did not reach the authenticated state, objects: ${app.getAllObjects()}")
    }

    protected fun tapButton(label: String) {
        app.waitForView(By.text(labelPattern(label)))?.click()
            ?: throw AssertionError("$label button not found, objects: ${app.getAllObjects()}")
    }

    protected fun waitForLoginPage(timeout: Long = 60_000) {
        app.waitForView(By.text("Not Logged In"), timeout = timeout)
            ?: throw AssertionError("Did not reach the login page, objects: ${app.getAllObjects()}")
    }

    protected fun tokenRequestCount(): Int = mock.requestCount("POST", TOKEN_PATH)

    private fun labelPattern(label: String): Pattern =
        Pattern.compile("\\s*${Pattern.quote(label)}\\s*", Pattern.CASE_INSENSITIVE)

    // A live previous bridge can read the SDK mid-reset and trigger a default initialization.
    private fun closeRunningActivities() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            runningActivities().forEach { activity -> activity.finish() }
        }
        val deadline = System.currentTimeMillis() + ACTIVITY_CLOSE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            var remaining = 0
            instrumentation.runOnMainSync { remaining = runningActivities().size }
            if (remaining == 0) {
                return
            }
            delay(200)
        }
        throw AssertionError("Previous activities were not destroyed before relaunch")
    }

    private fun runningActivities(): List<Activity> {
        val lifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance()
        return Stage.values()
            .filter { stage -> stage != Stage.DESTROYED }
            .flatMap { stage -> lifecycleMonitor.getActivitiesInStage(stage) }
    }

    private fun isEmbeddedLoginEnabled(): Boolean {
        val embeddedAuthActivity = ComponentName(targetContext, EMBEDDED_AUTH_ACTIVITY)
        return targetContext.packageManager
            .getActivityInfo(embeddedAuthActivity, PackageManager.MATCH_ALL)
            .isEnabled
    }

    companion object {
        const val DEFAULT_EMAIL = "test@frontegg.com"
        const val DEFAULT_PASSWORD = "Testpassword1!"

        private const val E2E_BASE_URL_PROPERTY = "FRONTEGG_E2E_BASE_URL"
        private const val E2E_CLIENT_ID_PROPERTY = "FRONTEGG_E2E_CLIENT_ID"
        private const val KEEP_ALIVE_PROPERTY = "http.keepAlive"
        private const val EMBEDDED_AUTH_ACTIVITY = "com.frontegg.android.EmbeddedAuthActivity"

        const val TOKEN_PATH = "/oauth/token"

        private const val ACTIVITY_CLOSE_TIMEOUT_MS = 10_000L
    }
}
