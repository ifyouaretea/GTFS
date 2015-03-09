package com.example.haductien.chatapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        SharedPreferences prefs;
        ListView list;
        Button star,addContact, PM, settings;
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            prefs = getActivity().getSharedPreferences("Chat", 0);
            list = (ListView)rootView.findViewById(R.id.listView);
            star = (Button)rootView.findViewById(R.id.star);
            addContact = (Button)rootView.findViewById(R.id.addContact);
            PM = (Button)rootView.findViewById(R.id.PM);
            settings = (Button)rootView.findViewById(R.id.settings);
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new  Logout().execute();
                }
            });
            addContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new  Logout().execute();
                }
            });
            PM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new  Logout().execute();
                }
            });
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    new  Logout().execute();
                }
            });

//            new Load().execute();
            return rootView;
        }
    }
}
