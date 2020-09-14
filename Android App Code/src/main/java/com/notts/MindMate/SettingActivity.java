package com.notts.MindMate;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 0;
    private final FileHandling fileHandling = new FileHandling(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        Button trackBut = findViewById(R.id.InAppBut);

        trackBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
            }
        });

        Button clearBut = findViewById(R.id.clearBut);
        clearBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearDataDialog();
            }
        });

        final Spinner notTime = findViewById(R.id.TimeDropDown);
        SharedPreferences sharedPref = getSharedPreferences("Notification_Preference_File", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        notTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullTime = notTime.getSelectedItem().toString();
                int hour = Integer.parseInt(fullTime.substring(0,2));
                int minute = Integer.parseInt(fullTime.substring(3,5));

                AlarmManager alarmMgr = (AlarmManager)getSystemService(SettingActivity.ALARM_SERVICE);
                if (alarmMgr!= null) {
                    PendingIntent alarmIntent = MainActivity.getAlarmIntent();
                    alarmMgr.cancel(alarmIntent);
                }

                editor.putInt("Notification_Time_Hour", hour);
                editor.putInt("Notification_Time_Minute", minute);
                editor.commit();

                Intent intent = new Intent(SettingActivity.this, NotificationReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(SettingActivity.this, 0, intent, 0);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.notificationTimes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        notTime.setAdapter(adapter);

        int selection = 0;
        selection = sharedPref.getInt("Notification_Time_Hour", 0)*2;
        if (sharedPref.getInt("Notification_Time_Minute",0) == 30){
            selection++;
        }
        System.out.println(selection);
        notTime.setSelection(selection);

        Button exportBut = findViewById(R.id.exportBut);
        exportBut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String filename = "WellbeingFile.txt";
                String data = "No Data";
                String preData = "";
                final FileHandling fileHandling = new FileHandling(SettingActivity.this);

                try {
                    preData = fileHandling.readFile(SettingActivity.this, filename);
                    if (preData != "" && preData != null)
                        data = preData;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent sharingIntent = new Intent();
                sharingIntent.setAction(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, data);
                sharingIntent.setType("text/plain");
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });
    }

    public void startMain(View view){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    public void startHelp(View view){
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    public void startCalendar(View view){
        Intent calendarIntent = new Intent(this, CalendarActivity.class);
        startActivity(calendarIntent);
    }

    private void clearDataDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Clear Data");
        builder.setMessage("You are about to delete all records of stored feelings and app tracking. Do you really want to proceed?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    final String filename = "WellbeingFile.txt";
                    fileHandling.clearFile(SettingActivity.this, filename);
                    fileHandling.clearFile(SettingActivity.this, "GraphData.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "You've choosen to delete all records", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You've changed your mind to delete all records", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

}
