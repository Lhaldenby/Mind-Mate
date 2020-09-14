package com.notts.MindMate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 0;
    private static PendingIntent alarmIntent;
    private static PendingIntent endDayIntent;
    private List<String> appList = new ArrayList<String>();
    private final FileHandling fileHandling = new FileHandling(this);
    private final String filename = "TrackedApps.txt";

    private String changed = "Today";
    private Map<String, String> apps = new HashMap<String, String>();
    private LinearLayout appLayout;
    private AlarmManager alarmManager;

    private BarChart barChart;

    public UsageStatsManager usm;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup barchart for later use
        barChart = (BarChart) findViewById(R.id.barchart);

        //check permission to get usage stats
        AppOpsManager appOps = (AppOpsManager)getSystemService(this.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());

        //if don't have the permission go to settings to change that
        if (mode == AppOpsManager.MODE_ALLOWED){
        } else {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
        }

        String data = "";
        try {
            data = fileHandling.readFile(this, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (data != null && data != "") {
            final String[] dataArray = data.split(";");

            for (int i=0; i < dataArray.length; i++){
                appList.add(dataArray[i]);
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

        //usage stats for today

        Calendar now = Calendar.getInstance();
        long endMillis = now.getTimeInMillis();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        long startMilis = now.getTimeInMillis();

        //get usage stats for the phone
        usm = (UsageStatsManager) this.getSystemService(this.USAGE_STATS_SERVICE);
        Map<String, UsageStats> lUsm = usm.queryAndAggregateUsageStats(startMilis, endMillis);

        //layout to show tracked app
        appLayout = findViewById(R.id.statsLinearLayout);

        setTrackedAppLayout(lUsm);

        //bar chart initial setup
        List<BarEntry> entries = new ArrayList<BarEntry>();
        ArrayList<String> labels = new ArrayList<String>();

        int count = 0;
        for (Map.Entry<String, UsageStats> entry : lUsm.entrySet()) {
            if (appList.contains(entry.getKey())) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(entry.getValue().getTotalTimeInForeground());
                entries.add(new BarEntry((float) count, (float) minutes));
                count++;
                labels.add(apps.get(entry.getKey()));
            }
        }

        setBarChart(entries,labels);
        createNotificationChannel();

        final Spinner timeFrame = findViewById(R.id.TimeFrameChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.timeFrames, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        timeFrame.setAdapter(adapter);

        timeFrame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullTime = timeFrame.getSelectedItem().toString();
                if (!fullTime.equals(changed)) {
                    appLayout.removeAllViews();
                    barChart.clear();
                    Calendar now = Calendar.getInstance();
                    int count = 0;
                    switch (fullTime) {
                        case "Today":
                            changed = "Today";
                            //get usage stats for the phone today

                            long TendMillis = now.getTimeInMillis();
                            now.set(Calendar.HOUR, 0);
                            now.set(Calendar.MINUTE, 0);
                            now.set(Calendar.SECOND, 0);
                            now.set(Calendar.MILLISECOND, 0);
                            long TstartMilis = now.getTimeInMillis();

                            Map<String, UsageStats> tUsm = usm.queryAndAggregateUsageStats(TstartMilis, TendMillis);

                            List<BarEntry> Tentries = new ArrayList<BarEntry>();
                            ArrayList<String> Tlabels = new ArrayList<String>();

                            for (Map.Entry<String, UsageStats> entry : tUsm.entrySet()) {
                                if (appList.contains(entry.getKey())) {
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(entry.getValue().getTotalTimeInForeground());
                                    Tentries.add(new BarEntry((float) count, (float) minutes));
                                    count++;
                                    Tlabels.add(apps.get(entry.getKey()));
                                }
                            }

                            setTrackedAppLayout(tUsm);
                            setBarChart(Tentries, Tlabels);

                            break;
                        case "Week":
                            changed = "Week";
                            //get usage stats for the phone today
                            Calendar weekCal = Calendar.getInstance();
                            weekCal.set(Calendar.HOUR_OF_DAY, 0);
                            weekCal.set(Calendar.MINUTE, 0);
                            weekCal.set(Calendar.SECOND, 0);
                            weekCal.set(Calendar.MILLISECOND, 0);
                            weekCal.set(Calendar.DAY_OF_WEEK, 1);

                            Map<String, UsageStats> WUsm = usm.queryAndAggregateUsageStats(weekCal.getTimeInMillis(), now.getTimeInMillis());

                            List<BarEntry> Wentries = new ArrayList<BarEntry>();
                            ArrayList<String> Wlabels = new ArrayList<String>();

                            for (Map.Entry<String, UsageStats> entry : WUsm.entrySet()) {
                                if (appList.contains(entry.getKey())) {
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(entry.getValue().getTotalTimeInForeground());
                                    Wentries.add(new BarEntry((float) count, (float) minutes));
                                    count++;
                                    Wlabels.add(apps.get(entry.getKey()));
                                }
                            }

                            setTrackedAppLayout(WUsm);
                            setBarChart(Wentries, Wlabels);

                            break;
                        case "Month":
                            changed = "Month";
                            Calendar monthCal = Calendar.getInstance();
                            monthCal.set(Calendar.HOUR_OF_DAY, 0);
                            monthCal.clear(Calendar.MINUTE);
                            monthCal.clear(Calendar.SECOND);
                            monthCal.clear(Calendar.MILLISECOND);
                            monthCal.set(Calendar.DAY_OF_MONTH, 1);

                            Map<String, UsageStats> MUsm = usm.queryAndAggregateUsageStats(monthCal.getTimeInMillis(), now.getTimeInMillis());

                            List<BarEntry> Mentries = new ArrayList<BarEntry>();
                            ArrayList<String> Mlabels = new ArrayList<String>();

                            for (Map.Entry<String, UsageStats> entry : MUsm.entrySet()) {
                                if (appList.contains(entry.getKey())) {
                                    long minutes = TimeUnit.MILLISECONDS.toMinutes(entry.getValue().getTotalTimeInForeground());
                                    Mentries.add(new BarEntry((float) count, (float) minutes));
                                    count++;
                                    Mlabels.add(apps.get(entry.getKey()));
                                }
                            }

                            setTrackedAppLayout(MUsm);
                            setBarChart(Mentries, Mlabels);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences sharedPref = getSharedPreferences("Notification_Preference_File", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (sharedPref.getInt("Notification_Time_Hour", -1) == -1 && sharedPref.getInt("Notification_Time_Minute", -1) == -1) {
            editor.putInt("Notification_Time_Hour", 13);
            editor.putInt("Notification_Time_Minute", 30);
            editor.commit();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, sharedPref.getInt("Notification_Time_Hour", 13));
        calendar.set(Calendar.MINUTE, sharedPref.getInt("Notification_Time_Minute", 30));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager = (AlarmManager)getSystemService(this.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("Action", "Notification");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND, 0);
        String date = dateFormat.format(calendar.getTime());

        Intent dayIntent = new Intent(this, EndofDayReceiver.class);
        dayIntent.putExtra("Action", "EndDay");
        dayIntent.putExtra("Date", date);
        endDayIntent = PendingIntent.getBroadcast(this, 0, dayIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, endDayIntent);

    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {

        return (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

    }

    private void setBarChart(List<BarEntry> entries, ArrayList<String> labels ){
        if (!appList.isEmpty()){
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setTrackedAppLayout(Map<String, UsageStats> lUsm){

        TextView title = new TextView(this);
        title.setText("Tracked Apps");
        title.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        title.setTextSize(18);
        appLayout.addView(title);

        if (!appList.isEmpty()) {
            for (Map.Entry<String, UsageStats> entry : lUsm.entrySet()) {
                if (appList.contains(entry.getKey())) {
                    TextView statsTxt = new TextView(this);
                    long millis = entry.getValue().getTotalTimeInForeground();
                    String time = String.format("%02dh:%02dm:%02ds", TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
                    statsTxt.setText(apps.get(entry.getKey()) + " : " + time);
                    appLayout.addView(statsTxt);
                }

            }

        } else {
            TextView emptyText = new TextView(this);
            emptyText.setText("No Apps Tracked");
            appLayout.addView(emptyText);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Feelings", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void startCalendar(View view){
        Intent calendarIntent = new Intent(this, CalendarActivity.class);
        startActivity(calendarIntent);
    }

    public void startHelp(View view){
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    public void startSetting(View view){
        Intent settingIntent = new Intent(this, SettingActivity.class);
        startActivity(settingIntent);
    }

    public void startEdit(View view){
        Intent calendarIntent = new Intent(this, EditAppsActivity.class);
        startActivity(calendarIntent);
    }

    public static PendingIntent getAlarmIntent(){ return alarmIntent;}

}
