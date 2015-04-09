/*
package cse.sutd.gtfs.serverUtils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.net.Socket;
import java.util.Map;

import cse.sutd.gtfs.GTFSClient;

*/
/**
 * Created by tes on 09/04/2015.
 *//*

public class SenderService extends NetworkService {

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
            Log.d("Message sent out", message.toString());

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
*/
