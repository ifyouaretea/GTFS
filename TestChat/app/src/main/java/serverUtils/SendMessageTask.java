package serverUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.net.Socket;

public class SendMessageTask extends AsyncTask<MessageBundle, Void, Boolean>{

    private final Socket mSocket;

    public SendMessageTask(Socket socket){
        mSocket = socket;
    }
    @Override
    protected Boolean doInBackground(MessageBundle[] params) {
        while(true){
            try{

                Log.d("Send client", "Successful");
                mSocket.setSoTimeout(10000); //attempts to send to server every 10 seconds

                JsonWriter serverOut = new JsonWriter(mSocket.getOutputStream());

                for(MessageBundle message : params) {
                    serverOut.write(message);
                    serverOut.flush();
                }
                break;
            }
            catch(IOException e){
                Log.d("Sending failed", e.getMessage());
                //e.printStackTrace();
            }
        }
        return true;
    }
}
