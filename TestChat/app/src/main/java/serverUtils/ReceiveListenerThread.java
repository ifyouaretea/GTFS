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
public class ReceiveListenerThread extends Thread{

    public final Socket mSocket;
//	public static MessageBundle RECEIVED = new MessageBundle(ownID, "", messageType.CLIENT_RECEIVED);

    public ReceiveListenerThread(Socket socket){
        this.mSocket = socket;
    }
	public void run(){
        try {
            Log.d("Receive connection", "Successful");

            JsonReader jIn = new JsonReader(mSocket.getInputStream(), true);

            StringBuilder buffer = new StringBuilder();
            InputStream serverStream = mSocket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(serverStream));

            byte[] byteBuffer = new byte[10000];

            serverStream.read(byteBuffer);
            Log.d("ByteBuffer", Arrays.toString(byteBuffer));

//            Map map = (Map) jIn.readObject();
//            Log.d("JSON in", map.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
	}
}

