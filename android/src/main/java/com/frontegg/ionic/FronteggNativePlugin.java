package com.frontegg.ionic;


import com.frontegg.android.FronteggApp;
import com.frontegg.android.FronteggAuth;
import com.frontegg.android.models.User;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

@CapacitorPlugin(name = "FronteggNative")
public class FronteggNativePlugin extends Plugin {
    private Disposable disposable = null;
    private Debouncer debouncer = new Debouncer(200);  // 200ms delay
    @Override
    public void load() {
        Map<String, String> constants = this.getConstants();

        FronteggApp.Companion.init(
            Objects.requireNonNull(constants.get("baseUrl")),
            Objects.requireNonNull(constants.get("clientId")),
            this.getContext()
        );


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
    public void getAuthState(PluginCall call) {
        call.resolve(getData());
    }


    public Map<String, String> getConstants() {
        String packageName = getContext().getPackageName();
        String className = packageName + ".BuildConfig";

        try {
            Class<?> buildConfigClass = Class.forName(className);

            // Get the field from BuildConfig class
            Field baseUrlField = buildConfigClass.getField("FRONTEGG_DOMAIN");
            Field clientIdField = buildConfigClass.getField("FRONTEGG_CLIENT_ID");
            String baseUrl = (String) baseUrlField.get(null); // Assuming it's a String
            String clientId = (String) clientIdField.get(null); // Assuming it's a String

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("baseUrl", baseUrl);
            resultMap.put("clientId", clientId);
            resultMap.put("bundleId", packageName);

            return resultMap;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + className);
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            System.out.println(
                "Field not found in BuildConfig: " +
                    "buildConfigField \"String\", 'FRONTEGG_DOMAIN', \"\\\"$fronteggDomain\\\"\"\n" +
                    "buildConfigField \"String\", 'FRONTEGG_CLIENT_ID', \"\\\"$fronteggClientId\\\"\""
            );
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            System.out.println(
                "Access problem with field in BuildConfig: " +
                    "buildConfigField \"String\", 'FRONTEGG_DOMAIN', \"\\\"$fronteggDomain\\\"\"\n" +
                    "buildConfigField \"String\", 'FRONTEGG_CLIENT_ID', \"\\\"$fronteggClientId\\\"\""
            );
            throw new RuntimeException(e);
        }
    }

}
