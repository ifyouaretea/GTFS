package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.R;
import cse.sutd.gtfs.serverUtils.MessageBundle;

/**
 * Created by Francisco Furtado on 02/04/2015.
 */
public class MessageAdapter extends ArrayAdapter<MessageBundle> {

    private final Context context;
    private final ArrayList<MessageBundle> values;
    private final String user;

    public MessageAdapter(Context context, ArrayList<MessageBundle> values, String user) {
        super(context, R.layout.message_list_item_left, values);
        this.context = context;
        this.values = values;
        this.user = user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageBundle message = this.getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        if(user.equalsIgnoreCase((String)message.getMessage().get("from_phone_number"))) {
            rowView = inflater.inflate(R.layout.message_list_item_right, parent, false);
            LinearLayout lay = (LinearLayout) rowView.findViewById(R.id.userAndMessage);
            lay.setBackgroundColor(Color.GREEN);
        }
        else
            rowView = inflater.inflate(R.layout.message_list_item_left, parent, false);
        TextView msg = (TextView) rowView.findViewById(R.id.textMessage);
        TextView user = (TextView) rowView.findViewById(R.id.textUser);
        msg.setText((String)values.get(position).getMessage().get("message"));

        return rowView;
    }
    private static class ViewHolder
    {
        TextView message;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
    @Override
    public int getCount() {
        return values.size();
    }
    @Override
    public MessageBundle getItem(int position) {
        return values.get(position);
    }
}
