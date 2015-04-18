package cse.sutd.gtfs;

import android.app.NotificationManager;
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
import cse.sutd.gtfs.Objects.ChatRooms;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class MessagingActivity extends ActionBarActivity {
    private TextView msg;
    private GTFSClient client;
    private MessageAdapter adapter;
    private ArrayList<MessageBundle> messageList;
   // private ArrayList<String> timestampList;

    private String toPhoneNumber;
    private String sessionToken;    //get from client.getSESSIONID();
    private String chatroomID;      //get from previous intent
    private String chatroomName;
    private int isGroup;

    private MessageDbAdapter dbMessages;
    private ChatRooms chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMessages = MessageDbAdapter.getInstance(this);
        Bundle extras = getIntent().getExtras();

        String title = null;
        if (extras != null) {
            chatroomID = extras.getString(MessageDbAdapter.CHATID);
            toPhoneNumber = extras.getString(MessageBundle.TO_PHONE_NUMBER);
<<<<<<< HEAD
            Cursor contactList = dbMessages.getContacts();
            final ArrayList<Contact> contacts = new ArrayList<Contact>();
            if (contactList != null) {
                contactList.moveToFirst();
                while (contactList.moveToNext()) {
                    contacts.add(new Contact(contactList.getString(0), contactList.getString(1)));
                }
                contactList.close();
            }
            isGroup = extras.getInt("ISGROUP");
            chat = new ChatRooms(chatroomID,chatroomName,isGroup);
=======
            isGroup = extras.getInt(MessageDbAdapter.CHATID);
            title = extras.getString(MessageDbAdapter.CHATNAME);
            chat = new ChatRooms(chatroomID,toPhoneNumber,isGroup);
            dbMessages.clearRead(chatroomID);
>>>>>>> ce41bd0f575345b9ab37c69327a443fa504b29ba
        }
        if (isGroup == 0)
            title = dbMessages.getUsername(chatroomID);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_action_profile); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(title);
        setContentView(R.layout.activity_messaging);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);

        sessionToken = client.getSESSION_ID();

        ListView listview = (ListView) findViewById(R.id.messageList);

        messageList = new ArrayList<>();
        //timestampList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, userID, isGroup);
        updateUI();
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
                    textBundle.putChatroomID(chatroomID);

                    Log.d("Attempting to send", textBundle.getMessage().toString());
                    Intent intent = new Intent(MessagingActivity.this, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(textBundle.getMessage()));
                    MessagingActivity.this.startService(intent);

                    messageList.add(textBundle);
                    adapter.notifyDataSetChanged();
                    msg.setText("");
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

    @Override
    public void onStart(){
        super.onStart();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }
    private void handleMessage(Map message) {
        String messageType = (String) message.get(MessageBundle.TYPE);
        if (MessageBundle.messageType.TEXT_RECEIVED.toString().equals(messageType)) {
            updateUI();
            Log.d("adapter updated", message.toString());
        }else if (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString().equals(messageType)){
            for(Object user: (Object[]) message.get(MessageBundle.USERS)){
                if(user.equals(toPhoneNumber)){
                    chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                    Log.d("Messaging activity id updated", chatroomID);
                    break;
                }
            }
        }
    }

    private void updateUI(){
        Cursor msgBundles = dbMessages.getChatMessages(chatroomID);
        if (msgBundles != null) {
            messageList.clear();
            if(msgBundles.getCount()>0) {
                msgBundles.moveToFirst();
                do {
                    MessageBundle a = new MessageBundle(msgBundles.getString(0),
                            sessionToken, MessageBundle.messageType.TEXT);
                    a.putMessage(msgBundles.getString(1));
                    a.putTimestamp(msgBundles.getString(2));
                    messageList.add(a);
//                    timestampList.add(msgBundles.getString(2));
                } while (msgBundles.moveToNext());
                msgBundles.close();
            }
        }
        adapter.notifyDataSetChanged();
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
