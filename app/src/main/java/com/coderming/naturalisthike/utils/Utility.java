package com.coderming.naturalisthike.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.coderming.naturalisthike.alarm.AlarmManagerHelper;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.ui.PlantListActivity;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by linna on 8/26/2016.
 */
public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();
    public static final String Plular = "s";
    private static final String DelimBar = "|";
    // **** Date and Time
    public static final SimpleDateFormat sDateFormat = new SimpleDateFormat("MMMM d, yyyy");
    public static final SimpleDateFormat sShortDateFormat = new SimpleDateFormat("MMM d");
    public static final SimpleDateFormat sDateTimeFormat = new SimpleDateFormat("MMMM d, yyyy KK:mm a");
    public static final SimpleDateFormat sTimeFormat = new SimpleDateFormat("KK:mm a");

    public static final String sFormate_1f = "%.1f";
    public static final String sAtTrailHeadTimeFormater = "%s am at TH";
    public static final String sHome2MP =" to meeting place.";
    private static final String BR_HOUR = "%d hour";
    private static final String BR_MIN = " %1.0f min ";
    public static final String sDayFormat = "%d Day%s";

    public static String formatDateOnly(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        return sDateFormat.format(date);
    }

    public static String formatTimeOnly(long dateInMilliseconds) {
        Date date = new Date(dateInMilliseconds);
        String str = sTimeFormat.format(date);
        return str;
    }
    public static String formatAtTrailHeader(long dateInMilli) {
        Date date = new Date(dateInMilli);
        String str = sTimeFormat.format(date);
        return String.format(sAtTrailHeadTimeFormater, str );
    }
    public static String formatTimeDuration(long dateInMilli) {
        StringBuilder sb = new StringBuilder();
        long val = dateInMilli / Constants.HOUR_INMILLIS;
        if (val > 0) {
            sb.append(String.format(BR_HOUR, val));
        }
        double dval = (dateInMilli % Constants.HOUR_INMILLIS);
        dval = Math.ceil(dval / Constants.MINUTE_INMILLIS);
        sb.append(String.format(BR_MIN, dval));
        return sb.toString();
    }
    public static String formatHome2MPTime(long drivingTime) {
        String str = formatTimeDuration(drivingTime);
        return str + sHome2MP;
    }
    public static String formatDays(long daysInMilli) {
        String str = String.format(sDayFormat, daysInMilli) + ( (daysInMilli > 1 )? Plular : Constants.DEFAULT_PREF_STRING);
        return str;
    }
    public static String format_double_1f(double val) {
        return String.format(sFormate_1f, val);
    }

    public static String capitalizeString(String str) {
        if ((str != null) && !str.isEmpty()) {
            if (str.length() > 1 ) {
                return str.substring(0, 1).toUpperCase() + str.substring(1);
            } else {
                return str.toUpperCase();
            }
        } else {
            return str;
        }
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return ((activeNetwork != null) && activeNetwork.isConnectedOrConnecting());
    }

    // **** Current Trip
    public static boolean resetIfNewTrip(Context context, long tripdate, double th_lan, double th_lon) {
        tripdate -= tripdate % Constants.DAY_INMILLIS;
        StringBuilder sb = new StringBuilder(Constants.TID_PREFIX);
        sb.append(tripdate);
        sb.append(DelimBar);
        sb.append(th_lan);
        sb.append(DelimBar);
        sb.append(th_lon);
        String newTripId = sb.toString();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String tripId = pref.getString(Constants.KEY_PREF_CURRENT_TRIP_ID, Constants.DEFAULT_PREF_STRING);
        if (!newTripId.equals(tripId)) {
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(Constants.KEY_PREF_CURRENT_TRIP_ID, newTripId);
            edit.putInt(Constants.KEY_ACTION_ID_REMINDER, Constants.FINISHED_ACTION_ID);
            edit.putInt(Constants.KEY_ACTION_ID_ALARMCLOCK, Constants.FINISHED_ACTION_ID);
            edit.apply();
            return true;
        } else {
            return false;
        }
    }
    // TODO: check if alarm is enabled ?
    public static boolean setAlarm(Context context, long tripdate) {
        long now = Calendar.getInstance().getTimeInMillis();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int reqInPref = pref.getInt(Constants.KEY_ACTION_ID_ALARMCLOCK, Constants.FINISHED_ACTION_ID);
        if (reqInPref == Constants.FINISHED_ACTION_ID) {        // no reminder has set
            long setAlarmTime = Constants.SET_CLOCK_ALARM_DAYS_INMILLIS;
            if ((setAlarmTime + now) < tripdate) {   // in range for reminder
                long trigTimeInMillis = tripdate - setAlarmTime;
                int reqCode = AlarmManagerHelper.setAlarmClockAlarm(context, trigTimeInMillis);
                pref.edit().putInt(Constants.KEY_ACTION_ID_ALARMCLOCK, reqCode).apply();
                return true;
            }
        }
        return false;
    }
    // TODO: check if reminder is enabled ?
    public static boolean setReminder(Context context, long tripDate) {
        long now = Calendar.getInstance().getTimeInMillis();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int reqInPref = pref.getInt(Constants.KEY_ACTION_ID_REMINDER, Constants.FINISHED_ACTION_ID);
        if (reqInPref == Constants.FINISHED_ACTION_ID) {        // no reminder has set
            long reminderTime = pref.getInt(Constants.KEY_PREF_REMINDER_DAYS, Constants.DEFAULT_REMINDER)
                    * Constants.DAY_INMILLIS;
            if ((reminderTime + now) < tripDate) {   // in range for reminder
                long trigTimeInMillis = tripDate - reminderTime;
                // TODO           if (isRemindEnabled) {
                int reqCode = AlarmManagerHelper.setReminderAlarm(context, trigTimeInMillis);
                pref.edit().putInt(Constants.KEY_ACTION_ID_REMINDER, reqCode).apply();
                return true;
            }
        }
        return false;
    }

    // *** Wakeup time
    public static long getWakeupTime(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(Constants.KEY_PREF_WAKEUP_TIME, Constants.DEFAULT_PREF_TIME);
    }

    public static void setWakeupTime(Context context, long wakeupTime) {
        if (wakeupTime > new Date().getTime()) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(Constants.KEY_PREF_WAKEUP_TIME, wakeupTime)
                .apply();
        } else {
            Log.w(LOG_TAG, "setWakeupTime: bad parameters, wakeuptime is not changed");
        }
    }

    public static boolean isAlarmClockSet(Context context) {
        int alarmActionId = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.KEY_ACTION_ID_ALARMCLOCK, Constants.DEFAULT_ACTION_ID_ALARMACTION);
        return alarmActionId == Constants.FINISHED_ACTION_ID;
    }

    public static void finishedAlarmAction(Context context, String prefKey) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putLong(prefKey, Constants.FINISHED_ACTION_ID)
            .apply();
    }

    // ****
    public static boolean isWeatherAvailable(long HikeDay) {
        return ((HikeDay - new Date().getTime())  <= Constants.WEATHER_FORECAST_LIMIT_INMILLIS);
    }

    /***
     * formated data
     */
    // http://forecast.weather.gov/MapClick.php?lat=47.62&amp;lon=-122.36
    public static Uri getWeatherInfoUrl(double lat, double lon) {
        Uri buildUri = Uri.parse(Constants.URL_WEATHER_BASE).buildUpon()
                .appendQueryParameter("lat", Double.toString(lat))
                .appendQueryParameter("lon", Double.toString(lon)).build();
//        Log.v("getWeatherInfoUrl", "uri =" + buildUri.toString());
        return buildUri;
    }

    public static void startPlantListActivity(Context context, long tripDBId, double thLan, double thLon, boolean isOnTrail) {
        Uri uri = TripContract.PlantTripEntry.buildTripUri(tripDBId);
        Intent intent = new Intent(context, PlantListActivity.class);    // back key goes to main, sunshine3 SyncAdapter
        intent.setData(uri);
        intent.putExtra(Constants.TAG_TRAILHEAD_LAT, thLan);
        intent.putExtra(Constants.TAG_TRAILHEAD_LON, thLon);
        intent.putExtra(Constants.TAG_IS_ON_TRAIL, isOnTrail);
        context.startActivity(intent);
    }
    public static long getReminder(Context context, long hikeDate) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int reminder = pref.getInt(Constants.KEY_PREF_REMINDER_DAYS, Constants.DEFAULT_REMINDER);
        return hikeDate - reminder;
    }
    public static LatLng getHomeGeo(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        LatLng homeGeo = new LatLng( pref.getFloat(Constants.KEY_PREF_HOME_ADDRESS_LAT, Constants.DEFAULT_HOME_ADDRESS_LAT),
                pref.getFloat(Constants.KEY_PREF_HOME_ADDRESS_LON, Constants.DEFAULT_HOME_ADDRESS_LON));
        return homeGeo;
    }
}
