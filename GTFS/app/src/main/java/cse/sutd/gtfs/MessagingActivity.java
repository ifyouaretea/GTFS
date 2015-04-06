package cse.sutd.gtfs;

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
        listview = (ListView) findViewById(R.id.messageList);
        MessageBundle hi = new MessageBundle("1234", "hi Nikhil!", MessageBundle.messageType.TEXT, true);
        MessageBundle hi1 = new MessageBundle("1234", "Beer Tonight! On?", MessageBundle.messageType.TEXT, false);
        final ArrayList<MessageBundle> message = new ArrayList<MessageBundle>();
        message.add(hi);
        message.add(hi1);

        populateMsgs(message);
        msg = (TextView)findViewById(R.id.message);
        send = (Button)findViewById(R.id.sendMessageButton);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "TODO: Implement sending", Toast.LENGTH_LONG).show();
                message.add(new MessageBundle("1234", msg.getText().toString(), MessageBundle.messageType.TEXT, true));
                populateMsgs(message);
                msg.setText("");
            }
        });
    }
    public void populateMsgs(ArrayList<MessageBundle> message){
        final MessageAdapter adapter = new MessageAdapter(this, message);
        //adapter.notifyDataSetChanged();
        listview.setAdapter(adapter);
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
