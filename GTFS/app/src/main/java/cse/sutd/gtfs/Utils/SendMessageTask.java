package cse.sutd.gtfs.Utils;

/**
 * Created by Francisco Furtado on 27/03/2015.
 */

import android.os.AsyncTask;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.net.Socket;

public class SendMessageTask extends AsyncTask<MessageBundle, Void, Boolean>{

    public static final String hostname = "128.199.73.51";
    public static final int hostport = 8091;
    private MessageBundle message;

    public SendMessageTask(MessageBundle message){
        this.message = message;
    }

    @Override
    protected Boolean doInBackground(MessageBundle[] params) {
        while(true){
            try{
                Socket server = new Socket(hostname, hostport);
                server.setSoTimeout(10000); //attempts to send to server every 10 seconds

                JsonWriter serverOut = new JsonWriter(server.getOutputStream());
                JsonReader serverIn = new JsonReader(server.getInputStream());

                serverOut.write(message);
                serverOut.flush();

                serverOut.close();
                serverIn.close();
                server.close();
                break;
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        return true;
    }
}