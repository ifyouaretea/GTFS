package serverUtils;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gfts.testchat.MyApplication;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by tes on 03/04/2015.
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
    private Executor executor = Executors.newFixedThreadPool(4);

    private class ListenerThread extends Thread {
        public void run() {
            while (true) {
                try {
                    if(!authenticated()) {
                        Log.d("Receiving authenticated", String.valueOf(authenticated()));
                        authenticate();
                    }
                    Map received = receive();
                    if (received == null)
                        sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public NetworkService(){
        super("GTFS.NetworkService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        listener = new ListenerThread();
        listener.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        final Intent receivedIntent = workIntent;
        executor.execute(new Runnable() {
            @Override
            public void run(){
                String jsonString = receivedIntent.getStringExtra(MESSAGE_KEY);
                if(jsonString==null)
                    return;
                Map message = JsonReader.jsonToMaps(jsonString);

                //loop until authenticated
                if(!authenticated()) {
                    Log.d("Sending authenticated", String.valueOf(authenticated()));
                    authenticate();
                }

                while(!send(message)){
                    Log.d("Sending", "retrying");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
           });
        }



    private boolean send(Map message){
        if (message == null)
            return false;

        try {
            if(client == null){
                client = new Socket(hostname, hostport);
            }
            JsonWriter serverOut = new JsonWriter(client.getOutputStream());
            serverOut.write(message);
            serverOut.flush();
            Log.d("Message sent out", message.toString());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Map receive(){
        try {
            if(client == null){
                client = new Socket(hostname, hostport);
            }
            JsonReader jIn = new JsonReader(client.getInputStream(), true);
            Map receivedMap = (Map) jIn.readObject();
            String messageType = (String) receivedMap.get(MessageBundle.TYPE);
            if(!MessageBundle.messageType.AUTH.toString().equals(messageType)) {
                Intent receivedMessageIntent = new Intent(MESSAGE_RECEIVED).putExtra
                        (MESSAGE_KEY, JsonWriter.objectToJson(receivedMap));

                LocalBroadcastManager.getInstance(getApplicationContext()).
                        sendBroadcast(receivedMessageIntent);
            }
            return receivedMap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void authenticate(){
        while(true) {
            try {
                if(client == null)
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
                if(!MessageBundle.messageType.AUTH.toString().
                        equals(receivedMessage.get(MessageBundle.TYPE)))
                    return;
                if (String.valueOf(receivedMessage.get(MessageBundle.STATUS)).
                        equals(MessageBundle.VALID_STATUS)) {

                    ((MyApplication)getApplication()).setAuthenticated(true);
                    break;
                }else{

                    ((MyApplication)getApplication()).setAuthenticated(false);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean authenticated(){
        return ((MyApplication) getApplication()).isAuthenticated();
    }
}
