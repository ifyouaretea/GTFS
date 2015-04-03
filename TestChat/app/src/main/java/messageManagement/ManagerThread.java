package messageManagement;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import serverUtils.MessageBundle;
import serverUtils.NetworkThread;

/**
 * Created by tes on 01/04/2015.
 */
public class ManagerThread extends Thread{

    NetworkThread networkThread;

    public ManagerThread(NetworkThread networkThread){
        this.networkThread = networkThread;
    }
    public void handleMessage(){
        Map message = networkThread.getMessage();
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (messageType.equals(MessageBundle.messageType.TEXT.toString())){

        }
    }
}
