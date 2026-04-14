package com.frontegg.ionic;



import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import kotlin.Unit;

import com.frontegg.android.FronteggApp;
import com.frontegg.android.FronteggAppKt;
import com.frontegg.android.FronteggAuth;
import com.frontegg.android.models.User;
import com.frontegg.android.regions.RegionConfig;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginConfig;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

@CapacitorPlugin(name = "FronteggNative")
public class FronteggNativePlugin extends Plugin {
    private Disposable disposable = null;
    private final Debouncer debouncer = new Debouncer(50);  // 200ms delay


    @Override
    public void load() {

        Log.d("FronteggNative", "Loading FronteggNativePlugin");
        // for regions initialization
        List<RegionConfig> regions = new ArrayList<>();
        boolean useAssetLinks = false;
        boolean useChromeCustomTabs = false;
        JSONArray array;
        try {
            JSONObject configJson = this.getConfig().getConfigJSON();

            useAssetLinks = configJson.has("useAssetLinks")
                    ? configJson.optBoolean("useAssetLinks", false)
                    : false;

            useChromeCustomTabs = configJson.has("useChromeCustomTabs")
                    ? configJson.optBoolean("useChromeCustomTabs", false)
                    : false;

            array = configJson.optJSONArray("regions");

            if (array == null) {
                array = new JSONArray();
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject regionJson = (JSONObject) array.get(i);
                String applicationId = null;
                if (regionJson.has("applicationId")) {
                    applicationId = regionJson.getString("applicationId");
                }
                regions.add(new RegionConfig(
                        regionJson.getString("key"),
                        regionJson.getString("baseUrl"),
                        regionJson.getString("clientId"),
                        applicationId
                ));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Class<?> mainActivityClass = resolveMainActivityClass();
        String deepLinkScheme;
        if (useAssetLinks) {
            deepLinkScheme = null;
        } else {
            deepLinkScheme = this.getConfig().getString("deepLinkScheme");
            if (deepLinkScheme == null || deepLinkScheme.isEmpty()) {
                deepLinkScheme = this.getContext().getPackageName();
            }
        }
        boolean useDiskCacheWebView = this.getConfig().getBoolean("useDiskCacheWebView", false);

        if (regions.isEmpty()) {
            PluginConfig config = this.getConfig();
            String baseUrl = config.getString("baseUrl");
            String clientId = config.getString("clientId");
            String applicationId = config.getString("applicationId");

            // Allow E2E tests to override the base URL via system property.
            // Tests set this via: System.setProperty("FRONTEGG_E2E_BASE_URL", url)
            String e2eBaseUrl = System.getProperty("FRONTEGG_E2E_BASE_URL");
            if (e2eBaseUrl != null && !e2eBaseUrl.isEmpty()) {
                Log.i("FronteggNative", "E2E override: using base URL " + e2eBaseUrl);
                baseUrl = e2eBaseUrl;
            }

            if (baseUrl == null || clientId == null) {
                throw new RuntimeException("Missing required config parameters: baseUrl, clientId");
            }
            if (baseUrl.startsWith("https://")) {
                baseUrl = baseUrl.substring(baseUrl.indexOf("://") + 3);
            }
            FronteggApp.Companion.init$android_release(
                    baseUrl,
                    clientId,
                    this.getContext(),
                    applicationId,
                    useAssetLinks,
                    useChromeCustomTabs,
                    mainActivityClass,
                    deepLinkScheme,
                    useDiskCacheWebView,
                    false,
                    false,
                    null
            );
        } else {
            FronteggApp.Companion.initWithRegions(
                    regions,
                    this.getContext(),
                    useAssetLinks,
                    useChromeCustomTabs,
                    mainActivityClass,
                    useDiskCacheWebView,
                    false,
                    false,
                    null
            );
        }

        FronteggAuth auth = FronteggAppKt.getFronteggAuth(this.getContext());

        if (this.disposable != null) {
            this.disposable.dispose();
        }
        this.disposable = Observable.mergeArray(
                auth.getAccessToken().getObservable(),
                auth.getRefreshToken().getObservable(),
                auth.getUser().getObservable(),
                auth.isAuthenticated().getObservable(),
                auth.isLoading().getObservable(),
                auth.getInitializing().getObservable(),
                auth.getShowLoader().getObservable(),
                auth.getRefreshingToken().getObservable()
        ).subscribe(nullableObject -> {
            debouncer.debounce(this::sendEvent);
        });

        sendEvent();
    }

    private Class<?> resolveMainActivityClass() {
        String className = this.getConfig().getString("mainActivityClass");
        if (className == null || className.isEmpty()) {
            className = this.getContext().getPackageName() + ".MainActivity";
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.w("FronteggNative", "MainActivity class not found: " + className + ", using null");
            return null;
        }
    }

    private void sendEvent() {
        JSObject data = getData();
        notifyListeners("onFronteggAuthEvent", data);
    }

    private JSObject getData() {
        FronteggAuth auth = FronteggAppKt.getFronteggAuth(this.getContext());
        String accessToken = auth.getAccessToken().getValue();
        String refreshToken = auth.getRefreshToken().getValue();
        User user = auth.getUser().getValue();
        boolean isAuthenticated = auth.isAuthenticated().getValue();
        boolean showLoader = auth.getShowLoader().getValue();
        boolean isLoading = showLoader;
        boolean initializing = auth.getInitializing().getValue();
        boolean refreshingToken = auth.getRefreshingToken().getValue();
        RegionConfig selectedRegion = auth.getSelectedRegion();

        JSObject data = new JSObject();

        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        if (user != null) {
            data.put("user", Parser.userToJSONObject(user));
        }
        data.put("isAuthenticated", isAuthenticated);
        data.put("isLoading", isLoading);
        data.put("initializing", initializing);
        data.put("showLoader", showLoader);
        data.put("refreshingToken", refreshingToken);
        data.put("selectedRegion", Parser.regionToJSONObject(selectedRegion));

        return data;
    }


    @PluginMethod
    public void login(PluginCall call) {
        String loginHint = call.getString("loginHint");
        FronteggAppKt.getFronteggAuth(this.getContext()).login(this.getActivity(), loginHint != null ? loginHint : "", null, (Exception e) -> {
            call.resolve();
            return Unit.INSTANCE;
        });
    }

    @PluginMethod
    public void directLoginAction(PluginCall call) {
        String type = call.getString("type");
        String data = call.getString("data");

        if (type == null || data == null) {
            call.reject("No type or data provided");
            return;
        }
        FronteggAppKt.getFronteggAuth(this.getContext()).directLoginAction(this.getActivity(), type, data, (Exception e) -> {
            call.resolve();
            return Unit.INSTANCE;
        });
    }

    @PluginMethod
    public void logout(PluginCall call) {
        FronteggAppKt.getFronteggAuth(this.getContext()).logout(() -> {
            call.resolve();
            return null;
        });
    }

    @PluginMethod
    public void switchTenant(PluginCall call) {
        String tenantId = call.getString("tenantId");
        if (tenantId == null) {
            call.reject("No tenantId provided");
            return;
        }
        FronteggAppKt.getFronteggAuth(this.getContext()).switchTenant(tenantId, (success) -> {
            JSObject result = new JSObject();
            result.put("success", success);
            call.resolve((JSObject) result);
            return null;
        });
    }


    @PluginMethod
    public void initWithRegion(PluginCall call) {
        String regionKey = call.getString("regionKey");
        if (regionKey == null) {
            call.reject("No regionKey provided");
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            FronteggApp.Companion.getInstance$android_release().initWithRegion(regionKey);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(call::resolve);
        });
    }


    @PluginMethod
    public void refreshToken(PluginCall call) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            FronteggAuth fronteggAuth = FronteggAppKt.getFronteggAuth(this.getContext());
            if (!fronteggAuth.refreshTokenIfNeeded()) {
                fronteggAuth.logout(() -> {
                    handler.post(call::resolve);
                    return null;
                });
            } else {
                handler.post(call::resolve);
            }
        });
    }


    @PluginMethod
    public void getAuthState(PluginCall call) {
        call.resolve(getData());
    }


    @PluginMethod
    public void getConstants(PluginCall call) {

        String baseUrl = FronteggAppKt.getFronteggAuth(this.getContext()).getBaseUrl();
        String clientId = FronteggAppKt.getFronteggAuth(this.getContext()).getClientId();
        String applicationId = FronteggAppKt.getFronteggAuth(this.getContext()).getApplicationId();
        String packageName = getContext().getPackageName();

        List<RegionConfig> regionsData = FronteggAppKt.getFronteggAuth(this.getContext()).getRegions();

        JSObject resultMap = new JSObject();
        resultMap.put("baseUrl", baseUrl);
        resultMap.put("clientId", clientId);
        resultMap.put("bundleId", packageName);
        resultMap.put("applicationId", applicationId);
        resultMap.put("isRegional", regionsData.size() > 0);
        resultMap.put("regionData", Parser.regionsToJSONObject(regionsData));

        call.resolve(resultMap);
    }

}
