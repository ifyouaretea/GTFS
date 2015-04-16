package cse.sutd.gtfs.serverUtils;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cedarsoftware.util.io.JsonIoException;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import cse.sutd.gtfs.GTFSClient;


/**
 * NetworkService that handles all of the network communication with the server. NetworkService
 * extends Android's IntentService to take advantage of IntentService's work queue capabilities
 * in handling incoming intents.
 */
public class NetworkService extends IntentService {

    public static final String MESSAGE_RECEIVED = "com.gtfs.MESSAGE_RECEIVED";
    public static final String MESSAGE_KEY = "message"; //key to get the message extra from the intents
    public static final int SLEEP_TIME = 1000;
    private final String hostname = "128.199.73.51";
    private final int hostport = 8091;

    /**
     * ListenerThread continuously attempts to receive messages the server. Only one ListenerThread
     * should be active at one time
     */
    private class ListenerThread extends Thread {

        public void run() {
            while (true) {
                try {
                    if(!authenticated()) {
                        Log.d("Receiving authenticated", String.valueOf(authenticated()));
                        authenticate();
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
    public NetworkService(){
        super("GTFS.NetworkService");
    }

    /**
     * Called when the service is started. Used to start the ListenerThread if not already listening
     * @param intent
     * @param flags
     * @param startId
     * @return START_STICKY flag
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(!((GTFSClient)getApplication()).isListening()) {
            ListenerThread listener = new ListenerThread();
            listener.start();
            ((GTFSClient)getApplication()).setListening(true);
        }

        return START_STICKY;
    }

    /**
     * Called when an incoming intent is received. The incoming intent must contain a String extra
     * containing the Json string to be sent (mapped by the key value MESSAGE_KEY).
     * @param workIntent The incoming intent
     */
    @Override
    protected void onHandleIntent(Intent workIntent){
        final Intent receivedIntent = workIntent;

        String jsonString = receivedIntent.getStringExtra(MESSAGE_KEY);
        if(jsonString==null)
            return;
        Map message = JsonReader.jsonToMaps(jsonString);

        //loop until authenticated
        if(!authenticated()) {
            Log.d("Sending authenticated", String.valueOf(authenticated()));
            authenticate();
        }

        while(!send(message)) {
            Log.d("Sending", "retrying");
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Attempts to send the message specified in the parameters.
     * @param message Message to be sent
     * @return Whether sending is successful
     */
    private boolean send(Map message){
        if (message == null)
            return false;

        try {
            if(((GTFSClient)getApplication()).getClient() == null){
                ((GTFSClient)getApplication()).setClient(new Socket(hostname, hostport));
                ((GTFSClient) getApplication()).setAuthenticated(false);
                authenticate();
            }

            JsonWriter serverOut = new JsonWriter(((GTFSClient)getApplication()).
                    getClient().getOutputStream());
            serverOut.write(message);
            serverOut.flush();
            Thread.sleep(10);
            Log.d("Message sent out", message.toString());

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *Attempts to receive a single Json message from the server. If it is unable to read from the
     * socket, it attempts again with a new socket. A received message that has to be handled
     * outside of the service is broadcast to be received by ManagerService
     * @return The received Json object, null if receiving was unsuccessful
     */
    private Map receive(){
        try {
            if (((GTFSClient) getApplication()).getClient() == null) {
                ((GTFSClient) getApplication()).setClient(new Socket(hostname, hostport));
                ((GTFSClient) getApplication()).setAuthenticated(false);
                authenticate();
            }else if(((GTFSClient) getApplication()).getClient().isClosed() ||
                    !(((GTFSClient) getApplication()).getClient().isConnected())){
                ((GTFSClient) getApplication()).setClient(new Socket(hostname, hostport));
                ((GTFSClient) getApplication()).setAuthenticated(false);
                authenticate();
            }
            InputStream in = ((GTFSClient) getApplication()).getClient()
                    .getInputStream();

            JsonReader jIn = new JsonReader(in, true);

            Map receivedMap = (Map) jIn.readObject();
            String messageType = (String) receivedMap.get(MessageBundle.TYPE);

            if (!MessageBundle.messageType.AUTH.toString().equals(messageType) &&
                    messageType != null){

                Intent receivedMessageIntent = new Intent(MESSAGE_RECEIVED).putExtra
                        (MESSAGE_KEY, JsonWriter.objectToJson(receivedMap));

                LocalBroadcastManager.getInstance(getApplicationContext()).
                        sendBroadcast(receivedMessageIntent);
            }
            return receivedMap;
        } catch(JsonIoException jException){
            try {
                ((GTFSClient) getApplication()).setClient(new Socket(hostname, hostport));
                ((GTFSClient) getApplication()).setAuthenticated(false);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Attempts to authenticate the client's identity with the server
     */
    private void authenticate(){
        while(true) {
            try {
                if(((GTFSClient)getApplication()).getClient() == null)
                    ((GTFSClient)getApplication()).setClient(new Socket(hostname, hostport));
                if (((GTFSClient)getApplication()).getClient().isClosed() ||
                        !((GTFSClient)getApplication()).getClient().isConnected())
                    ((GTFSClient)getApplication()).setClient(new Socket(hostname, hostport));

                //TODO: remove hardcoding

                String userID = ((GTFSClient)getApplication()).getID();
                while(userID == null){
                    Thread.sleep(SLEEP_TIME);
                    userID = ((GTFSClient)getApplication()).getID();
                }

                final MessageBundle authBundle = new MessageBundle(userID,
                        ((GTFSClient)getApplicationContext()).getSESSION_ID(),
                        MessageBundle.messageType.AUTH);
                authBundle.putUsername(((GTFSClient)getApplicationContext()).getNAME());
                send(authBundle.getMessage());
                Map receivedMessage = receive();
                Log.d("Authentication", receivedMessage.toString());

                if(!MessageBundle.messageType.AUTH.toString().
                        equals(receivedMessage.get(MessageBundle.TYPE)))
                    return;
                if (String.valueOf(receivedMessage.get(MessageBundle.STATUS)).
                        equals(MessageBundle.VALID_STATUS)){
                    ((GTFSClient)getApplication()).setAuthenticated(true);
                    break;
                }else{
                    ((GTFSClient)getApplication()).setAuthenticated(false);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Convenience method to get authentication status
     * @return
     */
    private boolean authenticated(){
        return ((GTFSClient) getApplication()).isAuthenticated();
    }
}
