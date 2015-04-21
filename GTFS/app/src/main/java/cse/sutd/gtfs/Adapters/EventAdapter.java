package cse.sutd.gtfs.Adapters;

/**
 * Created by Francisco Furtado on 31/03/2015.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.Objects.ChatRoom;
import cse.sutd.gtfs.Objects.Event;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;

public class EventAdapter extends ArrayAdapter<Event> {

    private final Context context;
    private final ArrayList<Event> values;
    private MessageDbAdapter dbAdapter;
    private String chatID;
    private String chatName;

    public EventAdapter(Context context, ArrayList<Event> values,String chatID) {
        super(context, R.layout.main_list_item, values);
        this.context = context;
        this.values = values;
        this.dbAdapter = ((GTFSClient) context.getApplicationContext()).getDatabaseAdapter();
        this.chatID = chatID;
        this.chatName = dbAdapter.getUsername(chatID);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.main_list_item, parent, false);

        TextView eventName = (TextView) rowView.findViewById(R.id.firstLine);
        eventName.setText(values.get(position).getEVENT_NAME());
        TextView latestmsg = (TextView) rowView.findViewById(R.id.secondLine);
        latestmsg.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(new Date(values.get(position).getEVENT_DATE())));
        TextView unreadCount = (TextView) rowView.findViewById(R.id.unreadCount);
        unreadCount.setVisibility(View.GONE);

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
