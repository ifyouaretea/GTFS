package cse.sutd.gtfs.Activities.Notes;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.Adapters.NoteAdapter;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.Objects.Note;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;

public class NoteListActivity extends ActionBarActivity {

    private ListView listView;
    private String chatID;
    private MessageDbAdapter dbAdapter;
    private GTFSClient client;
    private ArrayList<Note> noteList;

    public static final String CHAT_ID_KEY = "chatID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Bundle extras = getIntent().getExtras();

        if(extras != null){
            chatID = extras.getString(CHAT_ID_KEY);
        }
        client = (GTFSClient) getApplication();
        dbAdapter = client.getDatabaseAdapter();

        Cursor detailsCursor = dbAdapter.getNoteIDTitleBody(chatID);
        noteList = new ArrayList<>();
        if(detailsCursor == null)
            return;
        if (detailsCursor.getCount() < 1) {
            detailsCursor.close();
            return;
        }
        do {
            noteList.add(new Note(detailsCursor.getString(0), detailsCursor.getString(1),
                    detailsCursor.getString(2)));
        }while (!detailsCursor.moveToNext());

        NoteAdapter noteListAdapter = new NoteAdapter(this, noteList);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
