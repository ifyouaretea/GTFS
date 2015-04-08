package serverUtils;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Listens continuously for a connection from the server.
 *
 */
@Deprecated
public class ReceiveListenerTask extends AsyncTask<Void, Void, String>{
	public static final String hostname = "128.199.73.51";
    public static final int hostport = 8091;

    private TextView mTextView;
//	public static final String ownID;
//
//	public static MessageBundle RECEIVED = new MessageBundle(ownID, "", messageType.CLIENT_RECEIVED);

    public ReceiveListenerTask(TextView textView){
        this.mTextView = textView;
    }
	protected String doInBackground(Void... params){
        String inMsg = "fail";
        while(true){

            List<Character> myMsgBuffer = new ArrayList<>();

            try{
                Log.d("inMsg", myMsgBuffer.toString());
				Socket serverConnection = new Socket(hostname, hostport);
                serverConnection.setSoTimeout(10000);
                InputStream serverStream = serverConnection.
                        getInputStream();
                BufferedReader serverIn = new BufferedReader(new InputStreamReader(serverStream));

                //JsonReader serverIn = new JsonReader(serverConnection.getInputStream());
				//MessageBundle message = (MessageBundle) serverIn.readObject();

                int i = 0;
                while((i = serverIn.read()) >= 0) {
                    myMsgBuffer.add((char) i);
                    Log.d("msgBuffer", myMsgBuffer.toString());
                }


                //String msg = serverIn.readLine();

				JsonWriter serverOut = new JsonWriter(serverConnection.getOutputStream());
//				serverOut.write(ReceiveListenerThread.RECEIVED);

				serverConnection.close();
				serverIn.close();
				serverOut.close();

//				switch(message.getType()){
//				//TODO: implement handlers for the different message types
//                    default:
//                        Log.d("Received message", message.getMessage());
//                        return message.getMessage();
//				}

			}catch (IOException e){
                e.printStackTrace();
//                char[] msgBuffer2 = new char[msgBuffer.size()];
//                for(int j = 0; j < msgBuffer.size(); j++)
//                    msgBuffer2[j] = msgBuffer.get(j);
                Log.d("Before string", myMsgBuffer.toString());
                StringBuilder sb = new StringBuilder();
                for(char c : myMsgBuffer)
                    sb.append(c);
                Log.d("After String", sb.toString());
                //Log.d("Receiving failed", e.toString());
				//e.printStackTrace();
                break;
			}
		}
        return null;
	}

    protected void onPostExecute(String result) {
        mTextView.setText(result);
    }
}

