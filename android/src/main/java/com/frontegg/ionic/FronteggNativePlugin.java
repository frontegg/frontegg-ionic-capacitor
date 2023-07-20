package com.frontegg.ionic;


import com.frontegg.android.FronteggApp;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@CapacitorPlugin(name = "FronteggNative")
public class FronteggNativePlugin extends Plugin {


    @Override
    public void load() {
        Map<String, String> constants = this.getConstants();

        FronteggApp.Companion.init(
            Objects.requireNonNull(constants.get("baseUrl")),
            Objects.requireNonNull(constants.get("clientId")),
            this.getContext()
        );
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
