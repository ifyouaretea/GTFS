package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cse.sutd.gtfs.Objects.Contact;
import cse.sutd.gtfs.R;

/**
 * Created by Francisco Furtado on 16/04/2015.
 */
public class ContactAdapter extends ArrayAdapter<Contact>{
    private final Context context;
    private final ArrayList<Contact> values;

    public ContactAdapter(Context context, ArrayList<Contact> values) {
        super(context, R.layout.contact_list_item, values);
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
        chatName.setText(values.get(position).getNumber());
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
