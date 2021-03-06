package cse.sutd.gtfs.Activities.Notes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cedarsoftware.util.io.JsonWriter;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.MessageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class EditNoteActivity extends ActionBarActivity {

    private MessageDbAdapter dbAdapter;
    private Button confirmButton;
    private EditText bodyEdit;
    private GTFSClient client;
    private String noteID;

    public static final String NOTE_ID_KEY = "noteID";
    /**
     * Required extras:
     * String noteID: id of the note this belongs to
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();

        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher); //user's pic
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_edit_note);

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            noteID = extras.getString(NOTE_ID_KEY);
        }

        dbAdapter = client.getDatabaseAdapter();

        String[] titleBody = dbAdapter.getNoteTitleBody(noteID);

        getSupportActionBar().setTitle(titleBody[0]);
        bodyEdit = (EditText) findViewById(R.id.editBody);
        bodyEdit.setText(titleBody[1]);

        confirmButton = (Button) findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBundle editNoteBundle = new MessageBundle(client.getID(),
                        client.getSESSION_ID(),MessageBundle.messageType.EDIT_NOTE);
                editNoteBundle.putNoteID(noteID);
                editNoteBundle.putNoteText(bodyEdit.getText().toString());

                String jsonMessage = JsonWriter.objectToJson(editNoteBundle.getMessage());
                Intent sendMessageIntent = new Intent(EditNoteActivity.this, NetworkService.class);
                sendMessageIntent.putExtra(NetworkService.MESSAGE_KEY, jsonMessage);
                EditNoteActivity.this.startService(sendMessageIntent);
                EditNoteActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
