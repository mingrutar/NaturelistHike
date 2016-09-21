package com.coderming.naturalisthike.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.coderming.naturalisthike.service.MyBroadcastReceiver;
import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linna on 9/12/2016.
 */
public class AlarmManagerHelper {
    private static final String LOG_TAG = AlarmManagerHelper.class.getSimpleName();

    static boolean sInTestMode = false;
    static long sTestRedmindTime = 4 * Constants.MINUTE_INMILLIS;
    static long sTestAlarmTime = 2 * Constants.MINUTE_INMILLIS;
    static AtomicInteger reqCodeGen = new AtomicInteger();

    public static int setReminderAlarm(Context context, long trigTimeInMillis) {
        int reqCode = -1;
       if (sInTestMode) {    // testing
            trigTimeInMillis = new Date().getTime() + sTestRedmindTime;
        }
        reqCode = reqCodeGen.getAndDecrement();
        PendingIntent pi = setAlarmWithAlarmMgr(context, reqCode, trigTimeInMillis,
                Constants.KEY_ACTION_SOURCE, Constants.ACTION_TRIP_REMINDER );
        return reqCode;
    }
    public static int setAlarmClockAlarm(Context context, long trigTimeInMillis) {
        int reqCode = -1;
        if (Utility.isAlarmClockSet(context)) {
            Toast.makeText(context, "Alarm clock already set", Toast.LENGTH_LONG).show();
            return reqCode;
        }
        reqCode = reqCodeGen.getAndDecrement();
        PendingIntent pi = setAlarmWithAlarmMgr(context, reqCode, trigTimeInMillis,
                Constants.KEY_ACTION_SOURCE, Constants.ACTION_SET_ALARM_REMINDER);
// TODO: test status
        return reqCode;
    }
    static PendingIntent setAlarmWithAlarmMgr(Context context, int reqCode,
               long trigTimeInMillis, String key, int actionId ) {
        Intent resultIntent = new Intent(context, MyBroadcastReceiver.class);
        resultIntent.putExtra(key, actionId );
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reqCode, resultIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, trigTimeInMillis, pendingIntent);
//        Log.v(LOG_TAG, String.format("+++ALARM Set alarm actionId=%d on %s",
//                actionId, new Date(trigTimeInMillis).toString()));
        return pendingIntent;
    }
}
