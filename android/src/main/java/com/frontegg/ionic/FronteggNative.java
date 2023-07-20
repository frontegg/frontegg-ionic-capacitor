package com.frontegg.ionic;

import android.util.Log;

import com.frontegg.android.FronteggApp;

public class FronteggNative {



    public String echo(String value) {

        Log.i("Echo", value);
        return value;
    }
}
