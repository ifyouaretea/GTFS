package cse.sutd.gtfs;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import cse.sutd.gtfs.Adapters.MessageAdapter;


public class MessagingActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String recei="";
        if (extras != null) {
            recei = extras.getString("receiver");
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(recei);
        setContentView(R.layout.activity_messaging);
        final ListView listview = (ListView) findViewById(R.id.messageList);
        String[] messages = new String[] { "Hi", "Let's ", "go", "For", "Beer", "Tonight!", "OK!"};
        final ArrayList<String> message = new ArrayList<String>();
        for (int i = 0; i < messages.length; ++i) {
            message.add(messages[i]);
        }
        final MessageAdapter adapter = new MessageAdapter(this, message);
        listview.setAdapter(adapter);
//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view,
//                                    int position, long id) {
//                final String item = (String) parent.getItemAtPosition(position);
//                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
//                i.putExtra("receiver", message.get(position));
//                startActivity(i);
//            }
//
//        });
        //        adapter = new SimpleCursorAdapter(getActivity(),
//                R.layout.chat_list_item,
//                null,
//                new String[]{DataProvider.COL_MSG, DataProvider.COL_AT},
//                new int[]{R.id.text1, R.id.text2},
//                0);
//        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
//
//            @Override
//            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
//                switch(view.getId()) {
//                    case R.id.text1:
//                        LinearLayout root = (LinearLayout) view.getParent().getParent();
//                        if (cursor.getString(cursor.getColumnIndex(" "/*DataProvider.COL_FROM*/)) == null) {
//                            root.setGravity(Gravity.RIGHT);
//                            root.setPadding(50, 10, 10, 10);
//                        } else {
//                            root.setGravity(Gravity.LEFT);
//                            root.setPadding(10, 10, 50, 10);
//                        }
//                        break;
//                }
//                return false;
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_messaging, menu);
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
}
