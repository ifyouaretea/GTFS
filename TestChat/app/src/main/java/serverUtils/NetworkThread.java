package serverUtils;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tes on 30/03/2015.
 */
public class NetworkThread extends Thread{
    private final String hostname = "128.199.73.51";
    private final int hostport = 8091;

    private String outMessage;
    private MessageBundle authMessage = null;
    private Socket client;

    private BlockingQueue<Map> messageQueue = new ArrayBlockingQueue<Map>(1000);

    public NetworkThread(String outMessage){
        this.outMessage = outMessage;
    }
    @Override
    public void run() {
        //while (true){
            try {
                client = new Socket(hostname, hostport);
                Log.d("Socket creation", "Successful");

                final MessageBundle authBundle = new MessageBundle("82238071", "asdsd",
                        MessageBundle.messageType.AUTH);
                authBundle.putUsername("sy");

                final MessageBundle textBundle = new MessageBundle("82238071", "asdsd",
                        MessageBundle.messageType.TEXT);

                textBundle.putMessage("HI BRAH");
                textBundle.putToPhoneNumber("82238071");
                textBundle.putChatroomID("12345");

                addMessageToQueue(authBundle.getMessage());
                send();

                Map receivedMessage = receive();

                if (String.valueOf(receivedMessage.get(MessageBundle.STATUS)).equals(MessageBundle.VALID_STATUS)) {
                    Log.d("AUTH", "Successful");
                    addMessageToQueue(textBundle.getMessage());
                    send();
                    receivedMessage = receive();
                    if(receivedMessage.get(MessageBundle.MESSAGE) != null){
                    }
                    //break;
                }else{
                    Log.d("AUTH", "fail");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        //}
    }

    private boolean send(){
        Object message = messageQueue.poll();
        if (message == null)
            return false;
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

    private Map receive(){
        try {
            JsonReader jIn = new JsonReader(client.getInputStream(), true);
            Map mapInput = (Map) jIn.readObject();
            outMessage = mapInput.toString();
            Log.d("JSON in", outMessage);
            return mapInput;
        }catch (Exception e){
                e.printStackTrace();
                outMessage = e.getMessage();
            return null;
        }
    }


    public boolean addMessageToQueue(Map message){
        return messageQueue.offer(message);
    }
}
