package serverUtils;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Listens continuously for a connection from the server.
 *
 */
@Deprecated
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

