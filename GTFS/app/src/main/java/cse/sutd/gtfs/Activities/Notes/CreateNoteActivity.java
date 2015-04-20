package cse.sutd.gtfs.Activities.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cedarsoftware.util.io.JsonWriter;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

public class CreateNoteActivity extends ActionBarActivity {

    private Button confirmButton;
    private EditText titleCreate;
    private EditText bodyCreate;
    private GTFSClient client;
    private String chatroomID;

    public static final String CHAT_ID_KEY = "chatID";
    /**
     * Required extras:
     * String chatID: id of the chat the new note belongs to
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = (GTFSClient) getApplication();
        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Create Note");
        setContentView(R.layout.activity_create_note);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            chatroomID = extras.getString(CHAT_ID_KEY);
        }


        bodyCreate = (EditText) findViewById(R.id.create_body);
        titleCreate = (EditText) findViewById(R.id.create_title);

        confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBundle createNoteBundle = new MessageBundle(client.getID(),
                        client.getSESSION_ID(),MessageBundle.messageType.CREATE_NOTE);
                createNoteBundle.putNoteText(bodyCreate.getText().toString());
                createNoteBundle.putNoteTitle(titleCreate.getText().toString());
                createNoteBundle.putChatroomID(chatroomID);

                String jsonMessage = JsonWriter.objectToJson(createNoteBundle.getMessage());
                Intent sendMessageIntent = new Intent(CreateNoteActivity.this, NetworkService.class);
                sendMessageIntent.putExtra(NetworkService.MESSAGE_KEY, jsonMessage);
                CreateNoteActivity.this.startService(sendMessageIntent);
                CreateNoteActivity.this.finish();
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_note, menu);
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
