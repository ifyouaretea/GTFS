package cse.sutd.gtfs.Activities.Messaging;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.view.Window;
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

import cse.sutd.gtfs.Activities.Notes.NoteListActivity;
import cse.sutd.gtfs.Adapters.MessageAdapter;

import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


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


    private PopupMenu addTagPopupMenu;
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

        String title = null;

        if (extras != null) {
            toPhoneNumber = extras.getString(MessageBundle.TO_PHONE_NUMBER);

            if(toPhoneNumber != null)
                chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);
            else
                chatroomID = extras.getString(MessageDbAdapter.CHATID);

            isGroup = extras.getInt(MessageDbAdapter.ISGROUP);
            title = extras.getString(MessageDbAdapter.CHATNAME);
            chat = new ChatRoom(chatroomID,toPhoneNumber,isGroup);
            dbMessages.clearRead(chatroomID);
        }

        if (isGroup == 0) {
            if (chatroomID != null)
                title = dbMessages.getUsername(chatroomID);
            else
                title = dbMessages.getUsernameFromNumber(toPhoneNumber);
        }

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

        listview = (ListView) findViewById(R.id.messageList);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, listview, messageList, userID, isGroup);
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

                    for(int i = 0; i < addTagPopupMenu.getMenu().size(); i++){
                        if(addTagPopupMenu.getMenu().getItem(i).isChecked())
                            textBundle.putTag(addTagPopupMenu.getMenu()
                                    .getItem(i).getTitle().toString());
                    }

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

   /*     searchButton = (Button) findViewById(R.id.message_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getFilter().filter(messageSearchBar.getText());
            }
        });*/

        tagImageView = (ImageView) findViewById(R.id.add_tag_button);

        addTagPopupMenu = new PopupMenu(MessagingActivity.this, tagImageView);
        addTagPopupMenu.getMenuInflater().inflate(R.menu.menu_add_tag, addTagPopupMenu .getMenu());
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
                if(searchPosition <= 0)
                    searchPosition = adapter.getSearchResult().size() - 1;
                else
                    searchPosition--;
                moveToSearch();
            }
        });

        ImageView upSearch = (ImageView) findViewById(R.id.search_up_arrow);
        upSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchPosition >= adapter.getSearchResult().size() - 1)
                    searchPosition = 0;
                else
                    searchPosition++;
                moveToSearch();
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

    private void moveToSearch(){
        List<Integer> searchResults = adapter.getSearchResult();
        if(searchResults.size() > 0 && searchResults.get(searchPosition) > 0) {
            listview.setSelection(searchResults.get(searchPosition));
            listview.requestFocus();
            listview.getChildAt(searchResults.get(searchPosition)).getBackground().setColorFilter(
                    Color.parseColor("#00f00"), PorterDuff.Mode.DARKEN
            );
        }
        Log.d(String.valueOf(searchResults.size()), String.valueOf(searchPosition));
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
            case R.id.message_search_icon:
                searchBarLayout.setVisibility(View.VISIBLE);
                getSupportActionBar().hide();
                return true;
            case R.id.message_tags:
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.message_tags));
                final List<String> tagList = adapter.getTags();
                Log.d("Tags", tagList.toString());
                if (tagList.size() > 0) {
                    for (int i = 0; i < tagList.size(); i++)
                        popup.getMenu().add(i, i, i, tagList.get(i));
                    popup.getMenu().add(tagList.size(), tagList.size(), tagList.size(), "No tags");
                    popup.setOnMenuItemClickListener(
                            new PopupMenu.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(MenuItem item) {
                                    if(item.getItemId() == tagList.size())
                                        adapter.getFilter().filter("");
                                    else
                                        adapter.getFilter().filter(item.getTitle()
                                                .toString().toLowerCase());
                                    return true;
                                }
                            }
                    );
                }else{
                    popup.getMenu().add("No tags");
                }
                popup.show();
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
        }else if (MessageBundle.messageType.SINGLE_ROOM_INVITATION.toString().equals(messageType)){
            for(Object user: (Object[]) message.get(MessageBundle.USERS)){
                if(user.equals(toPhoneNumber)){
                    chatroomID = (String) message.get(MessageBundle.CHATROOMID);
                    Log.d("Messaging id updated", chatroomID);
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
