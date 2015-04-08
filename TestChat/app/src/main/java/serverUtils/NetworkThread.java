package serverUtils;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonIoException;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Created by tes on 30/03/2015.
 */
@Deprecated
public class NetworkThread extends Thread{
    private final String hostname = "128.199.73.51";
    private final int hostport = 8091;
//    private final String userID;
//    private final String sessionToken;


    private MessageBundle authMessage = null;
    private Socket client;
    private boolean authenticated = false;

    private BlockingQueue<Map> inMessageQueue = new ArrayBlockingQueue<Map>(1000);
    private BlockingQueue<Map> outMessageQueue = new ArrayBlockingQueue<Map>(1000);
    private Semaphore availableMessages = new Semaphore(0);

    @Override
    public void run() {
        while (true) {
            try {
                client = new Socket(hostname, hostport);
                client.setSoTimeout(1000);
                Log.d("Socket creation", "Successful");

                //loop until authenticated
                while (!authenticate()) {
                    sleep(1000);
                }

                Log.d("AUTH", "Successful");

                boolean sleep = false;
                while (true) {
                    sleep = false;
                    if (inMessageQueue.size() > 0){
                        send();
                        sleep = true;
                    }
                    Map received = receive();
                    if (received != null) {
                        Log.d("Received", received.toString());
                        sleep = true;
                    }
                    if(!sleep)
                        sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean send(){
        Object message = inMessageQueue.poll();
        if (message == null)
            return false;
        Log.d("Message sent out", message.toString());
        try {
            JsonWriter serverOut = new JsonWriter(client.getOutputStream());
            serverOut.write(message);
            serverOut.flush();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //TODO: fix all calls to receive() to refer to outMessageQueue
    private Map receive(){
        try {
            JsonReader jIn = new JsonReader(client.getInputStream(), true);
            Map receivedMap = (Map) jIn.readObject();
            outMessageQueue.offer(receivedMap);
            Log.d("Semaphore", "Releasing semaphore");
            availableMessages.release();
            return receivedMap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public boolean addMessageToQueue(Map message){
        return inMessageQueue.offer(message);
    }

    public Map getMessage(){
        try {
            Log.d("Semaphore", "Acquiring semaphore");
            availableMessages.acquire();
            Log.d("Semaphore", "Acquired");
        }catch (InterruptedException e){}
        return outMessageQueue.poll();
    }
    private boolean authenticate(){
        //TODO: remove hardcoding
        final MessageBundle authBundle = new MessageBundle("82238071", "asdsd",
                MessageBundle.messageType.AUTH);
        authBundle.putUsername("sy");
        addMessageToQueue(authBundle.getMessage());
        send();

        Map receivedMessage = receive();
        outMessageQueue.poll();
        if (String.valueOf(receivedMessage.get(MessageBundle.STATUS)).
                equals(MessageBundle.VALID_STATUS)) {
            this.authenticated = true;
            return true;
        }else{
            this.authenticated = false;
        }
        return false;
    }
}
