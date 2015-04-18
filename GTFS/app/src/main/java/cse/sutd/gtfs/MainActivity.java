package cse.sutd.gtfs;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cse.sutd.gtfs.Adapters.ChatAdapters;
import cse.sutd.gtfs.Objects.ChatRooms;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class MainActivity extends ActionBarActivity {
    private GTFSClient client;
    private SharedPreferences.Editor editor;
    private static final ExecutorService exec = new ScheduledThreadPoolExecutor(100);
    private ArrayList<ChatRooms> chatroom;
    private MessageDbAdapter dbMessages;
    private ListView listview;

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        private MessageBroadcastReceiver(){}
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d("Broadcast receiver", "received intent!");
            Map received = (Map) JsonReader.jsonToJava(intent.getStringExtra
                    (NetworkService.MESSAGE_KEY));
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

        MessageBundle userRequestBundle = new MessageBundle(client.getID(), client.getSESSION_ID(),
                MessageBundle.messageType.GET_USERS);

        Callable<String[][]> task = new Callable<String[][]>() {
            public String[][] call() {

                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                phones.moveToFirst();
                ArrayList<String> phoneNumbers10 = new ArrayList<String>();
                ArrayList<ArrayList<String>> phoneNumbers = new ArrayList<ArrayList<String>>();
                int i = 0;
                while (phones.moveToNext()) {
                    if (i >= 20) {
                        i = 0;
                        phoneNumbers.add(phoneNumbers10);
                        phoneNumbers10 = new ArrayList<String>();
                    }
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                    String h1 = phoneNumber.replaceAll("\\s", "");
                    String h2 = h1.replaceAll(" ", "");
                    h2 = h2.replace("+65", "");
                    h2 = h2.replaceAll("\\D", "");
                    if (h2.length() >= 8 && !h2.equals(client.getID())) {
                        phoneNumbers10.add(h2);
                        i++;
                    }
                }
                if (phoneNumbers10.size() < 20) {
                    for (int l = 0; l < (20 - phoneNumbers10.size()); l++) {
                        phoneNumbers10.add("");
                    }
                }
                phones.close();
                String[][] phonenumber = new String[phoneNumbers.size()][phoneNumbers10.size()];
                for (int j = 0; j < phoneNumbers.size(); j++)
                    for (int k = 0; k < phoneNumbers10.size(); k++)
                        phonenumber[j][k] = phoneNumbers.get(j).get(k);
                return phonenumber;
            }
        };
        String[][] users;
        Future<String[][]> backtothefuture = exec.submit(task);
        try {
            backtothefuture.get(1, TimeUnit.MINUTES);

            users = backtothefuture.get();
            for (String[] s : users) {
                userRequestBundle.putUsers(s);
                Intent i = new Intent(getApplicationContext(), NetworkService.class);
                i.putExtra(NetworkService.MESSAGE_KEY,
                        JsonWriter.objectToJson(userRequestBundle.getMessage()));

                this.startService(i);
            }
        }catch(Exception e){}

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
        chatroom = new ArrayList<ChatRooms>();

        if (chatrooms != null) {
            chatrooms.moveToFirst();
            while (chatrooms.moveToNext()) {
                ChatRooms a = new ChatRooms(chatrooms.getString(0),
                        chatrooms.getString(1), chatrooms.getString(2));
                chatroom.add(a);
                Log.d("chatroom", chatrooms.getString(0));
            }
            chatrooms.close();
        }

        ChatAdapters adapter = new ChatAdapters(this, chatroom);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = ((ChatRooms) parent.getItemAtPosition(position)).getId();
                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                i.putExtra("ID", chatroom.get(position).getId());
                startActivity(i);
            }
        });

    }
}