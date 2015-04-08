package messageManagement;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.cedarsoftware.util.io.JsonReader;
import com.gfts.testchat.MyApplication;

import java.util.Map;

import serverUtils.MessageBundle;
import serverUtils.NetworkService;
import serverUtils.NetworkThread;

/**
 * Created by tes on 01/04/2015.
 */
public class ManagerService extends Service{

    MessageDbAdapter dbAdapter;
    MessageBroadcastReceiver broadcastReceiver;

    private class MessageBroadcastReceiver extends BroadcastReceiver{
        private MessageBroadcastReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d("Receiver", "received intent!");
            Map received = (Map) JsonReader.jsonToJava(intent.getStringExtra
                    (NetworkService.MESSAGE_KEY));
            handleMessage(received);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter receivedIntentFilter = new IntentFilter(NetworkService.MESSAGE_RECEIVED);
        broadcastReceiver = new MessageBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        this.dbAdapter = ((MyApplication) getApplication()).getDatabaseAdapter();
        return super.onStartCommand(intent, flags, startId);
    }

    public void handleMessage(Map message){
        Log.d("Handle message", "I'm handling a message!");
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (MessageBundle.messageType.TEXT.toString().equals(messageType)){
            dbAdapter.storeMessage(message);
            Log.d("Database message insertion", message.toString());
        }else if(messageType.equals(MessageBundle.messageType.CREATE_ROOM.toString()) ||
                messageType.equals(MessageBundle.messageType.INVITATION.toString())
                ){
            dbAdapter.createGroupChat(message);
            Log.d("Database chat insertion", message.toString());
        }

        Log.d("Received Message", message.toString());
    }
}
