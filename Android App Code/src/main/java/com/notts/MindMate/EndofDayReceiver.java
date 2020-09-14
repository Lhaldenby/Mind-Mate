package com.notts.MindMate;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EndofDayReceiver extends BroadcastReceiver {

    private Date date;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getStringExtra("Action") != null && intent.getStringExtra("Action").equals("EndDay")){
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                date = dateFormat.parse(intent.getStringExtra("Date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.setTime(date);

            AppOpsManager appOps = (AppOpsManager)context.getSystemService(context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());

            if (mode == AppOpsManager.MODE_ALLOWED){

                String filename = "TrackedApps.txt";
                String data = "";
                FileHandling fileHandling = new FileHandling(context);
                List<String> appList = new ArrayList<String>();

                try {
                    data = fileHandling.readFile(context, filename);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (data != null && data != "") {
                    final String[] dataArray = data.split(";");

                    for (int i=0; i < dataArray.length; i++){
                        appList.add(dataArray[i]);
                    }

                }

                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startDay = calendar.getTimeInMillis();

                calendar.set(Calendar.HOUR, 23);
                calendar.set(Calendar.MINUTE,59);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long endDay = calendar.getTimeInMillis();

                //get usage stats for the phone
                UsageStatsManager usm = (UsageStatsManager) context.getSystemService(context.USAGE_STATS_SERVICE);
                Map<String, UsageStats> lUsm = usm.queryAndAggregateUsageStats(startDay, endDay);

                for (Map.Entry<String, UsageStats> entry : lUsm.entrySet()) {
                    if (appList.contains(entry.getKey())) {
                        //write to file GraphData.txt
                        String graphFile = "GraphData.txt";
                        String graphInfo = dateFormat.format(date) + "," + entry.getKey() + "," + entry.getValue();
                        try {
                            fileHandling.writeFile(context, filename, graphInfo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }


    }

}
