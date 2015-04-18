package cse.sutd.gtfs;

/**
 * Created by Francisco Furtado on 28/03/2015.
 */

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;

import com.matesnetwork.callverification.Cognalys;

import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.NetworkService;

public class GTFSClient extends Application{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private final String TWITTER_KEY = "6Rs5gyo7xHoEYYkls0ajWP9PO";
    private final String TWITTER_SECRET = "8nvcBPCoqhkt1Lvzjv6Pb5GmBB4uBmreV3KSgVxfcgCJrMQT8E";
    public final String PREFS_NAME = "MyPrefsFile";

    private String PROFILE_ID;
    private String PROFILE_NAME;
    private String SESSION_ID;

    private Socket client;
    private MessageDbAdapter messageDbAdapter;
    private static GTFSClient instance;
    private Map<String, ArrayList<String>> notificationMap;
    private boolean authenticated = false;
    private boolean listening = false;

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }
    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getInstance();
        messageDbAdapter = MessageDbAdapter.getInstance(this);
        Cognalys.enableAnalytics(getApplicationContext(), true, true);
        try {
            messageDbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        notificationMap = new HashMap<>();

//        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new TwitterCore(authConfig), new Digits());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public MessageDbAdapter getDatabaseAdapter(){
        return messageDbAdapter;
    }

    protected void getInstance(){
        // Initialize the instance of MySingleton
        if (instance == null){
            // Create the instance
            instance = new GTFSClient();
        }
    }

    public String getID() {
        return PROFILE_ID;
    }

    public void setID(String ID) {
        this.PROFILE_ID = ID;
    }

    public String getNAME() {return PROFILE_NAME; }

    public void setNAME(String PROFILE_NAME) { this.PROFILE_NAME = PROFILE_NAME; }

    public String getSESSION_ID() { return SESSION_ID; }

    public void setSESSION_ID(String SESSION_ID) { this.SESSION_ID = SESSION_ID; }


    public Map<String, ArrayList<String>> getNotificationMap() {
        return notificationMap;
    }

    public void resetNotificationMap() {
        this.notificationMap = new HashMap<String, ArrayList<String>>();
    }
}