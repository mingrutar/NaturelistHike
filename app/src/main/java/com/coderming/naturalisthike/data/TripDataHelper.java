package com.coderming.naturalisthike.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by linna on 8/28/2016.
 * trip is insert in TripDataFetcher
 */
public class TripDataHelper implements  DataConstants{
    private static final String LOG_TAG = TripDataHelper.class.getSimpleName();
    public static final String[] sId = new String[] {BaseColumns._ID};

    // trip
    static final String sCurrentTripSelection = TripContract.TripEntry.COLUMN_HIKE_DATE + QueryGE ;

    public static Cursor getTripList(Context context) {
        Calendar now = Calendar.getInstance();
        Cursor cursor = context.getContentResolver().query(TripContract.TripEntry.CONTENT_URI, null,
                sCurrentTripSelection, new String[]{Long.toString(now.getTimeInMillis())},
                TripContract.TripEntry.COLUMN_HIKE_DATE+QueryAsc, null);
        return cursor;
    }
    public static Cursor getATrip(Context context, long tripId) {
        Date now = new Date();
        Cursor cursor = context.getContentResolver().query(TripContract.TripEntry.CONTENT_URI, null,
                TripContract.TripEntry._ID+QueryPS, new String[]{Long.toString(tripId)}, null, null);
        return cursor;
    }
    // used at load data
    public static int updateHomeMpDrivingTime(Context context, long tripId, long time) {
        ContentValues values = new ContentValues();
        values.put(TripContract.TripEntry.COLUMN_HOME_MP_DRIVINGTIME, time);
        int ret = context.getContentResolver().update(TripContract.TripEntry.CONTENT_URI,
                values, TripContract.TripEntry._ID+QueryPS, new String[] {Long.toString(tripId)} );
//        Log.v(LOG_TAG, "updateHomeMpDrivingTime, ret="+Integer.toString(ret));
        return ret;
    }
    // used at load data
    public static int updateTHTimeandDriving(Context context, long dbId, long atTH, long time) {
        ContentValues values = new ContentValues();
        values.put(TripContract.TripEntry.COLUMN_AT_TRAILHEAD_TIME, atTH);
        values.put(TripContract.TripEntry.COLUMN_MP_TH_DRIVINGTIME, time);
        int ret = context.getContentResolver().update(TripContract.TripEntry.CONTENT_URI,
                values, TripContract.TripEntry._ID+QueryPS, new String[] {Long.toString(dbId)} );
//        Log.v(LOG_TAG, "updateMP2THDrivingTime, ret="+Integer.toString(ret));
        return ret;
    }
    public static int updateMyMeetingPlace(Context context, long dbId, String name, long meettime,
                                           LatLng geo, long home2MP, long MP2TH) {
        ContentValues values = new ContentValues();
        values.put(TripContract.TripEntry.COLUMN_MY_MEETING_PLACE, name);
        values.put(TripContract.TripEntry.COLUMN_MY_MEETING_TIME, meettime);
        values.put(TripContract.TripEntry.COLUMN_MY_MP_LATITUDE, geo.latitude);
        values.put(TripContract.TripEntry.COLUMN_MY_MP_LONGITUDE, geo.longitude);
        if (home2MP != -1) {
            values.put(TripContract.TripEntry.COLUMN_HOME_MP_DRIVINGTIME, home2MP);
        }
        if (MP2TH != -1) {
            values.put(TripContract.TripEntry.COLUMN_MP_TH_DRIVINGTIME, MP2TH);
        }
        int ret = context.getContentResolver().update(TripContract.TripEntry.CONTENT_URI,
                values, TripContract.TripEntry._ID+QueryPS, new String[] {Long.toString(dbId)} );
//        Log.v(LOG_TAG, "updateMyMeetingPlace, ret="+Integer.toString(ret));
        return ret;
    }
    public static Cursor getAllTrips(Context context) {
        Cursor cursor = context.getContentResolver().query(TripContract.TripEntry.CONTENT_URI,
                null, null, null, null);
        return cursor;
    }
    // hiker
    @Nullable
    public static Uri upsertHiker(Context context, long tripId, String name, String email, TripContract.HikerType type ) {
        if ((context == null) || (name == null) || (email == null)) {
            // todo: validate email
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        name = name.toLowerCase();
        email = email.toLowerCase();
        Uri uri = null;
        Cursor cursor = findHikerByEmail(context, email);
        long hikerId = -1;
        if ((cursor != null) && (cursor.moveToFirst())) {  // already inserted
            hikerId = cursor.getLong(0);
        } else {
            ContentValues values = new ContentValues();
            String[] names = name.split(DelimSpace);
            if (names.length == 2) {
                values.put(TripContract.HikerEntry.COLUMN_FNAME, names[0].toLowerCase());
                values.put(TripContract.HikerEntry.COLUMN_LNAME, names[1].toLowerCase());
                values.put(TripContract.HikerEntry.COLUMN_EMAIL, email.toLowerCase());
                uri = context.getContentResolver().insert(TripContract.HikerEntry.CONTENT_URI, values);
                if (uri != null) {
                    hikerId = Long.parseLong(uri.getLastPathSegment());
                }
            }
        }
        if (hikerId > 0) {
            ContentValues values = new ContentValues();
            values.put(TripContract.RosterEntry.COLUMN_TRIP_ID, tripId);
            values.put(TripContract.RosterEntry.COLUMN_HIKER_ID, hikerId);
            values.put(TripContract.RosterEntry.COLUMN_TYPE, type.getValue());
            uri = context.getContentResolver().insert(TripContract.RosterEntry.CONTENT_URI, values);
        } else {
            Log.w(LOG_TAG, "insertHiker with email "+email+" failed");
            uri = null;
        }
        return uri;
    }
    public static Cursor findTripByUrl(Context context, String url) {
        long hikeId = -1;
        url = url.toLowerCase();
        String sel = String.format(QuerySqS,TripContract.TripEntry.COLUMN_TRIP_URL, url);
//        Log.v(LOG_TAG, "+++findTripByUrl: sel="+sel);
        Cursor cursor = context.getContentResolver().query(TripContract.TripEntry.CONTENT_URI,
                null, sel, null, null, null);
        return cursor;

    }
//    static final String sEmailSelection=TripContract.HikerEntry.COLUMN_EMAIL+QueryPS;
    public static Cursor findHikerByEmail(Context context, String email) {
        long hikeId = -1;
        email = email.toLowerCase();
        String sel = String.format(QuerySqS,TripContract.HikerEntry.COLUMN_EMAIL, email);
//        Log.v(LOG_TAG, "+++findHikerByEmail: sel="+sel);
        Cursor cursor = context.getContentResolver().query(TripContract.RosterEntry.CONTENT_URI,
                null, sel, null, null, null);
//        null, sEmailSelection, new String[] {email}, null, null);
        return cursor;
    }
    // roster
//    static String sTripSelection = TripContract.RosterEntry.COLUMN_TRIP_ID+QueryPS;
    public static Cursor getTripRoster(Context context, long tripId) {
        String sel = TripContract.RosterEntry.COLUMN_TRIP_ID+QueryEQ+Long.toString(tripId);
        Cursor cursor = context.getContentResolver().query(TripContract.RosterEntry.CONTENT_URI,
                null, sel, null , null, null);
//        null, sTripSelection, new String[]{Long.toString(tripId)}, null, null);
        return cursor;
    }

    public static Cursor getTripLeaders(Context context, long tripId) {
        TripContract.HikerType type = TripContract.HikerType.Leader;
        String sel = String.format(QuerySdAndSd, TripContract.RosterEntry.COLUMN_TRIP_ID, tripId,
                TripContract.RosterEntry.COLUMN_TYPE, type.getValue() );
        Cursor cursor = context.getContentResolver().query(TripContract.RosterEntry.CONTENT_URI, null,
                  sel, null, null, null);
//                TripContract.RosterEntry.COLUMN_TRIP_ID+"=? AND "+ TripContract.RosterEntry.COLUMN_TYPE+QueryPS,
//                new String[]{Long.toString(tripId), Integer.toString(type.getValue())}, null, null);
        return cursor;
    }

}
