package com.example.haductien.chatapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Patterns;

import com.digits.sdk.android.Digits;
<<<<<<< HEAD
=======
import com.example.haductien.chatapp.GCM.Constants;
import com.example.haductien.chatapp.serverUtils.MessageBundle;
import com.example.haductien.chatapp.serverUtils.ReceiveListenerTask;
import com.example.haductien.chatapp.serverUtils.SendMessageTask;
>>>>>>> 685f20aa3be692fea9b7188ed4655939e01531c5
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

    private static MyApplication singleton;
    private static SharedPreferences prefs;

    private static String number = "def";
    public static final String PROFILE_ID = "profile_id";

    public static final String FROM = "82238071";
    public static final String REG_ID = "regId";
    public static final String MSG = "msg";
    public static final String TO = "chatId2";
<<<<<<< HEAD
=======
    public static final String SESSION_TOKEN = "asdfadsf";


    private static MyApplication singleton;
    public static String[] email_arr;
    private static SharedPreferences prefs;

>>>>>>> 685f20aa3be692fea9b7188ed4655939e01531c5
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    private MyApplication(){

    }
    // Returns the application instance
    public static MyApplication getInstance() {
        if(singleton ==null)
            singleton = new MyApplication();
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // twitter
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

    private List<String> getEmailList() {
        List<String> lst = new ArrayList<String>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                lst.add(account.name);
            }
        }
        return lst;
    }

    public static String getChatId() {
        return prefs.getString("chat_id", "");
    }
    public static void setChatId(String chatId) {
        prefs.edit().putString("chat_id", chatId).commit();
    }

    public static String getNumber() {
        return prefs.getString("number", "");
    }
    public static void setNumber(String number) {
        prefs.edit().putString("number", number).commit();
    }

    public static String getCurrentChat() {
        return prefs.getString("current_chat", null);
    }
    public static void setCurrentChat(String chatId) {
        prefs.edit().putString("current_chat", chatId).commit();
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
<<<<<<< HEAD
=======

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
>>>>>>> 685f20aa3be692fea9b7188ed4655939e01531c5
}




