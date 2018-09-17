package ru.uj.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Blokhin Evgeny on 07.09.2018.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollSchedulerService extends JobService {

    private static final String TAG = "PollSchedulerService";
    private PollTask mCurrentTask;
    public static final String ACTION_SHOW_NOTIFICATION = "ru.uj.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "ru.uj.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static void setServiceScheduler(Context context, boolean isOn) {
        final int JOB_ID = 1;

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (isOn) {
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollSchedulerService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setPeriodic(1000 * 60 * 1)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        } else {
            scheduler.cancel(JOB_ID);
        }

        QueryPreferences.setAlarmOn(context, isOn);
    }

    public static boolean isServiceSchedulerOn(Context context) {
        final int JOB_ID = 1;

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo :
                scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.setContext(this);
        mCurrentTask.execute(params);
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {

        WeakReference<Context> ctx;
        public void setContext (Context ctx) {
            this.ctx = new WeakReference<>(ctx);
        }

        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];

            String query = QueryPreferences.getStoredQuery(ctx.get());
            String lastResultId = QueryPreferences.getLastResultId(ctx.get());
            List<GalleryItem> items;

            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos(1);
            } else {
                items = new FlickrFetchr().searchPhotos(query, 1);
            }
            if (items.size() == 0) {
                return null;
            }
            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);
            } else {
                Log.i(TAG, "Got a new result: " + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(ctx.get());
                PendingIntent pi = PendingIntent.getActivity(ctx.get(), 0, i, 0);

                Notification notification = new NotificationCompat.Builder(ctx.get())
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                showBackgroundNotification(0, notification);

                jobFinished(jobParams, false);
            }

            QueryPreferences.setLastResultId(ctx.get(), resultId);
            return null;
        }

        private void showBackgroundNotification(int requestCode, Notification notification) {
            Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
            i.putExtra(REQUEST_CODE, requestCode);
            i.putExtra(NOTIFICATION, notification);
            sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return false;
    }
}
