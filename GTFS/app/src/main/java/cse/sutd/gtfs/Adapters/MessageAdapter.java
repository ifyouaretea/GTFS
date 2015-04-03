package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.R;

/**
 * Created by Francisco Furtado on 02/04/2015.
 */
public class MessageAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    public MessageAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.message_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.message_list_item, parent, false);
        TextView msg = (TextView) rowView.findViewById(R.id.textMessage);
        TextView user = (TextView) rowView.findViewById(R.id.textUser);
        msg.setText(values.get(position));
        String s = values.get(position);
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
