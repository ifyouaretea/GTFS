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
            Map received = (Map) JsonReader.jsonToJava(intent.getStringExtra
                    (NetworkService.MESSAGE_KEY));
            handleMessage(received);
        }
    }

    public ManagerService(){
        super();
        IntentFilter receivedIntentFilter = new IntentFilter(NetworkService.MESSAGE_RECEIVED);
        broadcastReceiver = new MessageBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        this.dbAdapter = ((MyApplication) getApplication()).getDatabaseAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void handleMessage(Map message){
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (messageType.equals(MessageBundle.messageType.TEXT.toString())){
            //TODO: implement notification code
            dbAdapter.storeMessage(message);
            Log.d("Database insertion", message.toString());
        }
        Log.d("Received Message", message.toString());
    }
}
