package com.gfts.testchat;

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

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SendActivity extends ActionBarActivity {

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
                Thread networkThread = new Thread() {
                    @Override
                    public void run(){

                        final String hostname = "localhost"; //= "128.199.73.51";
                        final int hostport = 8091;
                        final String ownID = "12345";
                        Log.d("Success!!!!!", "Thread created");

                        ServerSocket server = null;
                        try {
                            server = new ServerSocket(hostport);
                            Socket client = new Socket(hostname, hostport);

                            Socket serverSideClient = server.accept();

                            JsonWriter jOut = new JsonWriter(client.getOutputStream());
                            JsonReader jIn = new JsonReader(serverSideClient.getInputStream());


                            jOut.write(new MessageBundle(ownID, mMessageBody.getText().toString(), MessageBundle.messageType.TEXT));
                            jOut.flush();

                            mMessage = ((MessageBundle) jIn.readObject()).getMessage();
                            Log.d("Success!!!!!", mMessage);
                            server.close();
                            client.close();
                            serverSideClient.close();

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
                mMessageDisplay.setText(mMessage);
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
