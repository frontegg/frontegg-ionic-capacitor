package com.frontegg.ionic;

import com.frontegg.android.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.frontegg.android.regions.RegionConfig;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class Parser {


    public static JSONObject userToJSONObject(User user) {
        String jsonStr = new Gson().toJson(user, User.class);
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            return null;
        }
    }


    public static JSONObject regionToJSONObject(RegionConfig regionConfig) {
        String jsonStr = new Gson().toJson(regionConfig, RegionConfig.class);
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            return null;
        }
    }
    public static JSONArray regionsToJSONObject(List<RegionConfig> regions) {

        JSONArray regionsData = new JSONArray();
        for (RegionConfig regionConfig : regions) {
            JSONObject region = regionToJSONObject(regionConfig);
            if(region != null) {
                regionsData.put(region);
            }
        }
        return regionsData;
    }

}
