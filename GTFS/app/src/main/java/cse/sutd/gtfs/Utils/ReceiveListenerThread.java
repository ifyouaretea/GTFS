package cse.sutd.gtfs.Utils;

/**
 * Created by Francisco Furtado on 07/04/2015.
 */
import android.util.Log;

import com.cedarsoftware.util.io.JsonReader;

import java.io.InputStream;
import java.util.Map;

/**
 * Listens continuously for a connection from the server.
 *
 */
public class ReceiveListenerThread extends Thread{

    public final InputStream mInputStream;

    public ReceiveListenerThread(InputStream inputStream){
        mInputStream = inputStream;
    }
    public void run(){
        while(true) {
            try {
                Log.d("Receiver", "Successful");
                JsonReader jIn = new JsonReader(mInputStream, true);

                Map map = (Map) jIn.readObject();
                Log.d("JSON in", map.toString());
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


