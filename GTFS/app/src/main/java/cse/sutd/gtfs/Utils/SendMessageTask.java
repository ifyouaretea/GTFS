package cse.sutd.gtfs.Utils;

/**
 * Created by Francisco Furtado on 27/03/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.cedarsoftware.util.io.JsonWriter;

import java.io.OutputStream;

public class SendMessageTask extends AsyncTask<MessageBundle, Void, Boolean>{

    private final OutputStream mOutputStream;

    public SendMessageTask(OutputStream outputStream){
        mOutputStream = outputStream;
    }
    @Override
    protected Boolean doInBackground(MessageBundle[] params) {
        Log.d("Sender", "Successful");

        JsonWriter serverOut = new JsonWriter(mOutputStream);

        for(MessageBundle message : params) {
            serverOut.write(message);
            serverOut.flush();
        }

        return true;
    }
}
