package cse.sutd.gtfs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class NewGroupActivity extends ActionBarActivity {
   private EditText group_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("New Group");
        setContentView(R.layout.activity_new_group);

       group_name = (EditText) findViewById(R.id.group_name);
        final TextView countTextView = (TextView) findViewById(R.id.countTextView);
        countTextView.setText("25");

        EditText add_contact = (EditText) findViewById(R.id.add_contact);


        group_name.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft)
            {

            }

            @Override
            public void afterTextChanged(Editable group_name)
            {
                // this will show characters remaining

                countTextView.setText(Integer.toString(25 - group_name.toString().length()));
            }
        });
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_create_group) {

            if (group_name.getText().toString().trim().length() >0) {
                Intent intent = new Intent(NewGroupActivity.this, MainActivity.class);
                startActivity(intent);
                NewGroupActivity.this.finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "Please enter group name" , Toast.LENGTH_SHORT ).show();
            }
         return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
