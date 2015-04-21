package cse.sutd.gtfs.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
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
    private String searchTerm = "";
    private ListView target;
    private List<Integer> searchResult;
    private Set<String> filterTags;

    public List<Integer> getSearchResult() {
        return searchResult;
    }


    private final String user;
    private final int isGroup;
    private final Context context;

    private SearchFilter searchFilter;

    public MessageAdapter(Context context, ListView target, ArrayList<MessageBundle> values,
                          String user, int isGroup) {
        super(context, R.layout.message_list_item_left, values);
        this.context = context;
        this.values = values;
        this.originalValues = new ArrayList<>(values);
        this.user = user;
        this.isGroup = isGroup;
        this.target = target;
        this.searchResult = new ArrayList<>();
        this.filterTags = new HashSet<>();
//        extractTags();
    }

/*    private void extractTags(){
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
    }*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageBundle message = this.getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View rowView;
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
        String nonFormatedText = (String)values.get(position).
                getMessage().get(MessageBundle.MESSAGE);

        int searchPosition;

        if((searchPosition = nonFormatedText.toLowerCase().indexOf(searchTerm.toLowerCase())) > -1){
            Spannable formattedText = new SpannableString(nonFormatedText);
            formattedText.setSpan(new BackgroundColorSpan(Color.YELLOW),
                    searchPosition, searchPosition + searchTerm.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            msg.setText(formattedText);
        }else
                msg.setText(nonFormatedText);

        ImageView tagCircle = (ImageView) rowView.findViewById(R.id.circle);

        String tag = (String) message.getMessage().get(MessageBundle.TAGS);
        if(tag.trim().equals("important"))
            tagCircle.setColorFilter(Color.rgb(85,0,0));
        else if(tag.trim().equals("normal"))
            tagCircle.setColorFilter(Color.rgb(17, 102, 17));
        else if(tag.trim().equals("nonsense"))
            tagCircle.setColorFilter(Color.rgb(170, 147, 57));


        TextView time = (TextView) rowView.findViewById(R.id.textTime);
        time.setText((String)values.get(position).getMessage().get(MessageBundle.TIMESTAMP));

        return rowView;
    }
    private static class ViewHolder
    {
        TextView message;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
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

    public void applyTag(String tag){
        filterTags.add(tag.toLowerCase());
        getFilter().filter(filterTags.toString());
    }

    public void removeTag(String tag){
        filterTags.remove(tag.toLowerCase());
        getFilter().filter(filterTags.toString());
    }
    @Override
    public void notifyDataSetChanged() {
        searchResult.clear();
        for(int i = 0; i < values.size(); i++){
            if(((String) values.get(i).getMessage().get(MessageBundle.MESSAGE)).toLowerCase().
                    contains(searchTerm.toLowerCase()))
            searchResult.add(i);
        }
        super.notifyDataSetChanged();
        if(originalValues.size() < values.size())
            originalValues = new ArrayList<>(values);
    }

    private class SearchFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence searchedTags) {

            FilterResults results = new FilterResults();
            String[] searchedTagArray = searchedTags.toString().replaceAll("\\[", "")
                    .replaceAll("\\]", "").split(",");

            if (searchedTagArray == null || searchedTagArray.length == 0 ||
                    "[]".equals(searchedTags.toString())) {
//                ArrayList<MessageBundle> list = new ArrayList<>(originalValues);
                results.values = originalValues;
                results.count = originalValues.size();
            } else {
                final ArrayList<MessageBundle> newValues = new ArrayList<>();
                for (MessageBundle messageBundle : originalValues) {
                    String messageTag = (String)messageBundle.getMessage().
                            get(MessageBundle.TAGS);
                        for(String searchedTag : searchedTagArray) {
                            if (messageTag.toLowerCase().trim().equals(searchedTag.trim())) {
                                newValues.add(messageBundle);
                                break;
                            }
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

