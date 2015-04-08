package cse.sutd.gtfs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;

import java.util.UUID;

public class LoginActivity extends Activity {

    private GTFSClient client;
    private UUID sessionID;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
//    private final String TWITTER_KEY = "6Rs5gyo7xHoEYYkls0ajWP9PO";
//    private final String TWITTER_SECRET = "8nvcBPCoqhkt1Lvzjv6Pb5GmBB4uBmreV3KSgVxfcgCJrMQT8E";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = (GTFSClient)getApplicationContext();
        sessionID = UUID.randomUUID();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        String userID = prefs.getString("userid", null);
        if (userID != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("SessionID", sessionID);
            Log.d("TAG", sessionID.toString());
            startActivity(intent);
            LoginActivity.this.finish();
        }else{
//            client.setID("3128869026");
//            SharedPreferences.Editor editor = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE).edit();
//            editor.putString("userid", "3128869026");//3128869026
//            editor.commit();
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            intent.putExtra("SessionID", sessionID);
//            Log.d("TAG", sessionID.toString());
//            startActivity(intent);
//            LoginActivity.this.finish();
              login();
        }

    }

    private void login(){

        DigitsAuthButton digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {

            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                String userid = String.valueOf(session.getId());
                client.setID(userid);
                SharedPreferences.Editor editor = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("userid", userid);//3128869026
                editor.commit();
                intent.putExtra("userid", userid);
                intent.putExtra("SessionID", sessionID);
                Log.d("TAG", userid);
                Log.d("TAG", sessionID.toString());
                startActivity(intent);
                LoginActivity.this.finish();
            }
            @Override
            public void failure(DigitsException exception) {
                Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_LONG).show();
            }
        });
    }
}
