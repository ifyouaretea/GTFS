package cse.sutd.gtfs.Activities.Messaging;

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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Activities.Notes.NoteListActivity;
import cse.sutd.gtfs.Adapters.MessageAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

/*
 * From MainActivity Requires (CHATID, ISGROUP, GROUPNAME)
 * From ContactsActivity Requires (CHATID, TO_PHONE_NUMBER, ISGROUP)
 * From AddContactToGroup Requires (CHATID, ISGROUP, GROUPNAME)
 */
public class MessagingActivity extends ActionBarActivity {
    private TextView msg;
    private EditText messageSearchBar;
    private Button searchButton;
    private GTFSClient client;
    private MessageAdapter adapter;
    private View actionBarView;
    private ArrayList<MessageBundle> messageList;

    private String toPhoneNumber;
    private String sessionToken;    //get from client.getSESSIONID();
    private String chatroomID;      //get from previous intent
    private String chatroomName;
    private int isGroup;

    private MessageDbAdapter dbMessages;
    private ChatRoom chat;

    /**
     * Required extras
     * isGroup:
     * Chatname
     * chatID
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMessages = MessageDbAdapter.getInstance(this);
        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            isGroup = extras.getInt(MessageDbAdapter.ISGROUP);
            if (isGroup == 0) {
                toPhoneNumber = extras.getString(MessageBundle.TO_PHONE_NUMBER);
                if(toPhoneNumber != null)
                    chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);
                else
                    chatroomID = extras.getString(MessageDbAdapter.CHATID);

                if (chatroomID != null)
                    chatroomName = dbMessages.getUsername(chatroomID);
                else
                    chatroomName = dbMessages.getUsernameFromNumber(toPhoneNumber);

                chat = new ChatRoom(chatroomID, chatroomName,toPhoneNumber,isGroup);
            }else{
                chatroomID = extras.getString(MessageDbAdapter.CHATID);
                if (chatroomID != null)
                    chatroomName = dbMessages.getChatroomName(chatroomID);
                else
                    chatroomName = extras.getString(MessageDbAdapter.CHATNAME);

                if (chatroomName != null)
                    chatroomID = dbMessages.getChatroomID(chatroomName);
                else
                    chatroomName = dbMessages.getUsernameFromNumber(toPhoneNumber);

                chat = new ChatRoom(chatroomID, chatroomName,isGroup);
            }
            dbMessages.clearRead(chatroomID);
        }

//        Log.d("name",chatroomName);
//        Log.d("ID",chatroomID);
//        Log.d("isGroup",String.valueOf(isGroup));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_action_profile); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(chatroomName);

        setContentView(R.layout.activity_messaging);
        messageSearchBar = (EditText) findViewById(R.id.message_search_bar);

        client = (GTFSClient) getApplicationContext();
        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);

        sessionToken = client.getSESSION_ID();

        ListView listview = (ListView) findViewById(R.id.messageList);

        messageList = new ArrayList<>();
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
                                "Error in sending", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final MessageBundle textBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.TEXT);

                    textBundle.putMessage(msg.getText().toString());
                    textBundle.putChatroomID(chatroomID);
                    //TODO: remove hardcoded tags
                    textBundle.putParsedTags("[important]");

                    Intent intent = new Intent(client, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(textBundle.getMessage()));
                    client.startService(intent);

                    messageList.add(textBundle);
                    adapter.notifyDataSetChanged();
                    msg.setText("");
                }
            }
        });

        searchButton = (Button) findViewById(R.id.message_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getFilter().filter(messageSearchBar.getText());
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
            case R.id.open_notes:
                Intent openNotes = new Intent(this, NoteListActivity.class);
                openNotes.putExtra(NoteListActivity.CHAT_ID_KEY, chatroomID);
                startActivity(openNotes);
                return true;

            case R.id.message_tags:
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.message_tags));
                List<String> tagList = adapter.getTags();
                Log.d("Tags", tagList.toString());
                if (tagList.size() > 0) {
                    for (int i = 0; i < tagList.size(); i++)
                        popup.getMenu().add(i, i, i, tagList.get(i));
                    popup.setOnMenuItemClickListener(
                            new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    adapter.getFilter().filter(item.getTitle().toString().toLowerCase());
                                    return true;
                                }
                            }
                    );
                }else{
                    popup.getMenu().add("NO TAGS");
                }
                popup.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public View getActionBarView() {
        Window window = getWindow();
        View v = window.getDecorView();
        int resId = getResources().getIdentifier("action_bar_container", "id", "android");
        return v.findViewById(resId);
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
        }else if (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString().equals(messageType)){
            for(Object user: (Object[]) message.get(MessageBundle.USERS)){
                if(user.equals(toPhoneNumber)){
                    chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                    Log.d("Room id updated", chatroomID);
                    break;
                }
            }
        }else if (MessageBundle.messageType.ROOM_INVITATION.toString().equals(messageType)){
            for(Object user: (Object[]) message.get(MessageBundle.CHATROOM_NAME)){
                if(chatroomName.equals(user)){
                    chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                    Log.d("Room id updated", chatroomID);
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
                    a.putParsedTags(msgBundles.getString(3));
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
