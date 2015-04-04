package serverUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
