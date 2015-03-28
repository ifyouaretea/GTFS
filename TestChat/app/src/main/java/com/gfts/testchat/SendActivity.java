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

import serverUtils.*;


public class SendActivity extends ActionBarActivity {

    //TODO: Replace debugging values
    private final String hostname = "localhost"; //= "128.199.73.51";
    private final int sendPort = 8091;
    private final int receivePort = 8090;
    private final String ownID = "12345";

    private EditText mMessageBody;
    private TextView mMessageDisplay;
    private String mMessage;

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

                final MessageBundle messageBundle = new MessageBundle(ownID, mMessageBody.getText().toString(), MessageBundle.messageType.TEXT);


                Thread networkThread = new Thread() {
                    @Override
                    public void run(){
                        try {
                            ServerSocket sendServer = new ServerSocket(sendPort);
                            ServerSocket receiveServer = new ServerSocket(receivePort);

                            new SendMessageTask().execute(messageBundle);
                            Socket sendClient = sendServer.accept();

                            new ReceiveListenerTask(mMessageDisplay).execute();
                            Socket receiveClient = receiveServer.accept();

                            JsonWriter jOut = new JsonWriter(receiveClient.getOutputStream());
                            JsonReader jIn = new JsonReader(sendClient.getInputStream());

                            MessageBundle msg = (MessageBundle) jIn.readObject();
                            jOut.write(msg);
                            jOut.flush();

                            Log.d("Success!!!!!", "yay");
                            Log.d("Great success", msg.getMessage());
                            sendClient.close();
                            sendServer.close();
                            receiveClient.close();
                            receiveServer.close();

                        } catch (IOException e) {
                            Log.d("FAILURE", e.getMessage());
                        }

                    }
                    };
                    networkThread.start();
                try {
                    networkThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                };
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
