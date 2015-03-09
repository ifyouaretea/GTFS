package com.example.haductien.chatapp;

import android.app.Application;
import android.content.res.Configuration;

import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Francisco Furtado on 04/03/2015.
 */

public class MyApplication extends Application {

    private static MyApplication singleton;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public MyApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig("lgj3wZm2PTAYugiOWZddMS1Rv", "JwWBe8KKddQdd451UE7SXpqsVdPSPdFnfjYHTJnVTgKXHGfxer");
        Fabric.with(this, new TwitterCore(authConfig), new Digits());
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
