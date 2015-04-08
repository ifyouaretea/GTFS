package cse.sutd.gtfs.serverUtils;

import android.app.IntentService;
import android.app.NotificationManager;
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

/**
 * Created by Glen on 03/04/2015.
 */
public class NetworkService extends IntentService {

    public static final String SEND_MESSAGE = "com.gtfs.SEND_MESSAGE";
    public static final String MESSAGE_RECEIVED = "com.gtfs.MESSAGE_RECEIVED";
    public static final String MESSAGE_KEY = "message";

    private final String hostname = "128.199.73.51";
    private final int hostport = 8091;

    private MessageBundle authMessage;
    private Socket client;
    private ListenerThread listener;

    private class ListenerThread extends Thread {

        private ListenerThread singleton;

        public void run() {
            while (true) {
                try {
                    if (!authenticated()) {
                        Log.d("Receiving authenticated", String.valueOf(authenticated()));
                        authenticate();
                    }
                    Map received = receive();
                    Log.d("Listener received", received.toString());
                    if (received == null)
                        sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public NetworkService() {
        super("GTFS.NetworkService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!((GTFSClient) getApplication()).isListening()) {
            listener = new ListenerThread();
            listener.start();
            ((GTFSClient) getApplication()).setListening(true);
        }
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        final Intent receivedIntent = workIntent;

        String jsonString = receivedIntent.getStringExtra(MESSAGE_KEY);
        if (jsonString == null)
            return;
        Map message = JsonReader.jsonToMaps(jsonString);

        //loop until authenticated
        if (!authenticated()) {
            Log.d("Sending authenticated", String.valueOf(authenticated()));
            authenticate();
        }

        while (!send(message)) {
            Log.d("Sending", "retrying");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }


    private boolean send(Map message) {
        if (message == null)
            return false;

        try {
            if (client == null) {
                client = new Socket(hostname, hostport);
                ((GTFSClient) getApplication()).setAuthenticated(false);
                authenticate();
            }
            JsonWriter serverOut = new JsonWriter(client.getOutputStream());
            serverOut.write(message);
            serverOut.flush();
            Log.d("Message sent out", message.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Map receive() {
        try {
            if (client == null) {
                client = new Socket(hostname, hostport);
                ((GTFSClient) getApplication()).setAuthenticated(false);
                authenticate();
            }
            JsonReader jIn = new JsonReader(client.getInputStream(), true);
            Map receivedMap = (Map) jIn.readObject();
            String messageType = (String) receivedMap.get(MessageBundle.TYPE);

            if (!MessageBundle.messageType.AUTH.toString().equals(messageType)) {
                Intent receivedMessageIntent = new Intent(MESSAGE_RECEIVED).putExtra
                        (MESSAGE_KEY, JsonWriter.objectToJson(receivedMap));

                LocalBroadcastManager.getInstance(getApplicationContext()).
                        sendBroadcast(receivedMessageIntent);
            }
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Message Received")
                            .setContentText(receivedMap.toString());
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0, mBuilder.build());

            return receivedMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void authenticate() {
        while (true) {
            try {
                if (client == null)
                    client = new Socket(hostname, hostport);
                if (client.isClosed() || !client.isConnected())
                    client = new Socket(hostname, hostport);

                //TODO: remove hardcoding
                final MessageBundle authBundle = new MessageBundle("82238071", "asdsd",
                        MessageBundle.messageType.AUTH);
                authBundle.putUsername("sy");
                send(authBundle.getMessage());
                Map receivedMessage = receive();
                Log.d("Authentication", receivedMessage.toString());
                if (!MessageBundle.messageType.AUTH.toString().
                        equals(receivedMessage.get(MessageBundle.TYPE)))
                    return;
                if (String.valueOf(receivedMessage.get(MessageBundle.STATUS)).
                        equals(MessageBundle.VALID_STATUS)) {

                    ((GTFSClient) getApplication()).setAuthenticated(true);
                    break;
                } else {

                    ((GTFSClient) getApplication()).setAuthenticated(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean authenticated() {
        return ((GTFSClient) getApplication()).isAuthenticated();
    }
}
