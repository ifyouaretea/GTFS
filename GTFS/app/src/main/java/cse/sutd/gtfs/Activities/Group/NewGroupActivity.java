package cse.sutd.gtfs.Activities.Group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;

public class NewGroupActivity extends ActionBarActivity {

    private GTFSClient client;
    private SharedPreferences.Editor editor;

    private EditText group_name;
    private Switch timedGroup;
    private EditText time;
    private Spinner timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("New Group");
        setContentView(R.layout.activity_new_group);

        group_name = (EditText) findViewById(R.id.group_name);
        group_name.requestFocus();
        final TextView countTextView = (TextView) findViewById(R.id.countTextView);
        countTextView.setText("25");
        group_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft) {
            }

            @Override
            public void afterTextChanged(Editable group_name) {
                countTextView.setText(Integer.toString(25 - group_name.toString().length()));
            }
        });

        timer = (Spinner) findViewById(R.id.timer);


        final LinearLayout timing = (LinearLayout) findViewById(R.id.timing);
        time = (EditText) findViewById(R.id.time);

        timedGroup = (Switch) findViewById(R.id.switch1);
        timedGroup.setChecked(false);
        timedGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    timing.setVisibility(View.VISIBLE);
                    time.requestFocus();
                } else {
                    timing.setVisibility(View.GONE);
                }
            }
        });
        if (timedGroup.isChecked()) {
            timing.setVisibility(View.VISIBLE);
            time.requestFocus();
        } else {
            timing.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_create_group:
                if (group_name.getText().toString().trim().length() > 0) {
                    if((timedGroup.isChecked()&&time.getText().toString().trim().length()>0)||!timedGroup.isChecked()) {
                        Intent intent = new Intent(NewGroupActivity.this, AddContactToGroup.class);
                        String timeunit;
                        int TTL;
                        if(time.getText().toString().length()>0) {
                            if (String.valueOf(timer.getSelectedItem()).equals("WEEKS")) {
                                timeunit = "DAYS";
                                TTL = Integer.valueOf(time.getText().toString()) * 7;
                            } else if (String.valueOf(timer.getSelectedItem()).equals("MONTHS")) {
                                timeunit = "DAYS";
                                TTL = Integer.valueOf(time.getText().toString()) * 30;
                            } else {
                                timeunit = String.valueOf(timer.getSelectedItem());
                                TTL = Integer.valueOf(time.getText().toString());
                            }
                        }else{
                            timeunit = String.valueOf(timer.getSelectedItem());
                            TTL = 0;
                        }
                        String[] extra = {group_name.getText().toString(), String.valueOf(timedGroup.isChecked()), String.valueOf(TTL), timeunit};
                        //{groupname,timedgroup,time,unit}
                        Log.d("group info", Arrays.toString(extra));
                        intent.putExtra("extras", extra);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Please enter valid time", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
