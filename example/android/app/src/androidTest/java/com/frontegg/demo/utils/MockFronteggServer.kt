package com.frontegg.demo.utils

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * In-process mock that replaces the Frontegg backend for E2E tests.
 *
 * Wraps OkHttp [MockWebServer] with a [Dispatcher] that routes requests to
 * handlers for the most-used Frontegg API endpoints.  Token lifetimes, error
 * injection, and request counting are all configurable per-test.
 */
class MockFronteggServer {

    val server = MockWebServer()

    /* ── configurable token policy ─────────────────────────────── */

    /** Access-token TTL in seconds (default 1 hour). */
    private val accessTokenTTL = AtomicLong(3600)

    /** Refresh-token TTL in seconds (default 30 days). */
    private val refreshTokenTTL = AtomicLong(2_592_000)

    /** Monotonically increasing counter so every token is unique. */
    private val tokenCounter = AtomicInteger(0)

    /* ── connection-drop queue ─────────────────────────────────── */

    /** path -> remaining drop count */
    private val connectionDrops = ConcurrentHashMap<String, AtomicInteger>()

    /* ── request recording ─────────────────────────────────────── */

    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val requestLog = CopyOnWriteArrayList<RecordedRequest>()
    private val requestQueues = ConcurrentHashMap<String, LinkedBlockingQueue<RecordedRequest>>()

    /* ── lifecycle ─────────────────────────────────────────────── */

    fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""

                // Record the request.
                val normalised = normalisePath(path)
                requestCounts.getOrPut(normalised) { AtomicInteger(0) }.incrementAndGet()
                requestLog.add(request)
                requestQueues.getOrPut(normalised) { LinkedBlockingQueue() }.add(request)

                // Connection-drop injection.
                connectionDrops[normalised]?.let { counter ->
                    if (counter.getAndDecrement() > 0) {
                        return MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                    }
                }

                return route(request, path)
            }
        }
        server.start()
    }

    fun shutdown() {
        server.shutdown()
    }

    fun getBaseUrl(): String = server.url("/").toString().trimEnd('/')

    /* ── configuration helpers ─────────────────────────────────── */

    /**
     * Set token lifetimes used in subsequent token responses.
     *
     * @param accessTTL  access-token lifetime in seconds
     * @param refreshTTL refresh-token lifetime in seconds
     */
    fun configureTokenPolicy(accessTTL: Long, refreshTTL: Long) {
        accessTokenTTL.set(accessTTL)
        refreshTokenTTL.set(refreshTTL)
    }

    /**
     * Enqueue [count] connection drops for requests whose normalised path
     * matches [path].  The drops are consumed FIFO; subsequent requests
     * succeed normally.
     */
    fun queueConnectionDrops(path: String, count: Int) {
        connectionDrops[normalisePath(path)] = AtomicInteger(count)
    }

    /* ── request introspection ─────────────────────────────────── */

    /** Number of requests received for [path]. */
    fun requestCount(path: String): Int =
        requestCounts[normalisePath(path)]?.get() ?: 0

    /**
     * Block until a request for [path] arrives or [timeoutMs] elapses.
     * Returns the [RecordedRequest] or `null` on timeout.
     */
    fun waitForRequest(path: String, timeoutMs: Long = 10_000): RecordedRequest? {
        val queue = requestQueues.getOrPut(normalisePath(path)) { LinkedBlockingQueue() }
        return queue.poll(timeoutMs, TimeUnit.MILLISECONDS)
    }

    /** Clear all recorded state (counts, logs, drop queues). */
    fun reset() {
        requestCounts.clear()
        requestLog.clear()
        requestQueues.clear()
        connectionDrops.clear()
        tokenCounter.set(0)
        accessTokenTTL.set(3600)
        refreshTokenTTL.set(2_592_000)
    }

    /* ── routing ───────────────────────────────────────────────── */

    private fun route(request: RecordedRequest, path: String): MockResponse {
        return when {
            // HEAD /test — probe endpoint used by SDK connectivity checks.
            request.method.equals("HEAD", ignoreCase = true) && path.startsWith("/test") ->
                MockResponse().setResponseCode(200)

            path.startsWith("/oauth/token") ->
                handleOAuthToken(request)

            path.contains("/auth/v1/user/token/refresh") ->
                handleTokenRefresh(request)

            path.contains("/users/v2/me") ->
                handleUserProfile()

            path.contains("/users/v3/me/tenants") ->
                handleTenantList()

            path.startsWith("/oauth/logout") ->
                handleLogout()

            // Well-known / OIDC discovery (some SDK versions fetch this).
            path.contains("/.well-known/openid-configuration") ->
                handleOIDCDiscovery()

            else ->
                MockResponse().setResponseCode(404)
                    .setBody("""{"error":"not_found","path":"$path"}""")
        }
    }

    /* ── endpoint handlers ─────────────────────────────────────── */

    private fun handleOAuthToken(request: RecordedRequest): MockResponse {
        val seq = tokenCounter.incrementAndGet()
        val body = JSONObject().apply {
            put("access_token", "mock-access-token-$seq")
            put("refresh_token", "mock-refresh-token-$seq")
            put("token_type", "bearer")
            put("expires_in", accessTokenTTL.get())
        }
        return jsonResponse(body)
    }

    private fun handleTokenRefresh(request: RecordedRequest): MockResponse {
        val seq = tokenCounter.incrementAndGet()
        val body = JSONObject().apply {
            put("access_token", "mock-access-token-$seq")
            put("refresh_token", "mock-refresh-token-$seq")
            put("token_type", "bearer")
            put("expires_in", accessTokenTTL.get())
        }
        return jsonResponse(body)
    }

    private fun handleUserProfile(): MockResponse {
        val tenant = JSONObject().apply {
            put("tenantId", "mock-tenant-001")
            put("name", "Mock Tenant")
        }
        val body = JSONObject().apply {
            put("id", "mock-user-001")
            put("email", "e2e-test@frontegg.com")
            put("name", "E2E Test User")
            put("profilePictureUrl", "")
            put("verified", true)
            put("mfaEnrolled", false)
            put("activatedForTenant", true)
            put("tenantId", "mock-tenant-001")
            put("tenantIds", JSONArray().put("mock-tenant-001"))
            put("tenants", JSONArray().put(tenant))
            put("roles", JSONArray())
            put("permissions", JSONArray())
            put("metadata", JSONObject())
        }
        return jsonResponse(body)
    }

    private fun handleTenantList(): MockResponse {
        val tenant = JSONObject().apply {
            put("tenantId", "mock-tenant-001")
            put("name", "Mock Tenant")
            put("isActive", true)
        }
        return jsonResponse(JSONArray().put(tenant), isArray = true)
    }

    private fun handleLogout(): MockResponse {
        return MockResponse().setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""{"success":true}""")
    }

    private fun handleOIDCDiscovery(): MockResponse {
        val base = getBaseUrl()
        val body = JSONObject().apply {
            put("issuer", base)
            put("authorization_endpoint", "$base/oauth/authorize")
            put("token_endpoint", "$base/oauth/token")
            put("userinfo_endpoint", "$base/identity/resources/users/v2/me")
            put("jwks_uri", "$base/.well-known/jwks.json")
        }
        return jsonResponse(body)
    }

    /* ── helpers ───────────────────────────────────────────────── */

    private fun jsonResponse(body: JSONObject): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body.toString())

    private fun jsonResponse(body: JSONArray, isArray: Boolean = false): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(body.toString())

    /** Strip query string and collapse double slashes. */
    private fun normalisePath(path: String): String {
        val noQuery = path.substringBefore('?')
        return noQuery.replace(Regex("/+"), "/")
    }
}
