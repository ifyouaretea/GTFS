package cse.sutd.gtfs.messageManagement;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


import java.util.Map;

import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

/**
 * Created by tes on 01/04/2015.
 */
public class ManagerService extends Service{

    public static final String UPDATE_UI = "com.gtfs.UPDATE_UI";
    MessageDbAdapter dbAdapter;
    MessageBroadcastReceiver broadcastReceiver;


    private class MessageBroadcastReceiver extends BroadcastReceiver{
        private MessageBroadcastReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d("Broadcast receiver", "received intent!");
            Map received = (Map) JsonReader.jsonToJava(intent.getStringExtra
                    (NetworkService.MESSAGE_KEY));
            handleMessage(received);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter receivedIntentFilter = new IntentFilter(NetworkService.MESSAGE_RECEIVED);
        broadcastReceiver = new MessageBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        this.dbAdapter = ((GTFSClient) getApplication()).getDatabaseAdapter();
        Log.d("Manager service", "Broadcast receiver registered");
        return super.onStartCommand(intent, flags, startId);
    }

    public void handleMessage(Map message){
        Log.d("Handle message", message.toString());
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (MessageBundle.messageType.TEXT_RECEIVED.toString().equals(messageType)){
            dbAdapter.storeMessage(message);
            //TODO: fix possible synchronisation problems
            Log.d("DB message insertion", message.toString());

            Intent updateUIIntent = new Intent(UPDATE_UI);
            updateUIIntent.putExtra(NetworkService.MESSAGE_KEY,
                    JsonWriter.objectToJson(message));

            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(updateUIIntent);

        }else if(messageType.equals(MessageBundle.messageType.CREATE_ROOM.toString()) ||
                messageType.equals(MessageBundle.messageType.ROOM_INVITATION.toString())
                ){
            dbAdapter.createGroupChat(message);
            Intent updateUIIntent = new Intent(UPDATE_UI);
            updateUIIntent.putExtra(NetworkService.MESSAGE_KEY,
                    JsonWriter.objectToJson(message));

            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(updateUIIntent);

            Log.d("Database chat insertion", message.toString());
        }

        Log.d("Received Message", message.toString());
    }
}
