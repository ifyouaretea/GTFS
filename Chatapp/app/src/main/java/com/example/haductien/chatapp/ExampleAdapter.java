package com.example.haductien.chatapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Francisco Furtado on 13/03/2015.
 */
public class ExampleAdapter extends CursorAdapter {

    private List items;
    private TextView text;

    public ExampleAdapter(Context context, Cursor cursor, List items) {
        super(context, cursor, false);
        this.items = items;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Show list item data from cursor
        text.setText(items.get(cursor.getPosition()).toString());

        // Alternatively show data direct from database
        //text.setText(cursor.getString(cursor.getColumnIndex("column_name")));

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_chats, parent, false);
        text = (TextView) view.findViewById(R.id.name);
        return view;

    }

}