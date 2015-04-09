package cse.sutd.gtfs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.matesnetwork.callverification.Cognalys;
import com.matesnetwork.interfaces.VerificationListner;

import java.util.ArrayList;
import java.util.UUID;


public class LoginActivityCog extends Activity {
    private EditText phoneNumbTv = null;
    private GTFSClient client;
    private UUID sessionID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        sessionID = UUID.randomUUID();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        String userID = prefs.getString("userid", null);
        ((GTFSClient)getApplication()).setID(userID);
        if (userID != null) {
            Intent intent = new Intent(LoginActivityCog.this, MainActivity.class);
            intent.putExtra("SessionID", sessionID);
            Log.d("TAG", sessionID.toString());
            startActivity(intent);
            LoginActivityCog.this.finish();
        } else {
            setContentView(R.layout.activity_login_activity_cog);
            phoneNumbTv = (EditText) findViewById(R.id.ph_et);
            final TextView country_code_tv = (TextView) findViewById(R.id.country_code_tv);
            country_code_tv.setText(Cognalys.getCountryCode(getApplicationContext()));
            String number;
            findViewById(R.id.verifybutton).setOnClickListener(
                    new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (!TextUtils.isEmpty(phoneNumbTv.getText().toString())) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//                                verify();
                                String number = phoneNumbTv.getText().toString();
                                client.setID(number);
                                SharedPreferences.Editor editor = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE).edit();
                                editor.putString("userid", number);//3128869026
                                editor.commit();

                                Intent intent = new Intent(LoginActivityCog.this, ProfileActivity.class);
                                intent.putExtra("SessionID", sessionID);

                                Log.d("TAG", sessionID.toString());
                                startActivity(intent);
                                LoginActivityCog.this.finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter your phone number to verify", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }

    private void verify() {

        Cognalys.verifyMobileNumber(LoginActivityCog.this, "5f66dbb2d2e27560e9ea56a40 ", "768aa61cc5bf48090a4acb713", phoneNumbTv.getText().toString(), new VerificationListner() {

            @Override
            public void onVerificationStarted() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), "Your number has been verified\nThanks!!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onVerificationSuccess() {
                Toast.makeText(getApplicationContext(), "Your number has been verified\nThanks!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(ArrayList<String> errorList) {
                for (String error : errorList) {
                    Log.d("abx", "error:" + error);
                }
                Toast.makeText(getApplicationContext(), "Something went wrong.\n please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}