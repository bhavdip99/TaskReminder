package com.taskreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Edit_Note extends AppCompatActivity {

    SQLiteDatabase db;
    DbHelper mDbHelper;
    EditText mTitleText;
    EditText mDescriptionText;
    DatePicker pickerDate;
    TimePicker pickerTime;
    TextView time;
    TextView date;
    CheckBox checkBoxAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__note);
        mDbHelper = new DbHelper(this);
        db = mDbHelper.getWritableDatabase();

        mTitleText = (EditText) findViewById(R.id.txttitle);
        mDescriptionText = (EditText) findViewById(R.id.description);
        pickerDate = (DatePicker) findViewById(R.id.datePicker2);
        pickerTime = (TimePicker) findViewById(R.id.timePicker2);
        time = (TextView) findViewById(R.id.txt_selecttime);
        date = (TextView) findViewById(R.id.txt_selectdate);
        checkBoxAlarm = (CheckBox) findViewById(R.id.chkbox);

        final long id = getIntent().getExtras().getLong(getString(R.string.row_id_log));

        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.note_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Cursor cursor = db.rawQuery("select * from " + DbHelper.TABLE_NAME + " where " + DbHelper.C_ID + "=" + id, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mTitleText.setText(cursor.getString(cursor.getColumnIndex(DbHelper.TITLE)));
                mDescriptionText.setText(cursor.getString(cursor.getColumnIndex(DbHelper.DETAIL)));
                if (cursor.getString(cursor.getColumnIndex(mDbHelper.TIME)).toString().equals(getString(R.string.Not_Set_Alert))) {
                    checkBoxAlarm.setChecked(false);
                    pickerDate.setVisibility(View.INVISIBLE);
                    pickerTime.setVisibility(View.INVISIBLE);
                    time.setVisibility(View.INVISIBLE);
                    date.setVisibility(View.INVISIBLE);
                }
            }
            cursor.close();
        }




        checkBoxAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    pickerDate.setVisibility(View.VISIBLE);
                    pickerTime.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);
                    date.setVisibility(View.VISIBLE);
                } else {
                    pickerDate.setVisibility(View.INVISIBLE);
                    pickerTime.setVisibility(View.INVISIBLE);
                    time.setVisibility(View.INVISIBLE);
                    date.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(this, MainActivity.class);
        startActivity(setIntent);
    }

    void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_back:
                Intent openMainActivity = new Intent(this, MainActivity.class);
                startActivity(openMainActivity);
                return true;
            case R.id.action_save:
                final long id = getIntent().getExtras().getLong(getString(R.string.row_id_long));
                String title = mTitleText.getText().toString();
                String detail = mDescriptionText.getText().toString();
                ContentValues cv = new ContentValues();
                cv.put(DbHelper.TITLE, title);
                cv.put(DbHelper.DETAIL, detail);
                cv.put(DbHelper.TIME, getString(R.string.Not_Set));
                cv.putNull(DbHelper.DATE);

                if (checkBoxAlarm.isChecked()){
                    Calendar calender = Calendar.getInstance();
                    calender.clear();
                    calender.set(Calendar.MONTH, pickerDate.getMonth());
                    calender.set(Calendar.DAY_OF_MONTH, pickerDate.getDayOfMonth());
                    calender.set(Calendar.YEAR, pickerDate.getYear());
                    calender.set(Calendar.HOUR, pickerTime.getCurrentHour());
                    calender.set(Calendar.MINUTE, pickerTime.getCurrentMinute());
                    calender.set(Calendar.SECOND, 00);

                    SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.hour_minutes));
                    String timeString = formatter.format(new Date(calender.getTimeInMillis()));
                    SimpleDateFormat dateformatter = new SimpleDateFormat(getString(R.string.dateformate));
                    String dateString = dateformatter.format(new Date(calender.getTimeInMillis()));


                    AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(this, AlarmReceiver.class);

                    String alertTitle = mTitleText.getText().toString();
                    intent.putExtra(getString(R.string.alert_title), alertTitle);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), pendingIntent);
                    cv.put(DbHelper.TIME, timeString);
                    cv.put(DbHelper.DATE, dateString);

                }

                db.update(mDbHelper.TABLE_NAME, cv, mDbHelper.C_ID + "=" + id, null);

                Intent openMainScreen = new Intent(Edit_Note.this, MainActivity.class);
                openMainScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(openMainScreen);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
