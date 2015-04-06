package cse.sutd.gtfs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cse.sutd.gtfs.Adapters.MessageAdapter;
import cse.sutd.gtfs.Utils.MessageBundle;


public class MessagingActivity extends ActionBarActivity {
    private ListView listview;
    private TextView msg;
    private Button send;
    private GTFSClient client;

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
        getSupportActionBar().setLogo(R.drawable.ic_action_profile); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(recei);
        setContentView(R.layout.activity_messaging);

        client = (GTFSClient)getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final String userID = prefs.getString("userid", null);

        listview = (ListView) findViewById(R.id.messageList);
        MessageBundle hi = new MessageBundle("1234","asdsd" , MessageBundle.messageType.TEXT);
        hi.putMessage("hi Nikhil!"); hi.putToPhoneNumber("3128869026"); hi.putChatroomID("12345");
        MessageBundle hi1 = new MessageBundle("3128869026","asdsd" , MessageBundle.messageType.TEXT);
        hi1.putMessage("Beer Tonight! On?"); hi1.putToPhoneNumber("1234"); hi1.putChatroomID("12345");
        final ArrayList<MessageBundle> message = new ArrayList<MessageBundle>();
        message.add(hi);
        message.add(hi1);
        final MessageAdapter adapter = new MessageAdapter(this, message, userID);
        listview.setAdapter(adapter);
        msg = (TextView)findViewById(R.id.message);
        send = (Button)findViewById(R.id.sendMessageButton);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(msg.getText().toString().trim().length()>0) {
                    final MessageBundle textBundle = new MessageBundle(userID, "asdsd",
                            MessageBundle.messageType.TEXT);

                    textBundle.putMessage(msg.getText().toString());
                    textBundle.putToPhoneNumber("82238071");
                    textBundle.putChatroomID("12345");
                    Toast.makeText(getApplicationContext(), "TODO: Implement sending", Toast.LENGTH_LONG).show();
                    message.add(textBundle);
                    adapter.notifyDataSetChanged();
                    msg.setText("");
                }
            }
        });
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
