package com.coderming.naturalisthike.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by linna on 9/6/2016.
 */
public class TripPlantDataHelper implements DataConstants {
    private static final String LOG_TAG = TripPlantDataHelper.class.getSimpleName();

    // trip plants
    public static Uri insertLeaderSpeciesList(Context context, long tripId, String specisName, TripContract.SpeciesType type) {
        if ((context == null) || (specisName == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        specisName = specisName.toLowerCase();
        Uri uri = null;
        if (type == TripContract.SpeciesType.Plant) {
            long plantId = PlantDataHelper.findPlant(context, specisName);
            if (plantId > 0) {
                ContentValues values = new ContentValues();
                values.put(TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId);
                values.put(TripContract.PlantTripEntry.COLUMN_PLANT_ID, plantId);
                values.put(TripContract.PlantTripEntry.COLUMN_IS_LEADER_LIST, 1);
                uri = context.getContentResolver().insert(TripContract.PlantTripEntry.CONTENT_URI, values);
                return uri;
            }
        }
        // plants not found in plant list
        ContentValues values = new ContentValues();
        values.put(TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId);
        values.put(TripContract.PlantTripEntry.COLUMN_SPECIES_NAME, specisName);
        values.put(TripContract.PlantTripEntry.COLUMN_SPECIES_TYPE, type.getValue());
        values.put(TripContract.PlantTripEntry.COLUMN_IS_LEADER_LIST, 1);
        uri = context.getContentResolver().insert(TripContract.PlantTripEntry.CONTENT_URI, values);
        return uri;
    }
    public static Cursor getLeaderSpeciesList(Context context, long tripId) {
        if (context == null) {
            Log.w(LOG_TAG, "getLeaderSpeciesList all params must be non-null");
            return null;
        }
        String sel = String.format(QuerySdAndS1, TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId,
                TripContract.PlantTripEntry.COLUMN_IS_LEADER_LIST);
        return context.getContentResolver().query(TripContract.PlantTripEntry.CONTENT_URI, null,
                sel, null, null, null);
    }
    public static long findIdPlantOnTripPlantList(Context context, long tripId, long plantId) {
        long DBId = -1;
        String sel = String.format(QuerySdAndSd,
                TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId,
                TripContract.PlantTripEntry.COLUMN_PLANT_ID, plantId);
        Cursor cursor = context.getContentResolver().query(TripContract.PlantTripEntry.CONTENT_URI,
                new String[] {TripContract.PlantTripEntry.TABLE_NAME + DelimPeriod  +
                        TripContract.PlantTripEntry._ID}, sel, null, null, null);
//                TripContract.PlantTripEntry.COLUMN_TRIP_ID+"=? AND "+TripContract.PlantTripEntry.COLUMN_PLANT_ID+"=?",
//                new String[] {Long.toString(tripId), Long.toString(plantId)}, null, null);
        if ((cursor!=null) && cursor.moveToFirst()) {
            DBId = cursor.getLong(0);
        }
        return DBId;
    }
    public static Uri upsertObservedPlant(Context context, long tripId, long plantId ) {
        if (context == null) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        ContentValues values = new ContentValues();
        long dbId = findIdPlantOnTripPlantList(context, tripId, plantId);
        if (dbId != -1) {
            values.put(TripContract.PlantTripEntry.COLUMN_OBSERVED, 1);
            int num = context.getContentResolver().update(TripContract.PlantTripEntry.CONTENT_URI, values,
                    TripContract.PlantTripEntry._ID + QueryPS, new String[] {Long.toString(dbId)});
            if (num > 0) {
                return TripContract.PlantTripEntry.buildUri(dbId);
            } else {
                return null;
            }
        } else {
            values.put(TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId);
            values.put(TripContract.PlantTripEntry.COLUMN_PLANT_ID, plantId);
            values.put(TripContract.PlantTripEntry.COLUMN_OBSERVED, 1);
            return context.getContentResolver().insert(TripContract.PlantTripEntry.CONTENT_URI, values);
        }
    }
    public static Cursor getObservedPlantList(Context context) {
        return context.getContentResolver().query(TripContract.PlantTripEntry.CONTENT_URI, null,
                TripContract.PlantTripEntry.COLUMN_OBSERVED + QueryEQTrue, null, null);
//                new String[] { "1" } , null, null);
    }
    public static Uri upsertPlantPhoto(Context context, long tripId, long plantId, String photoUri, String voiceUri ) {
        if ((context == null) || (photoUri == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        ContentValues values = new ContentValues();
        long dbId = -1;
        if (plantId != -1) {
            dbId = findIdPlantOnTripPlantList(context, tripId, plantId);
        }
        if (dbId != -1) {
            values.put(TripContract.PlantTripEntry.COLUMN_OBSERVED, 1);
            values.put(TripContract.PlantTripEntry.COLUMN_PHOTO_URI, photoUri);
            if (voiceUri != null) {
                values.put(TripContract.PlantTripEntry.COLUMN_VOICE_URI, voiceUri);
            }
            int num = context.getContentResolver().update(TripContract.PlantTripEntry.CONTENT_URI, values,
                    TripContract.PlantTripEntry._ID + QueryPS, new String[] {Long.toString(dbId)});
            if (num > 0) {
                return TripContract.PlantTripEntry.buildUri(dbId);
            } else {
                return null;
            }
        } else {
            values.put(TripContract.PlantTripEntry.COLUMN_TRIP_ID, tripId);
            values.put(TripContract.PlantTripEntry.COLUMN_PLANT_ID, plantId);
            values.put(TripContract.PlantTripEntry.COLUMN_OBSERVED, 1);
            values.put(TripContract.PlantTripEntry.COLUMN_PHOTO_URI, photoUri);
            if (voiceUri != null) {
                values.put(TripContract.PlantTripEntry.COLUMN_VOICE_URI, voiceUri);
            }
            Uri uri = context.getContentResolver().insert(TripContract.PlantTripEntry.CONTENT_URI, values);
            return uri;
        }
    }
    public static Cursor getAllTripPlantList(Context context, long tripId) {
        String sel = String.format(QuerySd, TripContract.RosterEntry.COLUMN_TRIP_ID, tripId);
        Cursor cursor = context.getContentResolver().query(TripContract.PlantTripEntry.CONTENT_URI, null,
                sel, null, null, null);
//                TripContract.RosterEntry.COLUMN_TRIP_ID+"=? AND "+TripContract.PlantTripEntry.COLUMN_PLANT_ID+" is null ",
//                new String[]{Long.toString(tripId)}, null, null);
//        Log.v(LOG_TAG, "getAllTripPlantList get cursor "+ ((cursor==null)?"null":"valid"));
        return cursor;
    }

    public static Uri removeObservedFlag(Context context, long planttripId) {
        ContentValues values = new ContentValues();
        values.put(TripContract.PlantTripEntry.COLUMN_OBSERVED, 0);
        String sel = String.format(QuerySd,TripContract.PlantTripEntry._ID, planttripId );
        int num = context.getContentResolver().update(TripContract.PlantTripEntry.CONTENT_URI, values,
                sel, null);
//                TripContract.PlantTripEntry._ID+"=?", new String[] {Long.toString(planttripId)});
        if (num > 0) {
            return TripContract.PlantTripEntry.buildUri(planttripId);
        } else {
            return null;
        }
    }

}
