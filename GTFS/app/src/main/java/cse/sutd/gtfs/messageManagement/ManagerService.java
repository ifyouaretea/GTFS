package cse.sutd.gtfs.messageManagement;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


import java.util.ArrayList;
import java.util.Map;

import cse.sutd.gtfs.Activities.Messaging.MainActivity;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

/**
 * Created by tes on 01/04/2015.
 */
public class ManagerService extends Service{

    public static final String UPDATE_UI = "com.gtfs.UPDATE_UI";

    private String userID;
    MessageDbAdapter dbAdapter;
    MessageBroadcastReceiver broadcastReceiver;


    private class MessageBroadcastReceiver extends BroadcastReceiver{
        private MessageBroadcastReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent){
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
        userID = ((GTFSClient)getApplication()).getID();
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
            if(userID == null)
                userID = ((GTFSClient)getApplication()).getID();
            if(!message.get(MessageBundle.FROM_PHONE_NUMBER).equals(userID)) {
                addToNotification(message);
            }
        }else if(
                messageType.equals(MessageBundle.messageType.ROOM_INVITATION.toString()) )
            dbAdapter.createGroupChat(message);

        else if(messageType.equals(MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString()))
            dbAdapter.createSingleChat(message);

        else if(messageType.equals(MessageBundle.messageType.GET_ROOMS.toString()))
            dbAdapter.importChatrooms(message);

        else if (messageType.equals(MessageBundle.messageType.GET_USERS.toString()))
            dbAdapter.importUsers(message);

        else if (messageType.equals(MessageBundle.messageType.GET_NOTES.toString()))
            dbAdapter.importNotes(message);

        Intent updateUIIntent = new Intent(UPDATE_UI);
        updateUIIntent.putExtra(NetworkService.MESSAGE_KEY,
                JsonWriter.objectToJson(message));

        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(updateUIIntent);
    }

    private void addToNotification(Map message){
        Map<String, ArrayList<String>> notificationMap =
                ((GTFSClient) getApplication()).getNotificationMap();

        String chatroomID = (String) message.get(MessageBundle.CHATROOMID);

        if(notificationMap.get(chatroomID) == null)
            notificationMap.put(chatroomID,
                    new ArrayList<String>());

        notificationMap.get(chatroomID).add((String)message.get(MessageBundle.MESSAGE));

        String title, body;
        Intent openMessagingIntent;

        if(notificationMap.keySet().size() == 1) {
            title = dbAdapter.getChatroomName(chatroomID);
            if(title == null)
                Log.d("Null title", chatroomID);
            StringBuilder bodyBuilder = new StringBuilder();
            for (String s: notificationMap.get(chatroomID))
                bodyBuilder.append(s + "\n");
            body = bodyBuilder.toString();

            openMessagingIntent = new Intent(getApplicationContext(), MessagingActivity.class);
            openMessagingIntent.putExtra("ID", chatroomID);
            openMessagingIntent.putExtra(MessageBundle.TO_PHONE_NUMBER,
                    (String) message.get(MessageBundle.FROM_PHONE_NUMBER));
            openMessagingIntent.putExtra(MessageDbAdapter.ISGROUP,
                    dbAdapter.isGroup(chatroomID) ? 1 : 0);
        }
        else{
            title = String.format("Messages from %d chats", notificationMap.keySet().size());
            StringBuilder bodyBuilder = new StringBuilder();
            for(String s1: notificationMap.keySet())
                for(String s2 : notificationMap.get(s1))
                    bodyBuilder.append(s2 + "\n");
            body = bodyBuilder.toString();
            openMessagingIntent = new Intent(getApplicationContext(), MainActivity.class);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0,
                                openMessagingIntent, PendingIntent.FLAG_ONE_SHOT))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(body));

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Notification map", notificationMap.toString());
        mNotificationManager.notify(0, mBuilder.build());
    }
}
