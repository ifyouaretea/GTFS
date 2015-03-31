package cse.sutd.gtfs.Adapters;

/**
 * Created by Francisco Furtado on 31/03/2015.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.R;

public class ChatAdapters extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    public ChatAdapters(Context context, ArrayList<String> values) {
        super(context, R.layout.chatrow, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.chatrow, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        textView.setText(values.get(position));
        // change the icon for Windows and iPhone
        String s = values.get(position);
        imageView.setImageResource(R.drawable.ic_action_profile);

        return rowView;
    }
    @Override
    public long getItemId(int position) {
        return values.indexOf(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
