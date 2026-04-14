package com.frontegg.demo

import androidx.test.uiautomator.By
import com.frontegg.demo.utils.Env
import com.frontegg.demo.utils.UiTestInstrumentation
import com.frontegg.demo.utils.logout
import com.frontegg.demo.utils.tapLoginButton
import org.junit.Before
import org.junit.Test

class LogoutTest {
    private lateinit var instrumentation: UiTestInstrumentation

    @Before
    fun setUp() {
        instrumentation = UiTestInstrumentation()
        instrumentation.openApp()
    }

    @Test
    fun success_Log_Out() {
        instrumentation.tapLoginButton()

        instrumentation.inputTextByIndex(0, Env.loginEmail)

        instrumentation.clickByText("Continue")

        instrumentation.inputTextByIndex(1, Env.loginPassword)

        instrumentation.clickByText("Sign in")

        instrumentation.clickByTextIgnoreCase("Logout")

        instrumentation.waitForView(By.text("Not Logged In"), timeout = 10_000)
            ?: throw Exception("Logout exception — 'Not Logged In' text not found")
    }
}
