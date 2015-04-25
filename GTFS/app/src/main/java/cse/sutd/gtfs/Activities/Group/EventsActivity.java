package cse.sutd.gtfs.Activities.Group;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.cedarsoftware.util.io.JsonWriter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cse.sutd.gtfs.Activities.LoginActivityCog;
import cse.sutd.gtfs.Activities.Messaging.MessagingActivity;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.messageManagement.MessageDbAdapter;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class EventsActivity extends ActionBarActivity {
    private GTFSClient client;
    private SharedPreferences.Editor editor;
    private String chatID;

    private EditText name;
    private EditText date;
    private EditText time;
    private DatePickerDialog fromDatePickerDialog;
    private TimePickerDialog fromTimePickerDialog;

    private String EVENT_NAME;
    private long DATE_TIME;

    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    private Calendar c;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public static final String CHAT_ID_KEY = "chatID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        editor = prefs.edit();

        if (client.getID() == null) {
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatID = extras.getString(CHAT_ID_KEY);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Create Event");
        setContentView(R.layout.activity_events);

        dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        name = (EditText) findViewById(R.id.eventname);
        name.requestFocus();
        date = (EditText) findViewById(R.id.fromDate);
        date.setInputType(InputType.TYPE_NULL);

        time = (EditText) findViewById(R.id.fromTime);
        time.setInputType(InputType.TYPE_NULL);

        setCurrentTimeOnView();

        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    fromDatePickerDialog.show();
            }
        });
        time.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    fromTimePickerDialog.show();
            }
        });
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                year = selectedyear;
                month = selectedmonth;
                day = selectedday;
                selectedmonth = selectedmonth + 1;
                date.setText("" + selectedday + "/" + selectedmonth + "/" + selectedyear);
            }
        }, year, month, day);
        fromDatePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        fromTimePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hour = selectedHour;
                minute = selectedMinute;
                time.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
    }

    public void setCurrentTimeOnView() {
        c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        date.setText(dateFormatter.format(c.getTime()));
        time.setText(timeFormatter.format(c.getTime()));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_create_event:
                EVENT_NAME = name.getText().toString();

                Calendar getter = Calendar.getInstance();
                getter.set(year, month, day, hour, minute);
                DATE_TIME = getter.getTimeInMillis();
                final MessageBundle eventBundle = new MessageBundle(client.getID(),
                        client.getSESSION_ID(),MessageBundle.messageType.CREATE_EVENT);

                eventBundle.putChatroomID(chatID);
                eventBundle.putEventName(name.getText().toString());
                eventBundle.putEventDate(String.valueOf(DATE_TIME));

                Intent intent = new Intent(client, NetworkService.class);
                intent.putExtra(NetworkService.MESSAGE_KEY,
                        JsonWriter.objectToJson(eventBundle.getMessage()));
                client.startService(intent);

                Intent i = new Intent(getApplicationContext(), MessagingActivity.class);
                i.putExtra(MessageDbAdapter.CHATID, chatID);
                i.putExtra(MessageDbAdapter.ISGROUP, 1);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
