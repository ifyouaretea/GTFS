package cse.sutd.gtfs;

/**
 * Created by Francisco Furtado on 28/03/2015.
 */

import android.app.Application;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import cse.sutd.gtfs.Utils.MessageBundle;
import cse.sutd.gtfs.Utils.ReceiveListenerThread;
import cse.sutd.gtfs.Utils.SendMessageTask;
import io.fabric.sdk.android.Fabric;

public class GTFSClient extends Application{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "6Rs5gyo7xHoEYYkls0ajWP9PO";
    private static final String TWITTER_SECRET = "8nvcBPCoqhkt1Lvzjv6Pb5GmBB4uBmreV3KSgVxfcgCJrMQT8E";
    private static GTFSClient instance;
    private static SendMessageTask sender;
    private static ReceiveListenerThread listener;

    private String ownID;
    @Override
    public void onCreate(){
        super.onCreate();
        // Initialize the singletons so their instances are bound to the application process.
        initSingletons();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

    protected void initSingletons(){
        // Initialize the instance of MySingleton
        if (instance == null){
            // Create the instance
            instance = new GTFSClient();
        }
    }

    public static GTFSClient getInstance(){
        // Initialize the instance of MySingleton
        if (instance == null)
            instance = new GTFSClient();            // Create the instance

        return instance;
    }

    public String getID() {
        return ownID;
    }

    public void setID(String ID) {
        this.ownID = ID;
    }

    public void sendMessage(MessageBundle[] msg){

    }

    public void authenticate(MessageBundle[] msg){

    }
}