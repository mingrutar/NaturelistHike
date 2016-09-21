package com.coderming.naturalisthike.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by linna on 9/6/2016.
 */
public class TripCheckListDataHelper implements DataConstants{
    private static final String LOG_TAG = TripCheckListDataHelper.class.getSimpleName();


    public static Cursor findChecklistItem(Context context, String item, TripContract.CheckListSelectionType type) {
        String sel = String.format(QuerySdSqs, TripContract.CheckListEntry.COLUMN_TYPE, type.getValue(),
                TripContract.CheckListEntry.COLUMN_NAME, item );
        return context.getContentResolver().query(TripContract.CheckListEntry.CONTENT_URI, null,
                sel, null, null, null);
//                TripContract.CheckListEntry.COLUMN_TYPE+"=? AND "+TripContract.CheckListEntry.COLUMN_NAME+"=?",
//                new String[] {Integer.toString(type.getValue()), item}, null, null);
    }
    public static Cursor findLeaderChecklistItem(Context context, long tripId, String item) {
        TripContract.CheckListSelectionType type = TripContract.CheckListSelectionType.Leader;
        String sel = String.format(QuerySdSqSSd, TripContract.CheckListEntry.COLUMN_TYPE,
                type.getValue(), TripContract.CheckListEntry.COLUMN_NAME, item,
                TripContract.CheckListEntry.COLUMN_TRIP_ID, tripId);
        return context.getContentResolver().query(TripContract.CheckListEntry.CONTENT_URI, null,
                sel, null, null, null);
//                TripContract.CheckListEntry.COLUMN_TYPE+"=? AND "+TripContract.CheckListEntry.COLUMN_NAME+"=? AND "
//                + TripContract.CheckListEntry.COLUMN_TRIP_ID + "=?",
//                new String[] {Integer.toString(type.getValue()), item, Long.toString(tripId)}, null, null);
    }
    public static Uri upsertClubCheckList(Context context, String item) {
        if ((context == null) || (item == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        TripContract.CheckListSelectionType type = TripContract.CheckListSelectionType.Club;
        Cursor cursor = findChecklistItem(context, item, type);
        if ((cursor!=null) && cursor.moveToFirst()) {  //update is not allowed for club item
            long dbId = cursor.getLong(0);
            return TripContract.CheckListEntry.buildUri(dbId);
        } else {
            ContentValues values = new ContentValues();
            values.put(TripContract.CheckListEntry.COLUMN_NAME, item);
            values.put(TripContract.CheckListEntry.COLUMN_TYPE, type.getValue());
            values.put(TripContract.CheckListEntry.COLUMN_IS_OPTIONAL, 0);
            return context.getContentResolver().insert(TripContract.CheckListEntry.CONTENT_URI, values);
        }
    }
    public static Uri upsertLeaderCheckList(Context context, long tripId, String item, boolean isOptional) {
        if ((context == null) || (item == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        TripContract.CheckListSelectionType type = TripContract.CheckListSelectionType.Leader;
        ContentValues values = new ContentValues();
        Cursor cursor = findLeaderChecklistItem(context, tripId, item);
        if ((cursor!=null) && cursor.moveToFirst()) {  //update is not allowed for leader item
            long dbId = cursor.getLong(0);
            return TripContract.CheckListEntry.buildUri(dbId);
        } else {
            values.put(TripContract.CheckListEntry.COLUMN_NAME, item);
            values.put(TripContract.CheckListEntry.COLUMN_IS_OPTIONAL, isOptional ? 1 : 0);
            values.put(TripContract.CheckListEntry.COLUMN_TYPE, type.getValue());
            values.put(TripContract.CheckListEntry.COLUMN_TRIP_ID, tripId);
            return context.getContentResolver().insert(TripContract.CheckListEntry.CONTENT_URI, values);
        }
    }
    public static Uri upsertMyCheckList(Context context, String item, boolean isOptional) {
        if ((context == null) || (item == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        TripContract.CheckListSelectionType type = TripContract.CheckListSelectionType.Personal;
        ContentValues values = new ContentValues();
        Cursor cursor = findChecklistItem(context, item, type);
        if ((cursor != null) && cursor.moveToFirst()) {  //update is not allowed for club item
            long dbId = cursor.getLong(0);
            values.put(TripContract.CheckListEntry.COLUMN_IS_OPTIONAL, isOptional ? 1 : 0);
            int num = context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                    TripContract.CheckListEntry._ID + QueryPS, new String[] {Long.toString(dbId)});
            if (num > 0) {
                return TripContract.CheckListEntry.buildUri(dbId);
            } else {
                return null;
            }
        } else {
            values.put(TripContract.CheckListEntry.COLUMN_NAME, item);
            values.put(TripContract.CheckListEntry.COLUMN_IS_OPTIONAL, isOptional ? 1 : 0);
            values.put(TripContract.CheckListEntry.COLUMN_TYPE, type.getValue());
            return context.getContentResolver().insert(TripContract.CheckListEntry.CONTENT_URI, values);
        }
    }
    // todo: add unit test if needed
    public static int setCheckState(Context context, long id, TripContract.CheckListSelectionType type, boolean isChecked) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_IS_CHECKED, isChecked ? 1 : 0);
        values.put(TripContract.CheckListEntry.COLUMN_TYPE, type.getValue());
        return context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                TripContract.CheckListEntry._ID+QueryPS, new String[] { Long.toString(id) } );

    }
    public static int setAlwaysCheckState(Context context, long id, TripContract.CheckListSelectionType type, boolean isChecked) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_IS_ALWAYS_CHECK, isChecked ? 1 : 0);
        values.put(TripContract.CheckListEntry.COLUMN_TYPE, type.getValue());
        return context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                TripContract.CheckListEntry._ID+QueryPS, new String[] { Long.toString(id) } );

    }
    // todo: unittest
    public static int deleteChecklist(Context context, long id) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_TYPE, TripContract.CheckListSelectionType.Personal.getValue());
        return context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                TripContract.CheckListEntry._ID+QueryPS, new String[] { Long.toString(id) } );
    }

    public static Cursor getAllChecklistItem(Context context, long tripId) {
        return context.getContentResolver().query(TripContract.CheckListEntry.CONTENT_URI, null,
                null, null, null, null);
    }
    public static Cursor getClubChecklistItem(Context context) {
        return context.getContentResolver().query(TripContract.CheckListEntry.CONTENT_CLUB_URI, null,
                null, null, null, null);
    }
    public static Cursor getLeaderChecklistItem(Context context, long tripId) {
        Uri uri = TripContract.CheckListEntry.buildLeaderCLByTripUri(tripId);
        return context.getContentResolver().query(uri, null, null, null, null, null);
    }
    public static Cursor getMyChecklistItem(Context context) {

        return context.getContentResolver().query(TripContract.CheckListEntry.CONTENT_PERSONAL_URI, null,
                null, null, null, null);
    }

    public static boolean setCLItemCheck(Context context, long dbId, boolean check) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_IS_CHECKED, check?1:0);
        String sel = String.format("%s=%d", TripContract.CheckListEntry._ID, dbId);
        int num = context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                sel, null);
//                TripContract.CheckListEntry._ID+QueryPS, new String[] {Long.toString(dbId)});
        return (num > 0);
    }

    public static boolean setCLItemCheckAndRemember(Context context, long dbId, boolean check) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_IS_CHECKED, check?1:0);
        values.put(TripContract.CheckListEntry.COLUMN_IS_ALWAYS_CHECK, true);
        int num = context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                TripContract.CheckListEntry._ID+QueryPS, new String[] {Long.toString(dbId)});
        return (num > 0);
    }
    public static boolean setCLItemReset(Context context, long dbId) {
        ContentValues values = new ContentValues();
        values.put(TripContract.CheckListEntry.COLUMN_IS_ALWAYS_CHECK, false);
        int num = context.getContentResolver().update(TripContract.CheckListEntry.CONTENT_URI, values,
                TripContract.CheckListEntry._ID+QueryPS, new String[] {Long.toString(dbId)});
        return (num > 0);
    }

}
