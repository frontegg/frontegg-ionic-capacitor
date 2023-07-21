package com.frontegg.ionic;

import com.frontegg.android.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

public class Parser {


    public static JSONObject userToJSONObject(User user) {
        String jsonStr = new Gson().toJson(user, User.class);
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            return null;
        }
    }

}
