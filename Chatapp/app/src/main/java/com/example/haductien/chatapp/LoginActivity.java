package com.example.haductien.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;


public class LoginActivity extends Activity {
    private EditText hpNum;
    private DigitsAuthButton digitsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpDigitsButton();
    }

    private void setUpDigitsButton() {
        digitsButton =
                (DigitsAuthButton) findViewById(R.id.auth_button);
        hpNum = (EditText)findViewById(R.id.hpNum);
        digitsButton.setCallback(new AuthCallback() {

            @Override
            public void success(DigitsSession digitsSession, String phoneNumber) {
                hpNum.setText(phoneNumber);
            }

            @Override
            public void failure(DigitsException e) {
                Toast.makeText(getApplicationContext(),"Wrong Number",Toast.LENGTH_SHORT).show();
            }
        });
    }
}