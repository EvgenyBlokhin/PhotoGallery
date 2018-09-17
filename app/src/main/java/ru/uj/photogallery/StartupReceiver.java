package ru.uj.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Blokhin Evgeny on 11.09.2018.
 */
public class StartupReceiver extends BroadcastReceiver {
    private  static final String TAG = "StartupReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PollSchedulerService.setServiceScheduler(context, isOn);
        } else {
            PollService.setServiceAlarm(context, isOn);
        }
    }
}
