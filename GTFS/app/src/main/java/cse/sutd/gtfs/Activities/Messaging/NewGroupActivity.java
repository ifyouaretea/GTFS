package cse.sutd.gtfs.Activities.Messaging;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD:GTFS/app/src/main/java/cse/sutd/gtfs/NewGroupActivity.java
=======
import java.util.ArrayList;

import cse.sutd.gtfs.Adapters.ContactAdapter;
import cse.sutd.gtfs.AddContactToGroup;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;

>>>>>>> 99720bbe866a0bc16eaab8aebf014ae1c31fdd47:GTFS/app/src/main/java/cse/sutd/gtfs/Activities/Messaging/NewGroupActivity.java

public class NewGroupActivity extends ActionBarActivity {

    private GTFSClient client;
    private SharedPreferences.Editor editor;

    private EditText group_name;
    private Switch timedGroup;
    private EditText time;
    private Spinner timer;
    private Switch mySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        timedGroup = (Switch) findViewById(R.id.switch1);
        timer = (Spinner) findViewById(R.id.timer);
        Button add_contact = (Button) findViewById(R.id.addContact);
        add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewGroupActivity.this, AddContactToGroup.class);
                intent.putExtra("timed",timedGroup.isChecked());
                if(timedGroup.isChecked()) {
                    intent.putExtra("time",time.getText());
                    intent.putExtra("unit",String.valueOf(timer.getSelectedItem()));
                }
                intent.putExtra("groupName",group_name.getText());
                startActivity(intent);
            }
        });

        final LinearLayout timing = (LinearLayout) findViewById(R.id.timing);
        time = (EditText) findViewById(R.id.time);

        mySwitch = (Switch) findViewById(R.id.switch1);
        mySwitch.setChecked(false);
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
        switch(item.getItemId()){
            case R.id.action_create_group:
                if (group_name.getText().toString().trim().length() > 0) {
                    if((timedGroup.isChecked()&&time.getText().toString().trim().length()>0)||!timedGroup.isChecked()) {
                        Intent intent = new Intent(NewGroupActivity.this, AddContactToGroup.class);
                        String[] extra = {group_name.getText().toString(), String.valueOf(timedGroup.isChecked()), time.getText().toString(), String.valueOf(timer.getSelectedItem())};
                        //{groupname,timedgroup,time,unit}
                        intent.putExtra("extras", extra);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Please enter valid time", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
