package cse.sutd.gtfs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cse.sutd.gtfs.Adapters.ContactAdapter;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;


public class NewGroupActivity extends ActionBarActivity {
    private EditText group_name;
    private GTFSClient client;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();


        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("New Group");
        setContentView(R.layout.activity_new_group);

        group_name = (EditText) findViewById(R.id.group_name);
        group_name.requestFocus();
        final TextView countTextView = (TextView) findViewById(R.id.countTextView);
        countTextView.setText("25");
        group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft) {
            }

            @Override
            public void afterTextChanged(Editable group_name) {
                countTextView.setText(Integer.toString(25 - group_name.toString().length()));
            }
        });
        Button add_contact = (Button) findViewById(R.id.addContact);
        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewGroupActivity.this, AddContactToGroup.class);
                startActivityForResult(intent, 0);
            }
        });

        MessageDbAdapter dbMessages = MessageDbAdapter.getInstance(this);
        Cursor contactList = dbMessages.getContacts();
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        if (contactList != null) {
            contactList.moveToFirst();
            while (contactList.moveToNext()) {
                contacts.add(new Contact(contactList.getString(0), contactList.getString(1)));
            }
            contactList.close();
        }

        final ArrayList<Contact> filteredContacts = new ArrayList<Contact>();
        final ContactAdapter cntcts = new ContactAdapter(this, filteredContacts);

        Switch mySwitch = (Switch) findViewById(R.id.switch1);
        final LinearLayout timing = (LinearLayout) findViewById(R.id.timing);
        final EditText time = (EditText) findViewById(R.id.time);
        //set the switch to ON
        mySwitch.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    timing.setVisibility(View.VISIBLE);
                    time.requestFocus();
                } else {
                    timing.setVisibility(View.GONE);
                }
            }
        });
        //check the current state before we display the screen
        if (mySwitch.isChecked()) {
            timing.setVisibility(View.VISIBLE);
            time.requestFocus();
        } else {
            timing.setVisibility(View.GONE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_group) {

            if (group_name.getText().toString().trim().length() > 0) {
                Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);
                startActivity(intent);
                NewGroupActivity.this.finish();
            } else {
                Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (0) : {
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<String> selectedContacts = data.getStringArrayListExtra("selectedContacts");
                    for(String s:selectedContacts ){
                        Log.d("recei", s);
                    }
                    // TODO Update your TextView.
                }
                break;
            }
        }
    }
}



//
////TODO: integrate messageBundle sending
//MessageBundle createRoomBundle = new MessageBundle(client.getID(), client.getSESSION_ID(),
//        MessageBundle.messageType.CREATE_ROOM);
//
//createRoomBundle.putUsers(userArray);
//        createRoomBundle.putChatroomName(chatName);
//        createRoomBundle.putExpiry(unit, duration);
//        Intent i = new Intent(getApplicationContext(), NetworkService.class);
//        i.putExtra(NetworkService.MESSAGE_KEY,
//        JsonWriter.objectToJson(createRoomBundleBundle.getMessage()));
//
//        this.startService(i);
