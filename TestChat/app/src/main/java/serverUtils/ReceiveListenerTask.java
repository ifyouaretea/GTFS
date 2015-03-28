package serverUtils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import serverUtils.MessageBundle.messageType;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.gfts.testchat.SendActivity;

//TODO: put into receiveThread class
/**
 * Listens continuously for a connection from the server.
 *
 */
public class ReceiveListenerTask extends AsyncTask<Void, Void, String>{
//	public static final String hostname = "128.199.73.51";

    public static final String hostname = "localhost";
    public static final int hostport = 8090;

    private TextView mTextView;
//	public static final String ownID;
//
//	public static MessageBundle RECEIVED = new MessageBundle(ownID, "", messageType.CLIENT_RECEIVED);

    public ReceiveListenerTask(TextView mTextView){
        this.mTextView = mTextView;
    }
	protected String doInBackground(Void... params){
		while(true){
			try{
				Socket serverConnection = new Socket(hostname, hostport);
                serverConnection.setSoTimeout(10000);
                Log.d("Receive client", "Successful");
				JsonReader serverIn = new JsonReader(serverConnection.getInputStream());
				MessageBundle message = (MessageBundle) serverIn.readObject();
				
				JsonWriter serverOut = new JsonWriter(serverConnection.getOutputStream());
//				serverOut.write(ReceiveListenerThread.RECEIVED);
				

				serverConnection.close();
				serverIn.close();
				serverOut.close();
				
				switch(message.getType()){
				//TODO: implement handlers for the different message types
				case CLIENT_RECEIVED:
					break;
				case EDIT_NOTE:
					break;
				case FETCH_NOTE:
					break;
				case INVITE:
					break;
				case NEW_MESSAGE:
                    break;
				case TEXT:
                    Log.d("Received message", message.getMessage());
                    return message.getMessage();
				case TYPING:
					break;
				default:
					break;
					
				}

			}catch (IOException e){
				//e.printStackTrace();
			}
		}
	}

    protected void onPostExecute(String result) {
        mTextView.setText(result);
    }
}

