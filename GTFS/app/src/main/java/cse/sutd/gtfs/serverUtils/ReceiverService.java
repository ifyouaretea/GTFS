/*
package cse.sutd.gtfs.serverUtils;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.net.Socket;
import java.util.Map;

import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;

*/
/**
 * Created by tes on 09/04/2015.
 *//*

public class ReceiverService  extends NetworkService {

    private ListenerThread listener;

    private class ListenerThread extends Thread {

        private ListenerThread singleton;

        public void run() {
            if(((GTFSClient)getApplication()).getID() == null)
                stopSelf();
            while (true) {
                try {
                    if(!ReceiverService.super.authenticated()) {
                        Log.d("Receiving authenticated", String.valueOf(authenticated()));
                        ReceiverService.super.authenticate();
                    }
                    Map received = receive();
                    Log.d("Listener received", received.toString());
                    if (received == null)
                        sleep(SLEEP_TIME);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        sleep(SLEEP_TIME);
                    }catch (InterruptedException e1){}
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (!((GTFSClient) getApplication()).isListening()) {
            listener = new ListenerThread();
            listener.start();
            ((GTFSClient) getApplication()).setListening(true);
        }
    }

    private Map receive(){
        try {
            if (((GTFSClient) getApplication()).getClient() == null) {
                ((GTFSClient) getApplication()).setClient(new Socket(NetworkService.hostname,
                        NetworkService.hostport));
                ((GTFSClient) getApplication()).setAuthenticated(false);
                super.authenticate();
            }
            JsonReader jIn = new JsonReader(((GTFSClient) getApplication()).getClient().getInputStream(), true);
            Map receivedMap = (Map) jIn.readObject();
            String messageType = (String) receivedMap.get(MessageBundle.TYPE);

            if (!MessageBundle.messageType.AUTH.toString().equals(messageType) &&
                    messageType != null){

                Intent receivedMessageIntent = new Intent(MESSAGE_RECEIVED).putExtra
                        (MESSAGE_KEY, JsonWriter.objectToJson(receivedMap));

                LocalBroadcastManager.getInstance(getApplicationContext()).
                        sendBroadcast(receivedMessageIntent);
            }
            //TODO: Remove universal notifications
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Message Received")
                            .setContentText(receivedMap.toString())
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(receivedMap.toString()));

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0, mBuilder.build());

            return receivedMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
*/
