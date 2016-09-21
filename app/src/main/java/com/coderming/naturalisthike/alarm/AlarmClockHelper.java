package com.coderming.naturalisthike.alarm;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.util.Log;
import android.widget.Toast;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by linna on 9/10/2016.
 */
public class AlarmClockHelper {
    private static final String LOG_TAG = AlarmClockHelper.class.getSimpleName();

    public static void setAlarmClock(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long wakeupTime = prefs.getLong(Constants.KEY_PREF_WAKEUP_TIME, -1);
        if (wakeupTime == -1) {
            Log.e(LOG_TAG, "wrong data for alarm, no alarm will be set");
            Toast.makeText(context, "wrong data for alarm, no alarm will be set", Toast.LENGTH_LONG).show();
        }
        Calendar calTime = Calendar.getInstance();
        calTime.setTimeInMillis(wakeupTime);
        int hourOfDay = calTime.get(Calendar.HOUR_OF_DAY);
        int minute = calTime.get(Calendar.MINUTE);
        int day = calTime.get(Calendar.DAY_OF_WEEK);
        ArrayList<Integer> alarmDay = new ArrayList<>();
        alarmDay.add(day);

        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        String name = context.getString(R.string.clock_alarm_name);
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, name);
        alarmIntent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, alarmDay);             // ??
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hourOfDay);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        alarmIntent.putExtra(AlarmClock.EXTRA_IS_PM, false);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);       //ndo not display AlarmClock
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }
    @TargetApi(23)                  // not in use because AlarmClock works at 19
    public static void dismissAlarm(Context context ) {     // crash, require SDK 23
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long wakeupTime = prefs.getLong(Constants.KEY_PREF_WAKEUP_TIME, -1);
        if (wakeupTime == -1) {
            Log.e(LOG_TAG, "wrong data for alarm, cannot dismiss  alarm");
            Toast.makeText(context, "wrong data for alarm, cannot dismiss alarm", Toast.LENGTH_LONG).show();
        }
        Calendar calTime = Calendar.getInstance();
        calTime.setTimeInMillis(wakeupTime);
        int hourOfDay = calTime.get(Calendar.HOUR_OF_DAY);
        int minute = calTime.get(Calendar.MINUTE);
        int day = calTime.get(Calendar.DAY_OF_WEEK);
        ArrayList<Integer> alarmDay = new ArrayList<>();
        alarmDay.add(day);
        Intent alarmIntent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        alarmIntent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, alarmDay);             // ??
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hourOfDay);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        context.startActivity(alarmIntent);
    }
    public static  void showAlarm(Context context) {
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        context.startActivity(alarmIntent);
    }
}
