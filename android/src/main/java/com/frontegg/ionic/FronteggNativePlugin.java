package com.frontegg.ionic;


import android.os.Handler;
import android.os.Looper;

import com.frontegg.android.FronteggApp;
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

        // for regions initialization
        List<RegionConfig> regions = new ArrayList<>();
        JSONArray array;
        try {
            array = this.getConfig().getConfigJSON().optJSONArray("regions");

            if (array == null) {
                array = new JSONArray();
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject regionJson = (JSONObject) array.get(i);
                regions.add(new RegionConfig(
                    regionJson.getString("key"),
                    regionJson.getString("baseUrl"),
                    regionJson.getString("clientId")
                ));
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if(regions.size() == 0) {
            PluginConfig config = this.getConfig();
            String baseUrl = config.getString("baseUrl");
            String clientId = config.getString("clientId");
            if (baseUrl == null || clientId == null) {
                throw new RuntimeException("Missing required config parameters: baseUrl, clientId");
            }
            FronteggApp.Companion.init(
                baseUrl,
                clientId,
                this.getContext()
            );
        }else {
            FronteggApp.Companion.initWithRegions(regions, this.getContext());
        }

        FronteggAuth auth = FronteggAuth.Companion.getInstance();

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
            auth.getShowLoader().getObservable()
        ).subscribe(nullableObject -> {
            debouncer.debounce(this::sendEvent);
        });

        sendEvent();
    }

    private void sendEvent() {
        JSObject data = getData();
        notifyListeners("onFronteggAuthEvent", data);
    }

    private JSObject getData() {
        FronteggAuth auth = FronteggAuth.Companion.getInstance();
        String accessToken = auth.getAccessToken().getValue();
        String refreshToken = auth.getRefreshToken().getValue();
        User user = auth.getUser().getValue();
        boolean isAuthenticated = auth.isAuthenticated().getValue();
        boolean isLoading = auth.isLoading().getValue();
        boolean initializing = auth.getInitializing().getValue();
        boolean showLoader = auth.getShowLoader().getValue();
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
        data.put("selectedRegion", Parser.regionToJSONObject(selectedRegion));

        return data;
    }


    @PluginMethod
    public void login(PluginCall call) {
        FronteggApp.Companion.getInstance().getAuth().login(this.getActivity());
        call.resolve();
    }

    @PluginMethod
    public void logout(PluginCall call) {
        FronteggApp.Companion.getInstance().getAuth().logout(() -> {
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
        FronteggApp.Companion.getInstance().getAuth().switchTenant(tenantId, () -> {
            call.resolve();
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
        FronteggApp.Companion.getInstance().initWithRegion(regionKey);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(call::resolve);
      });
    }

    @PluginMethod
    public void refreshToken(PluginCall call) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            FronteggApp.Companion.getInstance().getAuth().refreshTokenIfNeeded();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(call::resolve);
        });
    }

    @PluginMethod
    public void getAuthState(PluginCall call) {
        call.resolve(getData());
    }


    @PluginMethod
    public void getConstants(PluginCall call) {

        String baseUrl = FronteggAuth.Companion.getInstance().getBaseUrl();
        String clientId = FronteggAuth.Companion.getInstance().getBaseUrl();
        String packageName = getContext().getPackageName();

        List<RegionConfig> regionsData = FronteggApp.Companion.getInstance().getRegions();

        JSObject resultMap = new JSObject();
        resultMap.put("baseUrl", baseUrl);
        resultMap.put("clientId", clientId);
        resultMap.put("bundleId", packageName);
        resultMap.put("isRegional", regionsData.size() > 0);
        resultMap.put("regionData", Parser.regionsToJSONObject(regionsData));

        call.resolve(resultMap);
    }

}
