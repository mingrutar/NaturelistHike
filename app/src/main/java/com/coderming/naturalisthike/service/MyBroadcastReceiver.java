package com.coderming.naturalisthike.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.alarm.AlarmClockHelper;
import com.coderming.naturalisthike.data.PlantDataHelper;
import com.coderming.naturalisthike.ui.MainActivity;
import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;

import java.util.Calendar;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = MyBroadcastReceiver.class.getSimpleName();

    public static final int NOTIFICATION_ID  = 1;
    public static final String EXTRA_REMINDER  = "EXTRA_FROM_REMINDER";
    private static final String Formatter = "added %s to favorite.";

    public MyBroadcastReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        int actionId = intent.getIntExtra(Constants.KEY_ACTION_SOURCE, -1);
        Log.v(LOG_TAG, "+++ALARM alarm for actionId="+Integer.toString(actionId)+" trigged at "+ Calendar.getInstance().getTime().toString());
        Context appContext = context.getApplicationContext();
        if (actionId == Constants.ACTION_TRIP_REMINDER) {
            createAndShowNotification(appContext, actionId);
            Utility.finishedAlarmAction(appContext, Constants.KEY_ACTION_ID_REMINDER);
        } else if (actionId == Constants.ACTION_SET_ALARM_REMINDER) {
            AlarmClockHelper.setAlarmClock(appContext);
            Utility.finishedAlarmAction(appContext, Constants.KEY_ACTION_ID_ALARMCLOCK);
        } else if (actionId == Constants.ACTION_CHROME_TAB_BUTTON) {
            String plantUniqName = intent.getStringExtra(Constants.CURRENT_PLANT_ID);
            PlantDataHelper.addToFavorite(appContext, plantUniqName);
            Toast.makeText(context, String.format(Formatter, plantUniqName), Toast.LENGTH_LONG ).show();
        }
   }
    private void createAndShowNotification(Context context, int actionId) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.reminder512)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_text));

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra( Constants.KEY_ACTION_SOURCE, actionId );
        resultIntent.setAction(Intent.ACTION_VIEW);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.v(LOG_TAG, "+++ALARM creted notification");
   }
}
