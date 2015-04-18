package cse.sutd.gtfs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.Map;

import cse.sutd.gtfs.Adapters.MessageAdapter;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class MessagingActivity extends ActionBarActivity {
    private TextView msg;
    private GTFSClient client;
    private MessageAdapter adapter;
    private ArrayList<MessageBundle> message;

    private String toPhoneNumber;   //TODO: idk yet
    private String sessionToken;    //get from client.getSESSIONID();
    private String chatroomID;      //get from previous intent


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageDbAdapter dbMessages = MessageDbAdapter.getInstance(this);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            chatroomID = extras.getString("ID");
            toPhoneNumber = extras.getString(MessageBundle.TO_PHONE_NUMBER);
        }

        //TODO: Download chatrooms from server to local DB

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_action_profile); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(dbMessages.getChatroomName(chatroomID));
        setContentView(R.layout.activity_messaging);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);

        sessionToken = client.getSESSION_ID();

        ListView listview = (ListView) findViewById(R.id.messageList);

        Cursor msgBundles = dbMessages.getChatMessages(chatroomID);
        message = new ArrayList<MessageBundle>();
        final ArrayList<String> timestamp = new ArrayList<String>();
        if (msgBundles != null) {
            if(msgBundles.getCount()>0) {
                msgBundles.moveToFirst();
                do {
                    MessageBundle a = new MessageBundle(msgBundles.getString(0),
                            sessionToken, MessageBundle.messageType.TEXT);
                    a.putMessage(msgBundles.getString(1));
                    a.putChatroomID(msgBundles.getString(2));
                    message.add(a);
                    timestamp.add(msgBundles.getString(2));
                } while (msgBundles.moveToNext());

                msgBundles.close();
            }
        }
//        MessageBundle hi = new MessageBundle("1234", "asdsd", MessageBundle.messageType.TEXT);
//        hi.putMessage("hi Nikhil!"); hi.putToPhoneNumber("3128869026"); hi.putChatroomID("12345");
//        MessageBundle hi1 = new MessageBundle("3128869026", "asdsd", MessageBundle.messageType.TEXT);
//        hi1.putMessage("Beer Tonight! On?"); hi1.putToPhoneNumber("1234"); hi1.putChatroomID("12345");

//        message.add(hi); message.add(hi1);

        adapter = new MessageAdapter(this, message, userID);
        listview.setAdapter(adapter);
        msg = (TextView) findViewById(R.id.message);
        Button send = (Button) findViewById(R.id.sendMessageButton);

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);
        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (msg.getText().toString().trim().length() > 0) {

                    if (sessionToken == null)
                        sessionToken = client.getSESSION_ID();
                    if (chatroomID == null) {
                        Log.d("FAIL FAIL FAIL", "FAIL");
                        Toast.makeText(MessagingActivity.this,
                                "Error in sending", Toast.LENGTH_SHORT);
                        return;
                    }
                    final MessageBundle textBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.TEXT);

                    textBundle.putMessage(msg.getText().toString());
                    textBundle.putToPhoneNumber(toPhoneNumber);
                    textBundle.putChatroomID(chatroomID);
                    textBundle.putTimestamp();

                    Log.d("Attempting to send", textBundle.getMessage().toString());
                    Intent intent = new Intent(MessagingActivity.this, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(textBundle.getMessage()));
                    MessagingActivity.this.startService(intent);

                    message.add(textBundle);


                    msg.setText("");
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messaging, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleMessage(Map message) {
        Log.d("Handle message", "I'm handling a message!");
        String messageType = (String) message.get(MessageBundle.TYPE);

        if (MessageBundle.messageType.TEXT_RECEIVED.toString().equals(messageType)) {
            adapter.notifyDataSetChanged();
            Log.d("adapter updated", message.toString());
        }else if (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString().equals(messageType)) {
            for(Object user: (Object[]) message.get(MessageBundle.USERS)){
                if(user.equals(toPhoneNumber)){
                    chatroomID = (String) message.get
                            (message.get(MessageBundle.CHATROOMID));
                    break;
                }
            }
        }
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Map received = (Map) JsonReader.jsonToJava(intent.getStringExtra
                    (NetworkService.MESSAGE_KEY));
            handleMessage(received);
        }
    }
}
