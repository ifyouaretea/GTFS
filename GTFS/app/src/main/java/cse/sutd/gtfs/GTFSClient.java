package cse.sutd.gtfs;

/**
 * Created by Francisco Furtado on 28/03/2015.
 */

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;

import com.matesnetwork.callverification.Cognalys;

import java.sql.SQLException;

import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.NetworkService;

public class GTFSClient extends Application{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private final String TWITTER_KEY = "6Rs5gyo7xHoEYYkls0ajWP9PO";
    private final String TWITTER_SECRET = "8nvcBPCoqhkt1Lvzjv6Pb5GmBB4uBmreV3KSgVxfcgCJrMQT8E";
    public static final String PREFS_NAME = "MyPrefsFile";
    private static GTFSClient instance;

    private static GTFSClient singleton;

    private MessageDbAdapter messageDbAdapter;
    private boolean authenticated = false;
    private boolean listening = false;

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
        messageDbAdapter = MessageDbAdapter.getInstance(this);
        try {
            messageDbAdapter.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
        singleton = this;
        startService(new Intent(this, ManagerService.class));
        startService(new Intent(this, NetworkService.class));
        initSingletons();
//        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
//        Fabric.with(this, new TwitterCore(authConfig), new Digits());
        Cognalys.enableAnalytics(getApplicationContext(), true, true);
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
    private String PROFILE_ID;


    protected void initSingletons(){
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
}