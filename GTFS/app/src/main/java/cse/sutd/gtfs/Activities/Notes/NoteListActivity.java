package cse.sutd.gtfs.Activities.Notes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Adapters.NoteAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Note;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.MessageManagement.ManagerService;
import cse.sutd.gtfs.MessageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;

public class NoteListActivity extends ActionBarActivity {

    private ListView listView;
    private String chatID;
    private MessageDbAdapter dbAdapter;
    private GTFSClient client;
    private ArrayList<Note> noteList;
    NoteAdapter noteListAdapter;

    public static final String CHAT_ID_KEY = "chatID";
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
        getSupportActionBar().setTitle("Notes");
        setContentView(R.layout.activity_note_list);
        Bundle extras = getIntent().getExtras();

        if(extras != null){
            chatID = extras.getString(CHAT_ID_KEY);
        }

        noteList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.noteList);

        dbAdapter = client.getDatabaseAdapter();
        noteListAdapter = new NoteAdapter(this, noteList);

        fetchNotesFromServer();

        IntentFilter receivedIntentFilter = new IntentFilter(ManagerService.UPDATE_UI);
        MessageBroadcastReceiver broadcastReceiver = new MessageBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, receivedIntentFilter);

        listView.setAdapter(noteListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String noteID = ((Note) parent.getItemAtPosition(position)).getId();
                Intent i = new Intent(NoteListActivity.this, EditNoteActivity.class);
                i.putExtra(EditNoteActivity.NOTE_ID_KEY, noteID);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchNotesFromServer();
    }

    private void fetchNotesFromServer(){
        MessageBundle createNoteBundle = new MessageBundle(client.getID(),
                client.getSESSION_ID(),MessageBundle.messageType.GET_NOTES);
        createNoteBundle.putChatroomID(chatID);

        String jsonMessage = JsonWriter.objectToJson(createNoteBundle.getMessage());
        Intent sendMessageIntent = new Intent(this, NetworkService.class);
        sendMessageIntent.putExtra(NetworkService.MESSAGE_KEY, jsonMessage);
        this.startService(sendMessageIntent);
        updateUI();
    }

    private void updateUI(){
        noteList.clear();
        Cursor detailsCursor = dbAdapter.getNoteIDTitleBody(chatID);

        if(detailsCursor == null)
            return;
        if (detailsCursor.getCount() < 1) {
            detailsCursor.close();
            return;
        }
        detailsCursor.moveToFirst();
        do {
            noteList.add(new Note(detailsCursor.getString(0), detailsCursor.getString(1),
                    detailsCursor.getString(2)));
        }while (detailsCursor.moveToNext());
        noteListAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.create_note:
                Intent intent = new Intent(this, CreateNoteActivity.class);
                intent.putExtra(CHAT_ID_KEY, chatID);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

}
