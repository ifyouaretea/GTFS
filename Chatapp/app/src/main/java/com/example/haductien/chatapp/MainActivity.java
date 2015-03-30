package com.example.haductien.chatapp;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity  extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(this,
                R.layout.main_list_item,
                null,
                new String[]{DataProvider.COL_NAME, DataProvider.COL_COUNT},
                new int[]{R.id.text1, R.id.text2},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch(view.getId()) {
                    case R.id.text2:
                        int count = cursor.getInt(columnIndex);
                        if (count > 0) {
                            ((TextView)view).setText(String.format("%d new message%s", count, count==1 ? "" : "s"));
                        }
                        return true;
                }
                return false;
            }
        });
        setListAdapter(adapter);

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.mipmap.ic_launcher);
        getActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_main);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this,
                DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_COUNT},
                null,
                null,
                DataProvider.COL_ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
                loadData(query);
                return true;
            }
        });

        return true;
    }

    private void loadData(String query) {
        ArrayList<String> chats = new ArrayList<String>();
        //TODO: Remove hardcoding
        chats.add("Nikhil");
        chats.add("HaoQin");
        chats.add("KangSheng");
        // Load data from list to cursor
        String[] columns = new String[]{"_id", "text"};
        Object[] temp = new Object[]{0, "default"};

        MatrixCursor cursor = new MatrixCursor(columns);

        for (int i = 0; i < chats.size(); i++) {

            temp[0] = i;
            temp[1] = chats.get(i);

            cursor.addRow(temp);

        }

        // Alternatively load data from database
        //Cursor cursor = db.rawQuery("SELECT * FROM table_name", null);

//        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

//        SearchView search = (SearchView) menu_main.findItem((R.id.action_search).getActionView());
//        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

//        search.setSuggestionsAdapter(new ExampleAdapter(this, cursor, chats));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search:
                //    openSearch();
                return true;
            case R.id.action_contacts:
                AddContactDialog dialog = new AddContactDialog();
                dialog.show(getFragmentManager(), "AddContactDialog");
                return true;
            case R.id.action_chat:
                //   openSettings();
                return true;
            case R.id.action_group:
                //   openSettings();
                return true;
            case R.id.action_profile:
                //   openSettings();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
