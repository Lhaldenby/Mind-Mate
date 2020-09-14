package com.notts.MindMate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DetailedCalendarDay extends AppCompatActivity {

    private final FileHandling fileHandling = new FileHandling(this);
    private final String filename = "TrackedApps.txt";

    private BarChart barChart;
    private List<String> appList = new ArrayList<String>();
    private Map<String, String> apps = new HashMap<String, String>();
    public UsageStatsManager usm;

    private List<BarEntry> entries = new ArrayList<BarEntry>();
    private ArrayList<String> labels = new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_calendar_day);

        Bundle b = getIntent().getExtras();
        //date
        String dateText = (String)b.get("date");
        String[] dataArray = (String[])b.get("data");

        TextView date = findViewById(R.id.DateText);
        date.setText(dateText);

        final TextView extraInfo = findViewById(R.id.Info);
        LinearLayout layout = findViewById(R.id.FeelingList);

        final List<String[]> feelingList = new ArrayList<String[]>();
        for (int i = 0; i < dataArray.length; i++) {
            final String[] info = dataArray[i].split(",");
            feelingList.add(info);
        }

        boolean emptyList = true;

        for (final String[] feeling : feelingList) {
            if (feeling[3].equals(dateText)) {
                emptyList = false;
                Button bt = new Button(this);
                bt.setText(feeling[0]);

                if (feeling[2].equals("0")) {

                    bt.setBackgroundColor(Color.RED);

                } else if (feeling[2].equals("1")) {

                    bt.setBackgroundColor(Color.rgb(255, 165, 0));

                } else {

                    bt.setBackgroundColor(Color.GREEN);

                }

                bt.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        extraInfo.setText(feeling[1]);

                    }
                    });
                layout.addView(bt);

                }

            }

        if (emptyList) {
            Button bt = new Button(this);
            bt.setText("No Feelings Submitted");
            bt.setBackgroundColor(Color.GRAY);
            layout.addView(bt);
        }

        String data = "";
        try {
            data = fileHandling.readFile(this, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (data != null && data != "") {
            final String[] dataArrayApps = data.split(";");

            for (int i=0; i < dataArrayApps.length; i++){
                appList.add(dataArrayApps[i]);
            }

        }

        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if ((!isSystemPackage(p))) {
                String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
                String packages = p.applicationInfo.packageName;

                if (appList.contains(packages)){
                    apps.put(packages,appName);
                }
            }
        }

        AppOpsManager appOps = (AppOpsManager)getSystemService(this.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());

        if (mode == AppOpsManager.MODE_ALLOWED){
            //date change
            Calendar specifiedDate = Calendar.getInstance();
            specifiedDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateText.substring(0,2)));
            specifiedDate.set(Calendar.MONTH, Integer.parseInt(dateText.substring(3,5))-1);
            specifiedDate.set(Calendar.YEAR, Integer.parseInt(dateText.substring(6,10)));
            specifiedDate.set(Calendar.HOUR, 0);
            specifiedDate.set(Calendar.MINUTE, 0);
            specifiedDate.set(Calendar.SECOND, 0);
            specifiedDate.set(Calendar.MILLISECOND, 0);
            long startMilis = specifiedDate.getTimeInMillis();

            Calendar now = Calendar.getInstance();
            long endMillis = now.getTimeInMillis();
            now.set(Calendar.HOUR, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);



            if (now.equals(specifiedDate)){
                now = Calendar.getInstance();
                endMillis = now.getTimeInMillis();
            }else {
                specifiedDate.set(Calendar.HOUR, 23);
                specifiedDate.set(Calendar.MINUTE, 59);
                specifiedDate.set(Calendar.SECOND, 59);
                specifiedDate.set(Calendar.MILLISECOND, 99);
                endMillis = specifiedDate.getTimeInMillis();
            }

            //get usage stats for the phone
            usm = (UsageStatsManager) this.getSystemService(this.USAGE_STATS_SERVICE);
            Map<String, UsageStats> lUsm = usm.queryAndAggregateUsageStats(startMilis, endMillis);

            int count = 0;
            for (Map.Entry<String, UsageStats> entry : lUsm.entrySet()) {
                if (appList.contains(entry.getKey())) {
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(entry.getValue().getTotalTimeInForeground());
                    entries.add(new BarEntry((float) count, (float) minutes));
                    count++;
                    labels.add(apps.get(entry.getKey()));
                }
            }
        }


        barChart = (BarChart) findViewById(R.id.barchart);

        if (!entries.isEmpty()){
            BarDataSet barDataSet = new BarDataSet(entries, "App time in minutes");
            barDataSet.setColors(new int[] {Color.rgb(197,174,180), Color.rgb(123,174,180), Color.rgb(153,180,159), Color.rgb(214,214,214)});
            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.9f);
            barChart.setData(barData);
        }
        barChart.setFitBars(true);
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.setTouchEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.setNoDataText("No App Tracking Data");
        barChart.getAxisRight().setEnabled(false);
        barChart.setDrawValueAboveBar(false);
        barChart.invalidate();

    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {

        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

    }

    public void startCalendar(View view){
        Intent calendarIntent = new Intent(this, CalendarActivity.class);
        startActivity(calendarIntent);
    }
}