package com.example.haductien.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends Activity {
    private static final String TWITTER_KEY = "lgj3wZm2PTAYugiOWZddMS1Rv";
    private static final String TWITTER_SECRET = "JwWBe8KKddQdd451UE7SXpqsVdPSPdFnfjYHTJnVTgKXHGfxer";
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    private DigitsAuthButton digitsButton;
    private MyApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = MyApplication.getInstance();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);
        setUpDigitsButton();
    }

    private void setUpDigitsButton() {

        digitsButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        digitsButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
//                app.setNumber(phoneNumber);
                Log.d("TAG",phoneNumber);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_MESSAGE, phoneNumber);
                startActivity(intent);
            }

            @Override
            public void failure(DigitsException exception) {
                Toast.makeText(getApplicationContext(), "Wrong Number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onResume(){
//        if(app.getInstance().contains("number")){
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//        }
        super.onResume();
    }
}