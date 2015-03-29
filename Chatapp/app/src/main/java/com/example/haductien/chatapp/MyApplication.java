package com.example.haductien.chatapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

/**
 * Created by Francisco Furtado on 04/03/2015.
 */

public class MyApplication extends Application {

    private static MyApplication singleton;
    private static SharedPreferences prefs;
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "lgj3wZm2PTAYugiOWZddMS1Rv";
//    private static final String TWITTER_SECRET = "JwWBe8KKddQdd451UE7SXpqsVdPSPdFnfjYHTJnVTgKXHGfxer";

    public static final String PROFILE_ID = "profile_id";

    public static final String FROM = "chatId";
    public static final String REG_ID = "regId";
    public static final String MSG = "msg";
    public static final String TO = "chatId2";
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    private MyApplication(){
//        ReceiveListenerThread listener = new ReceiveListenerThread();
//        listener.start();
    }
    // Returns the application instance
    public static MyApplication getInstance() {
        if(singleton == null)
            singleton = new MyApplication();
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // twitter
//        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new Twitter(authConfig));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static String getChatId() {
        return prefs.getString("chat_id", "");
    }
    public static void setChatId(String chatId) {
        prefs.edit().putString("chat_id", chatId).apply();
    }

    public static String getNumber() {
        return prefs.getString("number", "");
    }
    public static void setNumber(String number) {
        prefs.edit().putString("number", number).apply();
    }

    public static String getCurrentChat() {
        return prefs.getString("current_chat", null);
    }
    public static void setCurrentChat(String chatId) {
        prefs.edit().putString("current_chat", chatId).apply();
    }

    public static boolean contains(String key){
        return prefs.contains(key);
    }

    public static boolean isNotify() {
        return prefs.getBoolean("notifications_new_message", true);
    }

    public static String getRingtone() {
        return prefs.getString("notifications_new_message_ringtone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }
}




