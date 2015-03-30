package serverUtils;

import android.os.Message;
import android.util.Log;

import com.cedarsoftware.util.io.JsonReader;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 * Created by tes on 30/03/2015.
 */
public class NetworkThread extends Thread{
    private final String hostname = "128.199.73.51";
    private final int hostport = 8091;

    @Override
        public void run(){
            try {
                Socket client = new Socket(hostname, hostport);
                Log.d("Socket creation", "Successful");
                final MessageBundle messageBundle = new MessageBundle("12345", "HI",
                        MessageBundle.messageType.AUTH);

                new SendMessageTask(client).execute(messageBundle);
                ReceiveListenerThread receiver = new ReceiveListenerThread(client);
                
                receiver.start();
                receiver.join();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
}
