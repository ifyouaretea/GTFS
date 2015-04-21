package cse.sutd.gtfs.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class AddContactToGroup extends ActionBarActivity {
    private GTFSClient client;
    private SharedPreferences.Editor editor;
    private MessageDbAdapter dbMessages;

    private AddContactGroupAdapter addToGroup;
    private ArrayList<Contact> selected;
    private String[] extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Add Contacts");
        setContentView(R.layout.activity_add_contact_to_group);

        Intent intent = getIntent();
        extras = intent.getStringArrayExtra("extras");
        Log.d("group info", Arrays.toString(extras));

        dbMessages = MessageDbAdapter.getInstance(this);
        final ArrayList<Contact> contacts = new ArrayList<>();
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

        addToGroup = new AddContactGroupAdapter(this, contacts);
        ListView listview = (ListView) findViewById(R.id.contacts);
        listview.setAdapter(addToGroup);

        EditText myFilter = (EditText) findViewById(R.id.myFilter);
        myFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addToGroup.getFilter().filter(s.toString());
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String toPhoneNumber = ((Contact) parent.getItemAtPosition(position)).getNumber();
                final String contactName = ((Contact) parent.getItemAtPosition(position)).getName();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_contact_to_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_done:
                selected = new ArrayList<>();
                ArrayList<Contact> list = addToGroup.values;
                if (list.size()>0) {
                    for (Contact cc : list) {
                        if (cc.isSelected()) {
                            selected.add(cc);
                        }
                    }
                    String[] groupct = new String[selected.size()+1];
                    groupct[0] = client.getID();
                    for(int i=1;i<=selected.size();i++){
                        groupct[i]=selected.get(i-1).getNumber();
                    }
//                    String[] myArray = selected.toArray(new String[selected.size()]);
                    Log.d("users",Arrays.toString(groupct));

                    MessageBundle createBundle = new MessageBundle(client.getID(), client.getSESSION_ID(),
                            MessageBundle.messageType.CREATE_ROOM);
                    createBundle.putChatroomName(extras[0]);
                    if(!extras[1].equalsIgnoreCase("false")){
                        createBundle.putExpiry(TimeUnit.valueOf(extras[3]),Long.parseLong(extras[2]));
                    }
                    createBundle.putUsers(groupct);

                    Intent intent = new Intent(this, NetworkService.class);
                    intent.putExtra(NetworkService.MESSAGE_KEY,JsonWriter.objectToJson(createBundle.getMessage()));
                    this.startService(intent);

                    //TODO: check if chatroom exist
                    String chatID = dbMessages.getChatroomID(extras[0]);
                    Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                    i.putExtra(MessageDbAdapter.CHATID, chatID);
                    i.putExtra(MessageDbAdapter.CHATNAME, extras[0]);
                    i.putExtra(MessageDbAdapter.ISGROUP, 1);
                    startActivity(i);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Please add contacts!", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class AddContactGroupAdapter extends ArrayAdapter<Contact> {
        private final Context context;
        private final ArrayList<Contact> values;

        public AddContactGroupAdapter(Context context, ArrayList<Contact> values) {
            super(context, R.layout.add_contact_group_item, values);
            this.context = context;
            this.values = values;
        }

        private class ViewHolder {
            TextView name;
            CheckBox box;
            TextView status;
            ImageView ava;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.add_contact_group_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.contactName);
            holder.status = (TextView) rowView.findViewById(R.id.status);
            holder.box = (CheckBox) rowView.findViewById(R.id.checkBox1);
            holder.ava = (ImageView) rowView.findViewById(R.id.avatar);
            rowView.setTag(holder);

            holder.box.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Contact contact = (Contact) cb.getTag();
                    contact.setSelected(cb.isChecked());
                }
            });
            holder.box.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Contact contact = (Contact) cb.getTag();
                    contact.setSelected(cb.isChecked());
                }
            });

            Contact contact = values.get(position);
            holder.name.setText(contact.getName());
            holder.name.setText(contact.getName());
            holder.status.setText(contact.getStatus());
            holder.box.setChecked(contact.isSelected());
            holder.box.setTag(contact);

            return rowView;
        }

        @Override
        public long getItemId(int position) {
            return values.indexOf(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}



