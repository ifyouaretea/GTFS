package cse.sutd.gtfs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;

import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;


public class AddContactToGroup extends ActionBarActivity implements View.OnClickListener {
    private AddContactGroupAdapter addToGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_to_group);
        ArrayList<Contact> selected = new ArrayList<Contact>();

        final MessageDbAdapter dbMessages = MessageDbAdapter.getInstance(this);
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            StringBuffer responseText = new StringBuffer();
            responseText.append("The following were selected...n");

            ArrayList<Contact> contacts = addToGroup.values;
            ArrayList<Contact> selected = new ArrayList<Contact>();
            for(int i=0;i<contacts.size();i++){
                Contact contact = contacts.get(i);
                if(contact.isSelected()){
                    selected.add(contact);
                }
            }
            String[] selectedContacts = new String[selected.size()];
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedContacts", selectedContacts);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

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
            ViewHolder holder = null;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.add_contact_group_item, parent, false);

            holder = new ViewHolder();
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



