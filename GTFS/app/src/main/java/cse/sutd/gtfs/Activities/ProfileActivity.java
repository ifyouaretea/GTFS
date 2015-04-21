package cse.sutd.gtfs.Activities;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cedarsoftware.util.io.JsonWriter;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cse.sutd.gtfs.Activities.Messaging.MainActivity;
import cse.sutd.gtfs.GTFSClient;
import cse.sutd.gtfs.R;
import cse.sutd.gtfs.serverUtils.MessageBundle;
import cse.sutd.gtfs.serverUtils.NetworkService;


public class ProfileActivity extends ActionBarActivity {
    private GTFSClient client;
    private static final ExecutorService exec = new ScheduledThreadPoolExecutor(100);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = (GTFSClient) getApplicationContext();
        SharedPreferences prefs = getSharedPreferences(client.PREFS_NAME, MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        if(client.getID() == null){
            Intent intent = new Intent(this, LoginActivityCog.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("Profile");
        setContentView(R.layout.activity_profile);

        EditText number = (EditText) findViewById(R.id.phonenum);
        number.setText(prefs.getString("userid", null));

        Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        String username=null;
        if (c.getCount()>0)
            username = c.getString(c.getColumnIndex("display_name"));
        c.close();

        final EditText prof_name = (EditText) findViewById(R.id.prof_name);
        prof_name.setText(username);

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = prof_name.getText().toString();
                        editor.putString("username",name);
                        editor.commit();
                        client.setNAME(name);
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        startActivity(intent);
                        requestContacts();
                        ProfileActivity.this.finish();
                    }
                });

    }
    private void requestContacts(){
        MessageBundle userRequestBundle = new MessageBundle(client.getID(), client.getSESSION_ID(),
                MessageBundle.messageType.GET_USERS);

        Callable<String[][]> task = new Callable<String[][]>() {
            public String[][] call() {
                Cursor phones = getContentResolver().query(ContactsContract.
                        CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                phones.moveToFirst();
                ArrayList<ArrayList<String>> phoneNumbers = new ArrayList<>();
                final int USER_LIMIT = 15;
                do{
                    ArrayList<String> numberSubList = new ArrayList<>();
                    for(int i = 0; i < USER_LIMIT; i++) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                        String h1 = phoneNumber.replaceAll("\\s", "");
                        String h2 = h1.replaceAll(" ", "");
                        h2 = h2.replace("+65", "");
                        h2 = h2.replaceAll("\\D", "");
                        if (h2.length() >= 8 && !h2.equals(client.getID()))
                            numberSubList.add(h2);
                        if(!phones.moveToNext())
                            break;
                    }
                    phoneNumbers.add(numberSubList);
                }while (!phones.isAfterLast());

                phones.close();
                String[][] phonenumber = new String[phoneNumbers.size()][USER_LIMIT];

                for (int j = 0; j < phoneNumbers.size(); j++)
                    for (int k = 0; k < phoneNumbers.get(j).size(); k++)
                        phonenumber[j][k] = phoneNumbers.get(j).get(k);
                return phonenumber;
            }
        };

        String[][] users;
        Future<String[][]> backtothefuture = exec.submit(task);
        try {
            backtothefuture.get(1, TimeUnit.MINUTES);

            users = backtothefuture.get();
            for (String[] s : users) {
                userRequestBundle.putUsers(s);
                Intent i = new Intent(getApplicationContext(), NetworkService.class);
                i.putExtra(NetworkService.MESSAGE_KEY,
                        JsonWriter.objectToJson(userRequestBundle.getMessage()));
                this.startService(i);
            }
        }catch(Exception e){}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                            // Add all of this activity's parents to the back stack. Navigate up to the closest parent
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
