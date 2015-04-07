package cse.sutd.gtfs;

/**
 * Created by Francisco Furtado on 28/03/2015.
 */

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.digits.sdk.android.Digits;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.File;

import cse.sutd.gtfs.Utils.MessageBundle;
import io.fabric.sdk.android.Fabric;

public class GTFSClient extends Application{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private final String TWITTER_KEY = "6Rs5gyo7xHoEYYkls0ajWP9PO";
    private final String TWITTER_SECRET = "8nvcBPCoqhkt1Lvzjv6Pb5GmBB4uBmreV3KSgVxfcgCJrMQT8E";
    public static final String PREFS_NAME = "MyPrefsFile";
    private GTFSClient instance;
//    private SendMessageTask sender;
//    private ReceiveListenerTask listener;
    private boolean isAuthenticated;

    private String PROFILE_ID;

    public GTFSClient() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onCreate(){
        super.onCreate();
        // Initialize the singletons so their instances are bound to the application process.
        initSingletons();
        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
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

    protected void initSingletons(){
        // Initialize the instance of MySingleton
        if (instance == null){
            // Create the instance
            instance = new GTFSClient();
        }
    }

    public GTFSClient getInstance(){
        // Initialize the instance of MySingleton
        if (instance == null)
            instance = new GTFSClient();            // Create the instance

        return instance;
    }

    public String getID() {
        return PROFILE_ID;
    }

    public void setID(String ID) {
        this.PROFILE_ID = ID;
    }

    public boolean getLoggedIn() {
        return isAuthenticated;
    }

    public void setLoggedIn(boolean log) {
        this.isAuthenticated = log;
    }

    public void sendMessage(MessageBundle[] msg){

    }

    public void authenticate(MessageBundle[] msg){

    }

    public void clearAppData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}