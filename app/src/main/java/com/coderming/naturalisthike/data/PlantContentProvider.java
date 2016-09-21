package com.coderming.naturalisthike.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class PlantContentProvider extends ContentProvider implements DataConstants {
    private static final String LOG_TAG = PlantContentProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    static private final int PLANT = 100;
    static private final int PLANT_FAVORITE = 110;      // /*
    static private final int PLANT_BY_ID = 120;
    static private final int PLANT_BY_SCIENTIFIC = 130;

    static private final int PLANT_ALIAS = 200;
    static private final int PLANT_ALIAS_BY_SCIENTIFIC = 220;
//    static public final int PLANT_ALIAS_BY_ID = 250;
    TripDBHelper mDBHelper;

    static public UriMatcher buildUriMatcher() {
        UriMatcher ret = new UriMatcher(UriMatcher.NO_MATCH);
        ret.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLANTS, PLANT);
        ret.addURI(PlantContract.CONTENT_AUTHORITY, String.format(UriSS,
                PlantContract.PATH_PLANTS, PlantContract.PlantEntry.FAVORITE), PLANT_FAVORITE);
        ret.addURI(PlantContract.CONTENT_AUTHORITY,
                String.format(UriSSAny, PlantContract.PATH_PLANTS, PlantContract.SCIENTIFIC), PLANT_BY_SCIENTIFIC);
        ret.addURI(PlantContract.CONTENT_AUTHORITY,
                String.format(UriSId, PlantContract.PATH_PLANTS), PLANT_BY_ID);

        ret.addURI(PlantContract.CONTENT_AUTHORITY, PlantContract.PATH_PLAN_ALLIAS, PLANT_ALIAS);
        ret.addURI(PlantContract.CONTENT_AUTHORITY,
                String.format(UriSS,PlantContract.PATH_PLAN_ALLIAS, PlantContract.SCIENTIFIC), PLANT_ALIAS_BY_SCIENTIFIC);
//        ret.addURI(PlantContract.CONTENT_AUTHORITY,
//                String.format("%s/#",PlantContract.PATH_PLAN_ALLIAS), PLANT_ALIAS_ITEM);
        return ret;
    }

    static final SQLiteQueryBuilder sPlanCommonNameBuilder = new SQLiteQueryBuilder();
    static {
        sPlanCommonNameBuilder.setTables(PlantContract.AliasEntry.TABLE_NAME + " INNER JOIN " +
                PlantContract.PlantEntry.TABLE_NAME + " ON "+
                PlantContract.AliasEntry.TABLE_NAME+"."+PlantContract.AliasEntry.COLUMN_PLANT_ID
                + " = " + PlantContract.PlantEntry.TABLE_NAME+"."+PlantContract.PlantEntry._ID );
    }
    @Override
    public boolean onCreate() {
        mDBHelper = new TripDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String ret = null;
        if (null != uri) {
            int match = sUriMatcher.match(uri);
            if (match == PLANT) {
                ret = PlantContract.PlantEntry.CONTENT_TYPE;
            } else if (match == PLANT_FAVORITE) {
                ret = PlantContract.PlantEntry.CONTENT_TYPE;
            } else if (match == PLANT_BY_ID) {
                ret = PlantContract.PlantEntry.CONTENT_ITEM_TYPE;
            } else if (match == PLANT_BY_SCIENTIFIC) {
                ret = PlantContract.PlantEntry.CONTENT_ITEM_TYPE;
            } else if (match == PLANT_ALIAS) {
                ret = PlantContract.AliasEntry.CONTENT_TYPE;
            } else if (match == PLANT_ALIAS_BY_SCIENTIFIC) {
                ret = PlantContract.AliasEntry.CONTENT_TYPE;
            }
        }
        if (ret != null) {
            Log.v(LOG_TAG, "getType: for uri="+uri.toString()+", ret="+ret);
        } else {
            Log.i(LOG_TAG, "getType: NOT find type for uri="+uri.toString());
        }
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int matchCode = sUriMatcher.match(uri);
        Uri ret = null;
        long id = 0;
        SQLiteDatabase db = null;
        db = mDBHelper.getWritableDatabase();
        if (matchCode == PLANT) {
            id = db.insert(PlantContract.PlantEntry.TABLE_NAME, null, values);
//            Log.v(LOG_TAG, "insert: matchCode=PLANT, id=" + Long.toString(id));
            if (id > 0) {
                ret = PlantContract.PlantEntry.buildUri(id);
            }
        } else if (matchCode == PLANT_ALIAS) {        // used in add favorite
            long dbId = db.insert(PlantContract.AliasEntry.TABLE_NAME, null, values);
//            Log.v(LOG_TAG, "insert matchCode=PLANT_ALIAS, dbid=" + Long.toString(dbId));
            if (dbId > 0) {
                ret = PlantContract.AliasEntry.buildUri(dbId);
            }
        }
        if (ret == null) {
            Log.e(LOG_TAG, "Failed to insert uri=" + uri);
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ret;
    }

     @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
         Cursor cursor = null;
         SQLiteDatabase db = mDBHelper.getReadableDatabase();
         int matchCode = sUriMatcher.match(uri);
//         Log.v(LOG_TAG, "query: uri="+uri.toString()+", matchCode="+Integer.toString(matchCode));
         if (matchCode == PLANT_BY_SCIENTIFIC) {
             String scienticName = uri.getLastPathSegment();          //TODO: getLastSegment work?
             cursor = db.query(PlantContract.PlantEntry.TABLE_NAME, projection,
                     String.format("%s=?", PlantContract.PlantEntry.COLUMN_SCIENTIFIC),
                     new String[] {scienticName}, null, null, sortOrder);
         } else if (matchCode == PLANT_BY_ID) {
             String idStr = uri.getPathSegments().get(1);
             cursor = db.query(PlantContract.PlantEntry.TABLE_NAME, projection, PlantContract.PlantEntry._ID+"=?",
                     new String[] { idStr }, null, null, sortOrder);
         } else if (matchCode == PLANT) {
             cursor = db.query(PlantContract.PlantEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
         } else if  (matchCode == PLANT_ALIAS) {
             cursor = db.query(PlantContract.AliasEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
         } else if (matchCode == PLANT_FAVORITE) {
             cursor = db.query(PlantContract.PlantEntry.FAVORITE_VIEW, projection, selection, selectionArgs, null, null, sortOrder);
         } else if (matchCode == PLANT_ALIAS_BY_SCIENTIFIC) {
             cursor = sPlanCommonNameBuilder.query(db, projection, selection, selectionArgs,null,null, sortOrder) ;
         }
         if (cursor != null) {
             cursor.setNotificationUri(getContext().getContentResolver(), uri);
//             Log.v(LOG_TAG, "query: successed, #="+Integer.toString(cursor.getCount()));
         } else {
             Log.w(LOG_TAG, "insert: not supported Uri for insertion, uri=" + uri);
         }
         return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int matchCode = sUriMatcher.match(uri);
        int numrow = -1;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
//        Log.v(LOG_TAG, "update: uri="+uri.toString()+", matchCode="+Integer.toString(matchCode));
        if (matchCode == PLANT) {
            numrow = db.update(PlantContract.PlantEntry.TABLE_NAME, values, selection, selectionArgs);
        } else if (matchCode == PLANT_ALIAS) {
            numrow = db.update(PlantContract.AliasEntry.TABLE_NAME, values, selection, selectionArgs);
        } else {
            Log.w(LOG_TAG, "update: not supported Uri for updating, uri=" + uri);
        }
        if (numrow > 0) {         // with selection = "1", we can do this
            getContext().getContentResolver().notifyChange(uri, null);
//            Log.v(LOG_TAG, "update: #row=" + Integer.toString(numrow));
        }
        return numrow;           // no update needed
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int matchCode = sUriMatcher.match(uri);
        int numrow = -1;
        if (null == selection) {    // null will delete all rows
            selection = ForDelete;
        }
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        if (matchCode == PLANT) {
            numrow = db.delete(PlantContract.PlantEntry.TABLE_NAME, selection, selectionArgs);
        } else if (matchCode == PLANT_ALIAS) {
            numrow = db.delete(PlantContract.AliasEntry.TABLE_NAME, selection, selectionArgs);
        }
        if (numrow > 0)                // with selection = "1", we can do this
            getContext().getContentResolver().notifyChange(uri, null);
        return numrow;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLANT:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PlantContract.PlantEntry.TABLE_NAME, null, value);
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
            default:
                Log.i(LOG_TAG, "bulkInsert: unsupported Uri +" + uri.toString());
                return super.bulkInsert(uri, values);
        }
    }
}
