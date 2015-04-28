package cse.sutd.gtfs.Activities.Group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.Adapters.ContactAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.MessageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class GroupInfoActivity extends ActionBarActivity {
    private GTFSClient client;
    private MessageDbAdapter dbMessages;
    ListView listview;
    ArrayList<Contact> contacts = new ArrayList<>();;
    ContactAdapter contactAdapter;
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
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            chatID = extras.getString(CHAT_ID_KEY);
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Group Info");
        setContentView(R.layout.activity_group_info);

        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);
        final String sessionToken = client.getSESSION_ID();
        dbMessages = MessageDbAdapter.getInstance(this);

        listview = (ListView) findViewById(R.id.groupList);
        getGroupUsers();
        contactAdapter = new ContactAdapter(this, contacts);
        listview.setAdapter(contactAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String toPhoneNumber = ((Contact) parent.getItemAtPosition(position)).getNumber();
                if(toPhoneNumber.equals(client.getID()))
                    return;

                String chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);

                if (chatroomID == null) {
                    MessageBundle createBundle = new MessageBundle(userID, sessionToken,
                            MessageBundle.messageType.CREATE_SINGLE_ROOM);
                    createBundle.putToPhoneNumber(toPhoneNumber);
                    //TODO: setChatroomName
                    createBundle.putChatroomName(userID + "," + toPhoneNumber);

                    Intent intent = new Intent(GroupInfoActivity.this, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,
                            JsonWriter.objectToJson(createBundle.getMessage()));
                    GroupInfoActivity.this.startService(intent);
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
                //TODO: check if chatroom exist
                chatroomID = dbMessages.getChatIDForUser(toPhoneNumber);
                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                i.putExtra(MessageDbAdapter.CHATID, chatroomID);
                i.putExtra(MessageBundle.TO_PHONE_NUMBER, toPhoneNumber);
                i.putExtra(MessageDbAdapter.ISGROUP, 0);
                startActivity(i);
                finish();
            }
        });
    }

    private void getGroupUsers() {
        String[] userList = new String[0];
        Cursor userBundles = dbMessages.getUserForGroup(chatID);
        if (userBundles != null) {
            if (userBundles.getCount() > 0) {
                userBundles.moveToFirst();
                String users = userBundles.getString(0).replaceAll("\\[", "").replaceAll("\\]", "");
                userList = users.split(",");
            }
            userBundles.close();
        }

        for (String s : userList) {
            Log.d("group user",s);
            s=s.trim();
            Cursor contactBundles = dbMessages.getContact(s);
            Contact a;
            if (contactBundles != null) {
                if (contactBundles.getCount() > 0) {
                    contactBundles.moveToFirst();
                    a = new Contact(contactBundles.getString(0), contactBundles.getString(1));
                    Log.d("name",a.getName());
                    contacts.add(a);
                }else{
                    if (s.equals(client.getID()))
                      a = new Contact(s,"Me");
                    else
                        a = new Contact(s, s);
                    contacts.add(a);
                }
                contactBundles.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_group_info, menu);
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
}
