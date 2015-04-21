package cse.sutd.gtfs.Activities.Messaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cse.sutd.gtfs.Activities.Group.EventInfoActivity;
import cse.sutd.gtfs.Activities.Group.EventsActivity;
import cse.sutd.gtfs.Activities.Group.GroupInfoActivity;
import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Activities.Notes.NoteListActivity;
import cse.sutd.gtfs.Adapters.MessageAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.Objects.Event;
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
    private ImageView tagImageView;
    private ArrayList<MessageBundle> messageList;
    private LinearLayout searchBarLayout;
    private ListView listview;
    private int searchPosition = -1;

    private PopupMenu popup;
    private PopupMenu addTagPopupMenu;
    private String toPhoneNumber=null;
    private String sessionToken;    //get from client.getSESSIONID();
    private String chatroomID=null;      //get from previous intent
    private String chatroomName=null;
    private String userID=null;
    private int isGroup;

    private MessageDbAdapter dbMessages;
    private ChatRoom chat;
    private String[] userList;
    private String[] eventsList;

    private List<Event> unvotedEvents;


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
                if (toPhoneNumber != null)
                    chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);
                else
                    chatroomID = extras.getString(MessageDbAdapter.CHATID);
                if (chatroomID != null)
                    chatroomName = dbMessages.getUsername(chatroomID);
                else
                    chatroomName = dbMessages.getUsernameFromNumber(toPhoneNumber);

                chat = new ChatRoom(chatroomID, chatroomName, toPhoneNumber, isGroup);
            } else {
                chatroomID = extras.getString(MessageDbAdapter.CHATID);
                if (chatroomID != null)
                    chatroomName = dbMessages.getChatroomName(chatroomID);
                else
//=======
//            chatroomID = extras.getString(MessageDbAdapter.CHATID);
//            Log.d("ChatroomID", chatroomID);
//            if (chatroomID != null) {
//                isGroup = extras.getInt(MessageDbAdapter.ISGROUP);
//                Log.d("isGroup", String.valueOf(isGroup));
//                if(isGroup == 0)
//                    chatroomName = dbMessages.getUsername(chatroomID);
//                else if(isGroup == 1)
//                    chatroomName = dbMessages.getChatroomName(chatroomID);
//
//            }else{
//                isGroup = extras.getInt(MessageDbAdapter.ISGROUP);
//                if(isGroup==1){
//>>>>>>> 157e3bf5e09332fa268a9597f112dcd945afd01e
                    chatroomName = extras.getString(MessageDbAdapter.CHATNAME);

                if (chatroomName != null)
                    chatroomID = dbMessages.getChatroomID(chatroomName);
                else
                    chatroomName = dbMessages.getUsernameFromNumber(toPhoneNumber);

                chat = new ChatRoom(chatroomID, chatroomName, isGroup);
            }

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

        client = (GTFSClient) getApplicationContext();
        userID = client.getID();
        if (client.getID() == null) {
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);

        sessionToken = client.getSESSION_ID();

        listview = (ListView) findViewById(R.id.messageList);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, listview, messageList, client.getID(), isGroup);
        listview.setAdapter(adapter);
        updateUI();

        msg = (TextView) findViewById(R.id.message);
        Button send = (Button) findViewById(R.id.sendMessageButton);

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);
        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dbMessages.deleteExpiredChats();
                if(dbMessages.isChatDeleted(chatroomID)) {
                    Toast.makeText(MessagingActivity.this,
                            "Chat has been deleted", Toast.LENGTH_SHORT);
                    return;
                }
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

                    for (int i = 0; i < addTagPopupMenu.getMenu().size(); i++) {
                        if (addTagPopupMenu.getMenu().getItem(i).isChecked()) {
                            textBundle.putTag(addTagPopupMenu.getMenu()
                                    .getItem(i).getTitle().toString());
                            break;
                        }
                    }

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
   /*     searchButton = (Button) findViewById(R.id.message_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getFilter().filter(messageSearchBar.getText());
            }
        });*/

        tagImageView = (ImageView) findViewById(R.id.add_tag_button);

        addTagPopupMenu = new PopupMenu(MessagingActivity.this, tagImageView);
        addTagPopupMenu.getMenuInflater().inflate(R.menu.menu_add_tag, addTagPopupMenu.getMenu());
        addTagPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                item.setChecked(!item.isChecked());
                return item.isChecked();
            }
        });

        tagImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTagPopupMenu.show();
            }
        });
        searchBarLayout = (LinearLayout) findViewById(R.id.message_search_bar_layout);
        messageSearchBar = (EditText) findViewById(R.id.message_search_bar);
        messageSearchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.setSearchTerm(messageSearchBar.getText().toString());
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            }
        );

        ImageView downSearch = (ImageView) findViewById(R.id.search_down_arrow);
        downSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (adapter) {
                    if (searchPosition <= 0)
                        searchPosition = adapter.getSearchResult().size() - 1;
                    else
                        searchPosition--;
                    moveToSearch();
                }
            }
        });

        ImageView upSearch = (ImageView) findViewById(R.id.search_up_arrow);
        upSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchronized (adapter) {
                    if (searchPosition >= adapter.getSearchResult().size() - 1)
                        searchPosition = 0;
                    else
                        searchPosition++;
                    moveToSearch();
                }
            }
        });

        ImageView backButton = (ImageView) findViewById(R.id.search_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportActionBar().show();
                searchBarLayout.setVisibility(View.GONE);
            }
        });
    }

    private void moveToSearch() {
        List<Integer> searchResults = adapter.getSearchResult();
        if (searchResults.size() > 0 && searchResults.get(searchPosition) > 0) {
            listview.setSelection(searchResults.get(searchPosition));
            listview.requestFocus();
        }
        Log.d(String.valueOf(searchResults.size()), String.valueOf(searchPosition));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(isGroup ==0)
            getMenuInflater().inflate(R.menu.menu_messaging, menu);
        else
            getMenuInflater().inflate(R.menu.menu_messaging_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
            case R.id.message_events:
                Intent createEvent = new Intent(this, EventsActivity.class);
                createEvent.putExtra(EventsActivity.CHAT_ID_KEY, chatroomID);
                startActivity(createEvent);
                return true;
            case R.id.open_notes:
                Intent openNotes = new Intent(this, NoteListActivity.class);
                openNotes.putExtra(NoteListActivity.CHAT_ID_KEY, chatroomID);
                startActivity(openNotes);
                return true;
            case R.id.group_info:
                Intent groupinfo = new Intent(this, GroupInfoActivity.class);
                groupinfo.putExtra(GroupInfoActivity.CHAT_ID_KEY, chatroomID);
                startActivity(groupinfo);
                return true;
            case R.id.message_search_icon:
                searchBarLayout.setVisibility(View.VISIBLE);
                getSupportActionBar().hide();
                return true;
            case R.id.message_tags:
                if (popup == null) {
                    popup = new PopupMenu(this, findViewById(R.id.message_tags));
                    popup.setOnMenuItemClickListener(
                            new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    if (!item.isChecked()) {
                                        item.setChecked(true);
                                        adapter.applyTag(item.getTitle().toString());
                                    }else if(item.isChecked()){
                                        item.setChecked(false);
                                        adapter.removeTag(item.getTitle().toString());
                                    }
                                    return true;
                                }
                            }
                    );
                    List<String> tagList = dbMessages.getTagsForChat(chatroomID);
                    if (tagList.size() > 0) {
                        for (int i = 0; i < tagList.size(); i++) {
                            popup.getMenu().add(i, i, i, tagList.get(i));
                            popup.getMenu().getItem(i).setCheckable(true);
                        }
                    }
                }
                popup.show();
                return true;
            case R.id.view_events:
                Intent eventinfo = new Intent(this, EventInfoActivity.class);
                eventinfo.putExtra(GroupInfoActivity.CHAT_ID_KEY, chatroomID);
                startActivity(eventinfo);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        dbMessages.clearRead(chatroomID);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbMessages.clearRead(chatroomID);
    }

    private void handleMessage(Map message) {
        String messageType = (String) message.get(MessageBundle.TYPE);
        if (MessageBundle.messageType.TEXT_RECEIVED.toString().equals(messageType)) {
            updateUI();
        } else if (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString().equals(messageType)
                && chatroomID == null && isGroup == 0){
            for (Object user : (Object[]) message.get(MessageBundle.USERS)) {
                if (user.equals(toPhoneNumber)) {
                    chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                    chatroomName = dbMessages.getChatroomName(chatroomID);
                    return;
                }
            }
<<<<<<< HEAD
        } else if (MessageBundle.messageType.ROOM_INVITATION.toString().equals(messageType)
                && chatroomID == null && isGroup == 1) {
=======
        } else if (MessageBundle.messageType.ROOM_INVITATION.toString().equals(messageType)) {
            chatroomName = dbMessages.getUsername((String) message.get(MessageBundle.CHATROOMID));
>>>>>>> bac04b466601064c74f878d075d96c0fa34a0e09
            if (chatroomName.equals(message.get(MessageBundle.CHATROOM_NAME))) {
                chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                Log.d("Room id updated", chatroomID);
            }
        }else if (MessageBundle.messageType.EVENT_CREATED.toString().equals(messageType)) {
<<<<<<< HEAD
            final LinearLayout newEvent = (LinearLayout) findViewById(R.id.eventLayout);
=======
            try {
                dbMessages.insertEvent(message);
            }catch(SQLiteConstraintException e){
                
            }
            final LinearLayout newEvent = (LinearLayout) findViewById(R.id.eventLayout); //TODO: check eventLayout's id
>>>>>>> bac04b466601064c74f878d075d96c0fa34a0e09
            TextView eventDescription = (TextView) newEvent.findViewById(R.id.eventDesc);
            eventDescription.setText((String) message.get(MessageBundle.EVENT_NAME));
            final String eventID = (String) message.get(MessageBundle.EVENT_ID);
            ImageView voteAction = (ImageView)newEvent.findViewById(R.id.vote);
            voteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MessageBundle voteBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.EVENT_VOTE);
                    voteBundle.getMessage().put(MessageBundle.EVENT_ID, eventID);
                    voteBundle.putChatroomID(chatroomID);

                    Intent intent = new Intent(client, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(voteBundle.getMessage()));
                    client.startService(intent);
                    newEvent.setVisibility(View.GONE);
                }
            });

            ImageView unvoteAction = (ImageView)newEvent.findViewById(R.id.unvote);
            unvoteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MessageBundle voteBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.EVENT_UNVOTE);
                    voteBundle.getMessage().put(MessageBundle.EVENT_ID, eventID);
                    voteBundle.putChatroomID(chatroomID);

                    Intent intent = new Intent(client, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(voteBundle.getMessage()));
                    client.startService(intent);
                    newEvent.setVisibility(View.GONE);
                }
            });
            newEvent.setVisibility(View.VISIBLE);
            updateUI();
        }else if (MessageBundle.messageType.EVENT_VOTE_RECEIVED.toString().equals(messageType)) {
            String fromPhoneNumber = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);
            if(fromPhoneNumber.equals(client.getID())){
                MessageBundle a = new MessageBundle(client.getID(),
                        sessionToken, MessageBundle.messageType.TEXT);
                String name = dbMessages.getUsername(fromPhoneNumber);
                a.putMessage("You are attending "+(String)dbMessages.getEventName(MessageBundle.EVENT_ID));
                a.putMessageID(message.get("vote_timestamp").toString());
                a.putChatroomID(message.get(MessageBundle.CHATROOMID).toString());
                a.putFromPhoneNumber(message.get(MessageBundle.FROM_PHONE_NUMBER).toString());
                a.putTag("Admin");
                dbMessages.storeMessage(a.getMessage());
                messageList.add(a);
            }else{
                MessageBundle a = new MessageBundle(client.getID(),
                        sessionToken, MessageBundle.messageType.TEXT);
                String name = dbMessages.getUsername(fromPhoneNumber);
                a.putMessage(name+" is attending "+(String)dbMessages.getEventName(MessageBundle.EVENT_ID));
                a.putMessageID(message.get("vote_timestamp").toString());
                a.putChatroomID(message.get(MessageBundle.CHATROOMID).toString());
                a.putFromPhoneNumber(message.get(MessageBundle.FROM_PHONE_NUMBER).toString());
                a.putTag("Admin");
                dbMessages.storeMessage(a.getMessage());
                messageList.add(a);
            }
            updateUI();
        }else if (MessageBundle.messageType.EVENT_UNVOTE_RECEIVED.toString().equals(messageType)) {
            String fromPhoneNumber = (String) message.get(MessageBundle.FROM_PHONE_NUMBER);
            if(fromPhoneNumber.equals(client.getID())){
                MessageBundle a = new MessageBundle(client.getID(),
                        sessionToken, MessageBundle.messageType.TEXT);
                String name = dbMessages.getUsername(fromPhoneNumber);
                a.putMessage("You are not attending "+(String)dbMessages.getEventName(MessageBundle.EVENT_ID));
                a.putMessageID(message.get("vote_timestamp").toString());
                a.putChatroomID(message.get(MessageBundle.CHATROOMID).toString());
                a.putFromPhoneNumber(message.get(MessageBundle.FROM_PHONE_NUMBER).toString());
                a.putTag("Admin");
                dbMessages.storeMessage(a.getMessage());
                messageList.add(a);
            }else{
                MessageBundle a = new MessageBundle(client.getID(),
                        sessionToken, MessageBundle.messageType.TEXT);
                String name = dbMessages.getUsername(fromPhoneNumber);
                a.putMessage(name+" is not attending "+(String)dbMessages.getEventName(MessageBundle.EVENT_ID));
                a.putMessageID(message.get("vote_timestamp").toString());
                a.putChatroomID(message.get(MessageBundle.CHATROOMID).toString());
                a.putFromPhoneNumber(message.get(MessageBundle.FROM_PHONE_NUMBER).toString());
                a.putTag("Admin");
                dbMessages.storeMessage(a.getMessage());
                messageList.add(a);
            }
            updateUI();
        }
    }

    private void updateUI() {
        Cursor msgBundles = dbMessages.getChatMessages(chatroomID);
        if (msgBundles != null) {
            messageList.clear();
            if (msgBundles.getCount() > 0) {
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
        ((GTFSClient) getApplicationContext()).resetNotificationMap();
        adapter.notifyDataSetChanged();

        unvotedEvents = new ArrayList<>();
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