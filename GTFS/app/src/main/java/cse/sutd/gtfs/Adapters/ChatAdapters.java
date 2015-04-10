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

import cse.sutd.gtfs.Objects.ChatRooms;
import cse.sutd.gtfs.R;

public class ChatAdapters extends ArrayAdapter<ChatRooms> {

    private final Context context;
    private final ArrayList<ChatRooms> values;

    public ChatAdapters(Context context, ArrayList<ChatRooms> values) {
        super(context, R.layout.main_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView chatName = (TextView) rowView.findViewById(R.id.firstLine);
        TextView latestmsg = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView avatar = (ImageView) rowView.findViewById(R.id.avatar);
        chatName.setText(values.get(position).getName());
        avatar.setImageResource(R.drawable.ic_action_dark_profile);

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
