package cse.sutd.gtfs.Activities.Messaging;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import cse.sutd.gtfs.Activities.ContactsActivity;
import cse.sutd.gtfs.Activities.ProfileActivity;
import cse.sutd.gtfs.Activities.SettingsActivity;
import cse.sutd.gtfs.Adapters.ChatAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;


public class MainActivity extends ActionBarActivity {
    private GTFSClient client;
    private SharedPreferences.Editor editor;
    private ArrayList<ChatRoom> chatroom;
    private MessageDbAdapter dbMessages;
    private ListView listview;

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        private MessageBroadcastReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent){
           updateUI();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);

        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Chats");
        setContentView(R.layout.activity_main);
        Log.d("user", prefs.getString("userid", null));

        dbMessages = MessageDbAdapter.getInstance(this);
        listview = (ListView) findViewById(R.id.chatList);

        updateUI();

        client.resetNotificationMap();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_contacts:
                intent = new Intent(this, ContactsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_chat:
                return true;
            case R.id.action_group:
                intent = new Intent(this, NewGroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
//                Digits.getSessionManager().clearActiveSession();
                editor.putString("userid", null);
                editor.commit();
                client.setID(null);
                MainActivity.this.finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        client.resetNotificationMap();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void updateUI(){
        Cursor chatrooms = dbMessages.getChats();
        chatroom = new ArrayList<>();

        if (chatrooms != null) {
            if(chatrooms.getCount() >= 1) {
                chatrooms.moveToFirst();
                do {
                    String id = chatrooms.getString(0);
                    String name = chatrooms.getString(1);
                    int isGroup = chatrooms.getInt(2);
                    if (isGroup == 0)
                        name = dbMessages.getUsername(id);

                    ChatRoom a = new ChatRoom(id, name, isGroup);
                    chatroom.add(a);
                } while (chatrooms.moveToNext());

                chatrooms.close();
            }
        }

        ChatAdapter adapter = new ChatAdapter(this, chatroom);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = ((ChatRoom) parent.getItemAtPosition(position)).getId();
                Intent i = new Intent(MainActivity.this, MessagingActivity.class);
                i.putExtra(MessageDbAdapter.CHATID, chatroom.get(position).getId());
                i.putExtra(MessageDbAdapter.ISGROUP, chatroom.get(position).getIsGroup());
                i.putExtra(MessageDbAdapter.CHATNAME, chatroom.get(position).getName());
                startActivity(i);
            }
        });

    }
}