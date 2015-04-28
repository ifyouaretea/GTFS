package cse.sutd.gtfs.Activities.Group;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Adapters.EventAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Event;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.MessageManagement.ManagerService;
import cse.sutd.gtfs.MessageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

public class EventInfoActivity extends ActionBarActivity {
    private GTFSClient client;
    private MessageDbAdapter dbMessages;
    ListView listview;
    ArrayList<Event> events;
    EventAdapter eventAdapter;
    public static final String CHAT_ID_KEY = "chatID";
    private String chatID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();

        if (client.getID() == null) {
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);
        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            chatID = extras.getString(CHAT_ID_KEY);
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Event Info");
        setContentView(R.layout.activity_event_info);

        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        dbMessages = MessageDbAdapter.getInstance(this);

        listview = (ListView) findViewById(R.id.contactList);
        events = new ArrayList<>();
        getGroupEvents();
        eventAdapter = new EventAdapter(this, events, chatID);
        listview.setAdapter(eventAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                PopupMenu popupMenu = new PopupMenu(EventInfoActivity.this, view);
                for(String voter: events.get(position).getVoters()) {
                    if(voter.trim().equals(client.getID()))
                        popupMenu.getMenu().add(client.getNAME());
                    else {
                        String voterName = dbMessages.getUsername(voter.trim());
                        if(voterName != null)
                            popupMenu.getMenu().add(voterName);
                    }
                }
                if(popupMenu.getMenu().size() == 0)
                    popupMenu.getMenu().add("No votes");
                popupMenu.show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getGroupEvents(){
        Intent importEvents = new Intent(getApplicationContext(), NetworkService.class);
        MessageBundle importBundle = new MessageBundle(client.getID(),
                client.getSESSION_ID(), MessageBundle.messageType.GET_EVENTS);

        importBundle.putChatroomID(chatID);
        String getChatroomString = JsonWriter.objectToJson
                (importBundle.getMessage());
        importEvents.putExtra(NetworkService.MESSAGE_KEY, getChatroomString);
        startService(importEvents);
    }

    private void updateUI(){
        events.clear();
        Cursor eventsCursor = dbMessages.getEventsForChat(chatID);
        if(eventsCursor != null){
            if(eventsCursor.getCount() < 1)
                return;
            eventsCursor.moveToFirst();
            do{
                Event event = new Event(eventsCursor.getString(1), eventsCursor.getString(0),
                        eventsCursor.getLong(2));
                ArrayList<String> voters = new ArrayList<>();
                String rawVoterString = eventsCursor.getString(3);
                String[] parsedVoters = rawVoterString.replaceAll("\\[", "").replaceAll("\\]", "")
                        .split("\\D+");
                for(String voter : parsedVoters)
                    voters.add(voter);
                event.setVoters(voters);
                events.add(event);
            }while(eventsCursor.moveToNext());
        }
        eventAdapter.notifyDataSetChanged();
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }
}
