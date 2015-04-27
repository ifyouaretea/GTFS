package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.Note;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.MessageManagement.MessageDbAdapter;

/**
 * Created by tes on 19/04/2015.
 */
public class NoteAdapter extends ArrayAdapter<Note> {

    private final Context context;
    private final ArrayList<Note> values;
    private MessageDbAdapter dbAdapter;

    public NoteAdapter(Context context, ArrayList<Note> values) {
        super(context, R.layout.main_list_item, values);
        this.context = context;
        this.values = values;
        this.dbAdapter = ((GTFSClient) context.getApplicationContext()).getDatabaseAdapter();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.note_list_item, parent, false);

        TextView noteTitle = (TextView) rowView.findViewById(R.id.noteTitle);
        noteTitle.setText(values.get(position).getTitle());
        return rowView;
    }
}
