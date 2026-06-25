package com.frontegg.demo.utils

import androidx.test.platform.app.InstrumentationRegistry

object Env {
    val loginEmail: String
        get() = getStringByName("LOGIN_EMAIL")

    val loginPassword: String
        get() = getStringByName("LOGIN_PASSWORD")

    val loginWrongEmail: String
        get() = getStringByName("LOGIN_WRONG_EMAIL")

    val loginWrongPassword: String
        get() = getStringByName("LOGIN_WRONG_PASSWORD")

    val googleEmail: String
        get() = getStringByName("GOOGLE_EMAIL")

    val googlePassword: String
        get() = getStringByName("GOOGLE_PASSWORD")

    private fun getStringByName(name: String) =
        InstrumentationRegistry.getArguments().getString(name) ?: ""
}
