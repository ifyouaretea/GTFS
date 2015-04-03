package com.gfts.testchat;

import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import serverUtils.*;


public class SendActivity extends ActionBarActivity {

    //TODO: Replace debugging values

    private final String ownID = "12345";

    private EditText mMessageBody;
    private TextView mMessageDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        mMessageBody = (EditText) findViewById(R.id.editBody);
        mMessageDisplay = (TextView) findViewById(R.id.receivedMessage);

        final NetworkThread networkThread = new NetworkThread();
        networkThread.start();

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final MessageBundle textBundle = new MessageBundle("82238071", "asdsd",
                    MessageBundle.messageType.TEXT);

                textBundle.putMessage("HI BRAH");
                textBundle.putToPhoneNumber("82238071");
                textBundle.putChatroomID("12345");

                networkThread.addMessageToQueue(textBundle.getMessage());

                Map received = null;

                while(received == null){
                    received = networkThread.getMessage();
                }

                Log.d("Main thread received", received.toString());
                mMessageDisplay.setText(received.toString());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
