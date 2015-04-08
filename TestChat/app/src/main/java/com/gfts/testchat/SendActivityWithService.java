package com.gfts.testchat;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cedarsoftware.util.io.JsonWriter;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.jar.Attributes;

import serverUtils.*;


public class SendActivityWithService extends ActionBarActivity {

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

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            final MessageBundle textBundle = new MessageBundle("82238071", "asdsd",
                    MessageBundle.messageType.TEXT);

            textBundle.putMessage("HI BRAH");
            textBundle.putToPhoneNumber("81572260");
            textBundle.putChatroomID("1428400316768767");

                Intent intent = new Intent(SendActivityWithService.this, NetworkService.class);

                intent.putExtra(NetworkService.MESSAGE_KEY,
                        JsonWriter.objectToJson(textBundle.getMessage()));
                SendActivityWithService.this.startService(intent);
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
