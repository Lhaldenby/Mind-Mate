package com.notts.MindMate;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getStringExtra("Action") != null && intent.getStringExtra("Action").equals("Notification")){
            Intent intentSub = new Intent(context, SubmissionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentSub, 0);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Feelings")
                    .setSmallIcon(R.drawable.ic_stat_child_care)
                    .setContentTitle("How are you feeling?")
                    .setContentText("Come explain how you're feeling.")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE);


            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, builder.build());
        }

    }

}
