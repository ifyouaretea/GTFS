package serverUtils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

import serverUtils.MessageBundle.messageType;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class SendMessageTask extends AsyncTask<MessageBundle, Void, Boolean>{
	
//	public static final String hostname = "128.199.73.51";
    public static final String hostname = "localhost";

    public static final int hostport = 8091;

    @Override
    protected Boolean doInBackground(MessageBundle[] params) {
        while(true){
            try{
                Socket server = new Socket(hostname, hostport);
                Log.d("Send client", "Successful");
                server.setSoTimeout(10000); //attempts to send to server every 10 seconds

                JsonWriter serverOut = new JsonWriter(server.getOutputStream());

                for(MessageBundle message : params) {
                    serverOut.write(message);
                    serverOut.flush();
                }
                serverOut.close();
                server.close();
                break;
            }
            catch(IOException e){
                //e.printStackTrace();
            }
        }
        return true;
    }
}
