package cse.sutd.gtfs.Activities;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.Adapters.ContactAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.ManagerService;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class ContactsActivity extends ActionBarActivity {
    private GTFSClient client;
    private static final ExecutorService exec = new ScheduledThreadPoolExecutor(100);
    private MessageDbAdapter dbMessages;
    ListView listview;
    ArrayList<Contact> contacts;
    ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Contacts");
        setContentView(R.layout.activity_contacts);

        requestContacts();

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);
        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);


        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);
        final String sessionToken = client.getSESSION_ID();
        dbMessages = MessageDbAdapter.getInstance(this);

        //find listView
        listview = (ListView) findViewById(R.id.contactList);
        contacts = new ArrayList<>();

        contactAdapter = new ContactAdapter(this, contacts);
        listview.setAdapter(contactAdapter);
        updateUI();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String toPhoneNumber = ((Contact) parent.getItemAtPosition(position)).getNumber();

                String chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);

                if (chatroomID == null) {
                    MessageBundle createBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.CREATE_SINGLE_ROOM);
                    createBundle.putToPhoneNumber(toPhoneNumber);
                    //TODO: setChatroomName
                    createBundle.putChatroomName(userID + "," + toPhoneNumber);

                    Intent intent = new Intent(ContactsActivity.this, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(createBundle.getMessage()));
                    ContactsActivity.this.startService(intent);
                }
                try{
                    Thread.sleep(1000);
                }catch(Exception e){}
                //TODO: check if chatroom exist
                chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);
                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                i.putExtra(MessageDbAdapter.CHATID, chatroomID);
                i.putExtra(MessageBundle.TO_PHONE_NUMBER, toPhoneNumber);
                i.putExtra(MessageDbAdapter.ISGROUP, 0);
                startActivity(i);
            }
        });
    }

    private void requestContacts() {
        MessageBundle userRequestBundle = new MessageBundle(client.getID(), client.getSESSION_ID(),
                MessageBundle.messageType.GET_USERS);

        Callable<String[][]> task = new Callable<String[][]>() {
            public String[][] call() {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                phones.moveToFirst();
                ArrayList<ArrayList<String>> phoneNumbers = new ArrayList<>();
                final int USER_LIMIT = 15;
                do {
                    ArrayList<String> numberSubList = new ArrayList<>();
                    for (int i = 0; i < USER_LIMIT; i++) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                        String h1 = phoneNumber.replaceAll("\\s", "");
                        String h2 = h1.replaceAll(" ", "");
                        h2 = h2.replace("+65", "");
                        h2 = h2.replaceAll("\\D", "");
                        if (h2.length() >= 8 && !h2.equals(client.getID()))
                            numberSubList.add(h2);
                        if (!phones.moveToNext())
                            break;
                    }
                    phoneNumbers.add(numberSubList);
                } while (!phones.isAfterLast());

                phones.close();
                String[][] phonenumber = new String[phoneNumbers.size()][USER_LIMIT];

                for (int j = 0; j < phoneNumbers.size(); j++)
                    for (int k = 0; k < phoneNumbers.get(j).size(); k++)
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
        } catch (Exception e) {
        }
    }

    private void updateUI() {
        Log.d("ContactActivity", "Updating UI");
        contacts.clear();
        Cursor contactBundles = dbMessages.getContacts();

        if (contactBundles != null) {
            if (contactBundles.getCount() > 0) {
                contactBundles.moveToFirst();
                do {
                    Contact a = new Contact(contactBundles.getString(0), contactBundles.getString(1));
                    contacts.add(a);
                } while (contactBundles.moveToNext());
            }
            contactBundles.close();
        }
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_contacts, menu);
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
        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }
}
