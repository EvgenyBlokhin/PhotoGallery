package ru.uj.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Blokhin Evgeny on 17.09.2018.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context c, Intent intent) {
        Log.i(TAG, "received result: " +getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int requestCodePSS = intent.getIntExtra(PollSchedulerService.REQUEST_CODE, 0);
            Notification notification = (Notification) intent.getParcelableExtra(PollSchedulerService.NOTIFICATION);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
            notificationManager.notify(requestCodePSS, notification);
        } else {
            int requestCodePS = intent.getIntExtra(PollService.REQUEST_CODE, 0);
            Notification notification = (Notification) intent.getParcelableExtra(PollService.NOTIFICATION);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
            notificationManager.notify(requestCodePS, notification);
        }


    }
}
