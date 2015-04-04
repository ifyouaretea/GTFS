package com.example.haductien.chatapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Message;
import android.preference.PreferenceManager;

import com.digits.sdk.android.Digits;
import com.example.haductien.chatapp.GCM.Constants;
import com.example.haductien.chatapp.serverUtils.MessageBundle;
import com.example.haductien.chatapp.serverUtils.ReceiveListenerTask;
import com.example.haductien.chatapp.serverUtils.SendMessageTask;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import org.apache.http.auth.AUTH;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;
/**
 * Created by Francisco Furtado on 04/03/2015.
 */

public class MyApplication extends Application {

//    private static MyApplication singleton;
//    private static SharedPreferences prefs;
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private static final String TWITTER_KEY = "lgj3wZm2PTAYugiOWZddMS1Rv";
//    private static final String TWITTER_SECRET = "JwWBe8KKddQdd451UE7SXpqsVdPSPdFnfjYHTJnVTgKXHGfxer";

    public static final String PROFILE_ID = "profile_id";

    public static final String FROM = "82238071";
    public static final String REG_ID = "regId";
    public static final String MSG = "msg";
    public static final String TO = "chatId2";
    public static final String SESSION_TOKEN = "asdfadsf";


    private static MyApplication singleton;
    public static String[] email_arr;
    private static SharedPreferences prefs;

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

    public static String getServerUrl() {
        return prefs.getString("server_url_pref", Constants.SERVER_URL);
    }

    public static String getSenderId() {
        return prefs.getString("sender_id_pref", Constants.SENDER_ID);
    }

    /**
     * Starts a SendMessageTask to send a text message
     */

    public static void sendTextMessage(String text){
        MessageBundle outMessage = new MessageBundle(FROM, text, MessageBundle.messageType.TEXT);
        MessageBundle authMessage = new MessageBundle(FROM, text, MessageBundle.messageType.AUTH);
        SendMessageTask sender = new SendMessageTask();
        ReceiveListenerTask receiver = new ReceiveListenerTask();
        sender.execute(authMessage);
        receiver.execute();

    }
}




