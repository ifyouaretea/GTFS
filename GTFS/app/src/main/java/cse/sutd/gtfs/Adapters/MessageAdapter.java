package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;

/**
 * Created by Francisco Furtado on 02/04/2015.
 */
public class MessageAdapter extends ArrayAdapter<MessageBundle> implements Filterable{


    private ArrayList<MessageBundle> values;
    private ArrayList<MessageBundle> originalValues;
    private List<String> tags;
    private final String user;
    private final int isGroup;
    private final Context context;

    private SearchFilter searchFilter;
//    private SubsetFilter subsetFilter;


    public MessageAdapter(Context context, ArrayList<MessageBundle> values, String user, int isGroup) {
        super(context, R.layout.message_list_item_left, values);
        this.context = context;
        this.values = values;
        this.originalValues = new ArrayList<>(values);
        this.user = user;
        this.isGroup = isGroup;
        extractTags();
    }

    private void extractTags(){
        tags = new LinkedList<>();
        Set<String> rawTags = new HashSet<>();
        String tag;

        for(MessageBundle messageBundle: originalValues){
            tag = (String) messageBundle.getMessage().get(MessageBundle.TAGS);
            if(!rawTags.contains(tag))
                rawTags.add(tag);
        }

        for(String tagArray: rawTags){
            for (String t: tagArray.replaceAll("\\[", "").replaceAll("\\]", "").split(","))
                tags.add(t);
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageBundle message = this.getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if(user.equalsIgnoreCase((String)message.getMessage().get("from_phone_number"))) {
            rowView = inflater.inflate(R.layout.message_list_item_right, parent, false);
        }

        else {
            rowView = inflater.inflate(R.layout.message_list_item_left, parent, false);
            TextView userName = (TextView) rowView.findViewById(R.id.textUser);
            if(isGroup==0){
                userName.setVisibility(View.GONE);
            }else{
                MessageDbAdapter dbMessages = MessageDbAdapter.getInstance(context);
                Cursor contactName = dbMessages.getContact(user);
                String contact = null;
                if (contactName != null) {
                    contactName.moveToFirst();
                    while (contactName.moveToNext()) {
                        contact = contactName.getString(1);
                    }
                    contactName.close();
                    userName.setText(contact);
                }else{
                    userName.setText(user);
                }
            }
        }

        TextView msg = (TextView) rowView.findViewById(R.id.textMessage);
        msg.setText((String)values.get(position).getMessage().get(MessageBundle.MESSAGE));

        TextView time = (TextView) rowView.findViewById(R.id.textTime);
        time.setText((String)values.get(position).getMessage().get(MessageBundle.TIMESTAMP));

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

    @Override
    public Filter getFilter(){
        if(searchFilter == null)
            searchFilter = new SearchFilter();
        return searchFilter;
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        getTags();
        if(originalValues.size() < values.size())
            originalValues = new ArrayList<>(values);
    }

    public List<String> getTags(){
        return tags;
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence searchTerm) {

            FilterResults results = new FilterResults();
            if (searchTerm == null || searchTerm.length() == 0) {
//                ArrayList<MessageBundle> list = new ArrayList<>(originalValues);
                results.values = originalValues;
                results.count = originalValues.size();
            } else {
                final ArrayList<MessageBundle> newValues = new ArrayList<>();
                String lowerSearchTerm = searchTerm.toString().toLowerCase();

                for (MessageBundle messageBundle : values) {
                    final String matchText = ((String)messageBundle.getMessage().
                            get(MessageBundle.MESSAGE)).toLowerCase();

                    if (matchText.contains(lowerSearchTerm )) {
                        newValues.add(messageBundle);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,  FilterResults results) {
            if (results.count > 0) {
                values = (ArrayList<MessageBundle>) results.values;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    };
}

