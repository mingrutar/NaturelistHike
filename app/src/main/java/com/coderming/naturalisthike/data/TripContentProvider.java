package com.coderming.naturalisthike.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TripContentProvider extends ContentProvider implements DataConstants {
    private static final String LOG_TAG = TripContentProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static public final int TRIP = 100;
    static public final int TRIP_BY_ID = 120;

    static public final int HIKER = 200;
    static public final int HIKER_BY_ID = 220;
    static public final int TRIP_LIST_BY_HIKER = 240;

    static public final int ROSTER = 300;
    static public final int ROSTER_BY_ID = 310;                 // /*

    static public final int TRIP_PLANT = 400;
    static public final int TRIP_PLANT_ITEM = 450;

    static public final int CHECK_LIST = 500;
    static public final int CHECK_LIST_CLUB = 510;
    static public final int CHECK_LIST_LEADER = 512;
    static public final int CHECK_LIST_PERSONAL = 513;
    static public final int CHECK_LIST_BY_ID = 550;
    static public final int CHECK_LIST_PERSONAL_BY_ID = 551;

    TripDBHelper mDBHelper;
    static Map<Integer, String> mMatchTable;
    static {
        mMatchTable = new HashMap<>();
        mMatchTable.put(TRIP, TripContract.TripEntry.TABLE_NAME);
        mMatchTable.put(ROSTER, TripContract.RosterEntry.TABLE_NAME);
        mMatchTable.put(CHECK_LIST_CLUB, TripContract.CheckListEntry.TABLE_NAME);
        mMatchTable.put(CHECK_LIST_LEADER, TripContract.CheckListEntry.TABLE_NAME);
    }

    static final String sTripPlantProjection =
            PlantContract.PlantEntry.PROJECTION+","+TripContract.PlantTripEntry.PROJECTION;
    static final SQLiteQueryBuilder sLeaderPlantQueryBuilder;
    static{
        sLeaderPlantQueryBuilder = new SQLiteQueryBuilder();
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sLeaderPlantQueryBuilder.setTables(
            TripContract.PlantTripEntry.TABLE_NAME+" LEFT JOIN " +
            PlantContract.PlantEntry.TABLE_NAME + " ON "+
            TripContract.PlantTripEntry.TABLE_NAME+"."+ TripContract.PlantTripEntry.COLUMN_PLANT_ID +"="+
            PlantContract.PlantEntry.TABLE_NAME+"."+ PlantContract.PlantEntry._ID  );
    }

    public TripContentProvider() {
    }

    static public UriMatcher buildUriMatcher() {
        UriMatcher ret = new UriMatcher(UriMatcher.NO_MATCH);
        ret.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_TRIP, TRIP);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSId, TripContract.PATH_TRIP), TRIP_BY_ID);

        ret.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_HIKER, HIKER);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSId, TripContract.PATH_HIKER), HIKER_BY_ID);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSSId, TripContract.PATH_HIKER,
             TripContract.HikerEntry.TRIPS), TRIP_LIST_BY_HIKER);

        ret.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_ROSTER, ROSTER);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSAny, TripContract.PATH_ROSTER), ROSTER_BY_ID);

        ret.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_TRIP_PLANT, TRIP_PLANT);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSAny,TripContract.PATH_TRIP_PLANT),
                TRIP_PLANT_ITEM);

        ret.addURI(TripContract.CONTENT_AUTHORITY, TripContract.PATH_CHECK_LIST, CHECK_LIST);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSS,TripContract.PATH_CHECK_LIST,
                TripContract.CheckListSelectionType.Club), CHECK_LIST_CLUB);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSS,TripContract.PATH_CHECK_LIST,
                TripContract.CheckListSelectionType.Personal), CHECK_LIST_PERSONAL);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSSId,TripContract.PATH_CHECK_LIST,
                TripContract.CheckListSelectionType.Leader), CHECK_LIST_LEADER);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSSId,TripContract.PATH_CHECK_LIST,
                TripContract.CheckListSelectionType.Personal), CHECK_LIST_PERSONAL_BY_ID);
        ret.addURI(TripContract.CONTENT_AUTHORITY, String.format(UriSAny, TripContract.PATH_CHECK_LIST), CHECK_LIST_BY_ID);
        return ret;
    }

    @Override
    public String getType(Uri uri) {
        if (uri != null) {
            int match = sUriMatcher.match(uri);
//            Log.v(LOG_TAG, "++getType: uri="+uri.toString()+", matchCode="+ Integer.toString(match));
            switch (match) {
                case TRIP:              //trip
                    return TripContract.TripEntry.CONTENT_TYPE;
                case TRIP_BY_ID:
                    return TripContract.TripEntry.CONTENT_ITEM_TYPE;
                case HIKER:             //hiker
                    return TripContract.HikerEntry.CONTENT_TYPE;
                case HIKER_BY_ID:             //hiker
                    return TripContract.HikerEntry.CONTENT_ITEM_TYPE;
                case TRIP_LIST_BY_HIKER:
                    return TripContract.HikerEntry.CONTENT_TYPE;
                case ROSTER:                    // roster of all trip
                case ROSTER_BY_ID:              // roast of a trip
                    return TripContract.RosterEntry.CONTENT_TYPE;
                case TRIP_PLANT:               // all trip_plant
                     return TripContract.PlantTripEntry.CONTENT_TYPE;
                case TRIP_PLANT_ITEM:
                    return TripContract.PlantTripEntry.CONTENT_ITEM_TYPE;
                case CHECK_LIST:                            // check list
                case CHECK_LIST_CLUB:
                case CHECK_LIST_LEADER:
                case CHECK_LIST_PERSONAL:
                    return TripContract.CheckListEntry.CONTENT_TYPE;
                case CHECK_LIST_BY_ID:
                case CHECK_LIST_PERSONAL_BY_ID:
                    return TripContract.CheckListEntry.CONTENT_ITEM_TYPE;
            }
        }
        return null;
    }
    @Override
    public boolean onCreate() {
        mDBHelper = new TripDBHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int matchCode = sUriMatcher.match(uri);
        Uri ret = null;
        long id = 0;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (matchCode == TRIP) {
            id = db.insert(TripContract.TripEntry.TABLE_NAME, null, values);
            if (id != -1) {
                ret = TripContract.TripEntry.buildUri(id);
            }
        } else if (matchCode == HIKER) {
            id = db.insert(TripContract.HikerEntry.TABLE_NAME, null, values);
            if (id != -1) {
                ret = TripContract.HikerEntry.buildUri(id);
            }
        } else if (matchCode == ROSTER) {
            id = db.insert(TripContract.RosterEntry.TABLE_NAME, null, values);
            if (id != -1) {
                ret = TripContract.RosterEntry.buildUri(id);
            }
        } else if (matchCode == CHECK_LIST) {
            id = db.insert(TripContract.CheckListEntry.TABLE_NAME, null, values);
            if (id != -1) {
                ret = TripContract.CheckListEntry.buildUri(id);
            }
        } else if (matchCode == TRIP_PLANT) {
            id = db.insert(TripContract.PlantTripEntry.TABLE_NAME, null, values);
            if (id != -1) {
                ret = TripContract.PlantTripEntry.buildUri(id);
            }
        }
        if (id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            Log.e(LOG_TAG, "Failed to insert row uri=" + uri);
//                throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        return ret;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        int matchCode = sUriMatcher.match(uri);
        int deleteCount = 0;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
//        Log.v(LOG_TAG, String.format("+++++query: matchCode=%d,proj=%s,sel==%s,selArg=%s,sort=%s",
//                matchCode, Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder));
        if (matchCode == TRIP) {             // TODO: do not include past trips
            cursor = db.query(TripContract.TripEntry.TABLE_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == TRIP_BY_ID) {
            cursor = db.query(TripContract.TripEntry.TABLE_NAME, projection,
                    String.format(QuerySQ, TripContract.TripEntry._ID),
                    new String[] { uri.getLastPathSegment() },
                    null, null, sortOrder);
        } else if (matchCode == TRIP_LIST_BY_HIKER) {
            cursor = db.query(TripContract.HikerEntry.TABLE_NAME, projection,
                    String.format(QuerySQ, TripContract.HikerEntry.COLUMN_EMAIL),
                    new String[]{uri.getLastPathSegment()}, null, null, sortOrder);
          } else if (matchCode == ROSTER) {
            cursor = db.query(TripContract.RosterEntry.VIEW_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == ROSTER_BY_ID) {
            cursor = db.query(TripContract.RosterEntry.VIEW_NAME, projection,
                    String.format(QuerySQ, TripContract.RosterEntry._ID),
                    new String[]{uri.getLastPathSegment()}, null, null, sortOrder);
       } else if (matchCode == TRIP_PLANT) {
            if (projection == null) {
                projection = sTripPlantProjection.split(DelimCommon);
            }
            cursor = sLeaderPlantQueryBuilder.query(db, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == CHECK_LIST) {
            cursor = db.query(TripContract.CheckListEntry.TABLE_NAME, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == CHECK_LIST_CLUB) {
            cursor = db.query(TripContract.CheckListEntry.CLUB_VIEW, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == CHECK_LIST_LEADER) {
            cursor = db.query(TripContract.CheckListEntry.LEADER_VIEW, projection,
                    String.format(QuerySQ, TripContract.CheckListEntry.COLUMN_TRIP_ID),
                    new String[] {uri.getLastPathSegment()}, null, null, sortOrder);
        } else if (matchCode == CHECK_LIST_PERSONAL) {
            cursor = db.query(TripContract.CheckListEntry.MY_VIEW, projection,
                    selection, selectionArgs, null, null, sortOrder);
        } else {
            Log.i(LOG_TAG, "query: not supported Uri for query, uri="+uri);
        }
        if (cursor != null) {
//            Log.v(LOG_TAG, String.format("+++query: valid cursor for matchCode=%d, selction=", matchCode, selection));
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchCode = sUriMatcher.match(uri);
        int deleteCount = 0;
//        if (null == selection) {    // null will delete all rows
//            selection = "1";
//        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (matchCode == TRIP) {
            deleteCount = db.delete(TripContract.TripEntry.TABLE_NAME,
                    selection, selectionArgs);
        } else if (matchCode == HIKER) {      // someone removed from trip
            deleteCount = db.delete(TripContract.HikerEntry.TABLE_NAME,
                    selection, selectionArgs);
        } else if (matchCode == TRIP_PLANT) {
            deleteCount = db.delete(TripContract.PlantTripEntry.TABLE_NAME,
                    selection, selectionArgs);
        } else if (matchCode == ROSTER) {      // someone removed from trip
            deleteCount = db.delete(TripContract.RosterEntry.TABLE_NAME,
                    selection, selectionArgs);
        } else if (matchCode == CHECK_LIST) {
            deleteCount = db.delete(TripContract.CheckListEntry.TABLE_NAME,
                    selection, selectionArgs);
        } else {
            Log.i(LOG_TAG, "delete: not supported Uri for deletion, uri="+uri);
        }
        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int updateCount = 0;
        int matchCode = sUriMatcher.match(uri);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (matchCode == TRIP) {
            updateCount = db.update(TripContract.TripEntry.TABLE_NAME, values,
                    selection, selectionArgs);
        } else if (matchCode == ROSTER) {
            updateCount = db.update(TripContract.RosterEntry.TABLE_NAME, values,
                    selection, selectionArgs);
        } else if (matchCode == CHECK_LIST){
            updateCount = db.update(TripContract.CheckListEntry.TABLE_NAME, values,
                    selection, selectionArgs);
        } else if (matchCode == TRIP_PLANT) {
            updateCount = db.update(TripContract.PlantTripEntry.TABLE_NAME, values,
                    selection, selectionArgs);
        }
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int matchCode = sUriMatcher.match(uri);
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (mMatchTable.containsKey(matchCode)) {
            db.beginTransaction();
            int returnCount = 0;
            try {
                for (ContentValues value : values) {
                    long _id = db.insert(mMatchTable.get(matchCode), null, value);
                    if (_id != -1) {
                        returnCount++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return returnCount;
        } else {
            Log.i(LOG_TAG, "bulkInsert: unsupported Uri +" + uri.toString());
            return super.bulkInsert(uri, values);
        }
    }
}
