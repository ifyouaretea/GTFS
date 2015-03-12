package com.example.haductien.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private EditText hpNum;
//    private DigitsAuthButton digitsButton;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpDigitsButton();
    }

    private void setUpDigitsButton() {

        hpNum = (EditText)findViewById(R.id.phone);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(hpNum.getText().length()<8 && (hpNum.getText().charAt(0)!='8'||hpNum.getText().charAt(0)!='9'))
                    Toast.makeText(getBaseContext(), "Please Enter a valid number", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    //WE'LL USE THIS ONCE TWITTER ALLOWS US TO LOGIN. NEED TO WAIT A WHILE... :P
//        digitsButton =
//                (DigitsAuthButton) findViewById(R.id.auth_button);
//        digitsButton.setOnClickListener();
//        digitsButton.setCallback(new AuthCallback() {
//
//            @Override
//            public void success(DigitsSession digitsSession, String phoneNumber) {
//                hpNum.setText(phoneNumber);
//            }
//
//            @Override
//            public void failure(DigitsException e) {
//                Toast.makeText(getApplicationContext(),"Wrong Number",Toast.LENGTH_SHORT).show();
//            }
//        });
    }


}