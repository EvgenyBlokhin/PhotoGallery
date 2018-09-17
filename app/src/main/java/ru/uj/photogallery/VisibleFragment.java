package ru.uj.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Blokhin Evgeny on 15.09.2018.
 */
public class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter filter = new IntentFilter(PollSchedulerService.ACTION_SHOW_NOTIFICATION);
            getActivity().registerReceiver(mOnShowNotification, filter, PollSchedulerService.PERM_PRIVATE, null);
        } else {
            IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
            getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
