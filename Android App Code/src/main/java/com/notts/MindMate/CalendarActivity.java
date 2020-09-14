package com.notts.MindMate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CalendarActivity extends AppCompatActivity {

    public HashMap<String, Integer> dateColors = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        final String filename = "WellbeingFile.txt";
        String data = "";
        final FileHandling fileHandling = new FileHandling(this);

        try {
            data = fileHandling.readFile(this, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final List<String[]> feelingList = new ArrayList<String[]>();
        String[] sendInfo = new String[0];

        if (data != null && data != "") {
            final String[] dataArray = data.split(";");

            for (int i = 0; i < dataArray.length; i++) {
                final String[] info = dataArray[i].split(",");

                if (dateColors.containsKey(info[3])){
                    if (info[2].equals("0")){
                        dateColors.put(info[3], dateColors.get(info[3]) - 1);
                    }else if (info[2].equals("1")) {
                        //nothing
                    } else {
                        dateColors.put(info[3], dateColors.get(info[3]) + 1);
                    }
                } else {
                    if (info[2].equals("0")){
                        dateColors.put(info[3], -1);
                    }else if (info[2].equals("1")) {
                        dateColors.put(info[3], 0);
                    } else {
                        dateColors.put(info[3], 1);
                    }
                }

                feelingList.add(info);

            }
            sendInfo = dataArray;
        }

        LinearLayout layout = findViewById(R.id.ListLayout);
        final CalendarView calendar = findViewById(R.id.feelingCalendar);
        calendar.setFocusedMonthDateColor(Color.RED);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        final String selectedDate = sdf.format(new Date(calendar.getDate()));

        Iterator dateIterator = dateColors.entrySet().iterator();
        while (dateIterator.hasNext()) {
            Map.Entry pair = (Map.Entry) dateIterator.next();
            System.out.println(pair.getKey() + " : " + pair.getValue());
        }

        for (final String[] feeling : feelingList){

            if (feeling[3].equals(selectedDate)){
                Button bt = new Button(CalendarActivity.this);
                bt.setText(feeling[0]);

                if (feeling[2].equals("0")) {
                    bt.setBackgroundColor(Color.RED);
                } else if (feeling[2].equals("1")) {
                    bt.setBackgroundColor(Color.rgb(255,165,0));
                } else {
                    bt.setBackgroundColor(Color.GREEN);
                }

                layout.addView(bt);
            }

        }

        Button expandBut = findViewById(R.id.ExpandBut);
        final String[] finalSendInfo = sendInfo;
        expandBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent detailIntent = new Intent(CalendarActivity.this, DetailedCalendarDay.class);
                    detailIntent.putExtra("date", selectedDate);
                    detailIntent.putExtra("data", finalSendInfo);
                    startActivity(detailIntent);
            }
        });

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener(){
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                LinearLayout layout = findViewById(R.id.ListLayout);

                if(layout.getChildCount() > 0)
                    layout.removeAllViews();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                final String selectedDate = sdf.format(cal.getTime());

                for (final String[] feeling : feelingList){

                    if (feeling[3].equals(selectedDate)){
                        Button bt = new Button(CalendarActivity.this);
                        bt.setText(feeling[0]);
                        if (feeling[2].equals("0")) {
                            bt.setBackgroundColor(Color.RED);
                        } else if (feeling[2].equals("1")) {
                            bt.setBackgroundColor(Color.rgb(255,165,0));
                        } else {
                            bt.setBackgroundColor(Color.GREEN);
                        }

                        layout.addView(bt);
                    }

                }

                Button expandBut = findViewById(R.id.ExpandBut);
                expandBut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Intent detailIntent = new Intent(CalendarActivity.this, DetailedCalendarDay.class);
                            detailIntent.putExtra("date", selectedDate);
                            detailIntent.putExtra("data", finalSendInfo);
                            startActivity(detailIntent);
                    }
                });

            }
        });
    }

    public void startMain(View view){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    public void startSubmission(View view){
        Intent submitIntent = new Intent(this, SubmissionActivity.class);
        startActivity(submitIntent);
    }

    public void startHelp(View view){
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    public void startSetting(View view){
        Intent settingIntent = new Intent(this, SettingActivity.class);
        startActivity(settingIntent);
    }
}
