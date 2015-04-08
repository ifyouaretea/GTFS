package messageManagement;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

import java.util.Map;

import serverUtils.MessageBundle;
import serverUtils.NetworkThread;

/**
 * Created by tes on 01/04/2015.
 */

@Deprecated
public class ManagerThread extends Thread{

    NetworkThread networkThread;
    MessageDbAdapter dbAdapter;
    TextView textView;

    public ManagerThread(NetworkThread networkThread, MessageDbAdapter dbAdapter,
                         TextView textView){
        this.networkThread = networkThread;
        this.dbAdapter = dbAdapter;
        this.textView = textView;
    }

    @Override
    public void run(){
        Map received;
        while(true) {
            received = null;
            while (received == null) {
                received = networkThread.getMessage();
            }
            handleMessage();
        }
    }
    public void handleMessage(){
        Map message = networkThread.getMessage();
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (messageType.equals(MessageBundle.messageType.TEXT.toString())){
            dbAdapter.storeMessage(message);
            Log.d("Database", "great success");
        }
        textView.setText(message.toString());
    }
}
