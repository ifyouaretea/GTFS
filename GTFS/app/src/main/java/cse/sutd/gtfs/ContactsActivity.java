package cse.sutd.gtfs;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;

import cse.sutd.gtfs.Adapters.ContactAdapter;
import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;


public class ContactsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Contacts");
        setContentView(R.layout.activity_contacts);

        GTFSClient client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);
        String sessionToken = client.getSESSION_ID();
        MessageDbAdapter  dbMessages = MessageDbAdapter.getInstance(this);

        ListView listview = (ListView) findViewById(R.id.contactList);
        final ArrayList<Contact> contacts = new ArrayList<Contact>();
        Cursor contactBundles = dbMessages.getContacts();
        if (contactBundles != null) {
            if(contactBundles.getCount()>0) {
                contactBundles.moveToFirst();
                do {
                    Contact a = new Contact(contactBundles.getString(0));
                    contacts.add(a);
                } while (contactBundles.moveToNext());
                contactBundles.close();
            }
        }
        ContactAdapter cntcts = new ContactAdapter(this, contacts);

        listview.setAdapter(cntcts);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = ((Contact) parent.getItemAtPosition(position)).getNumber();
                //TODO: check if chatroom exist
                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                i.putExtra("toNumber", contacts.get(position).getNumber());
                startActivity(i);
            }
        });

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
       switch(item.getItemId()){

           case android.R.id.home:
               NavUtils.navigateUpFromSameTask(this);
               return true;
       }

        return super.onOptionsItemSelected(item);
    }
}
