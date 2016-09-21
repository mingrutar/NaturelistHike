package com.coderming.naturalisthike.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by linna on 9/3/2016.
 */
public class PlantDataHelper implements DataConstants {
    private static final String LOG_TAG = PlantDataHelper.class.getSimpleName();
    public static final String[] sId = new String[] {BaseColumns._ID};


    /***
     * Insertions
     */
    public static Uri upsertPlant(Context context, String family, String scientific, String common, String imageUrl) {
        if ((context == null) || (family == null) || (imageUrl == null) ||
                (scientific == null) || (common == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        long plantId = findPlant(context, scientific);
        if (plantId == -1 ) {
            ContentValues values = new ContentValues();
            values.put(PlantContract.PlantEntry.COLUMN_FAMILY, family.toLowerCase());
            values.put(PlantContract.PlantEntry.COLUMN_SCIENTIFIC, scientific.toLowerCase());
            values.put(PlantContract.PlantEntry.COLUMN_COMMON, common.toLowerCase());
            values.put(PlantContract.PlantEntry.COLUMN_IMAGE_URL, imageUrl.toLowerCase());
            return context.getContentResolver().insert(PlantContract.PlantEntry.CONTENT_URI, values);
        } else {
            upsertPlantAlias(context, plantId, common);
            return PlantContract.PlantEntry.buildUri(plantId);
        }
    }
    public static Uri upsertPlantAlias(Context context, long plantId, String alias) {
        if ((context == null) || (alias == null)) {
            Log.w(LOG_TAG, "insertPlant all params must be non-null");
            return null;
        }
        long aliasId = findAliasIdByName(context, alias);
        if (aliasId == -1) {
            ContentValues values = new ContentValues();
            values.put(PlantContract.AliasEntry.COLUMN_PLANT_ID, plantId);
            values.put(PlantContract.AliasEntry.COLUMN_NAME, alias.toLowerCase());
            return context.getContentResolver().insert(PlantContract.AliasEntry.CONTENT_URI, values);
        } else {
            return PlantContract.AliasEntry.buildUri(aliasId);
        }
    }

    /***
     *  Search
     */

    public static long findPlant(Context context, String scientficName) {
        long plantId = -1;
        scientficName = scientficName.toLowerCase();
        Uri uri = PlantContract.PlantEntry.buildSearchUri(scientficName);
        Cursor cursor = context.getContentResolver().query(uri, sId, TripDBHelper.sScientificStmt,
                new String[] {String.format(QueryQs, scientficName)}, null, null);
        if ( (cursor != null) && (cursor.moveToFirst()) ) {
            plantId = cursor.getLong(0);
        }
        return plantId;
    }
    // all plants
    public static Cursor getAllPlants(Context context) {
        return context.getContentResolver().query(PlantContract.PlantEntry.CONTENT_URI,
                null, null, null, null);
    }
    // all favorites
    public static Cursor getAllFavoritePlants(Context context) {
        return context.getContentResolver().query(PlantContract.PlantEntry.CONTENT_FAVORITE_URI,
                null, null, null, null);
    }
    public static void addToFavorite(Context context, String scientific) {
//        Log.v(LOG_TAG, "++addToFavorite: scientific = " + scientific);
        scientific = scientific.toLowerCase();
        ContentValues cv = new ContentValues();
        cv.put(PlantContract.PlantEntry.COLUMN_IS_FAVOR, 1);
        context.getContentResolver().update(PlantContract.PlantEntry.CONTENT_URI, cv,
                String.format(QuerySqS,PlantContract.PlantEntry.COLUMN_SCIENTIFIC, scientific), null );
    }
    public static void removeFromFavorite(Context context, String scientific) {
//        Log.v(LOG_TAG, "++removeFavorite: scientific = " + scientific);
        scientific = scientific.toLowerCase();
        ContentValues cv = new ContentValues();
        cv.put(PlantContract.PlantEntry.COLUMN_IS_FAVOR, 0);
        context.getContentResolver().update(PlantContract.PlantEntry.CONTENT_URI, cv,
                String.format(QuerySqS,PlantContract.PlantEntry.COLUMN_SCIENTIFIC, scientific), null );
    }
    /***
     *  Alias (common name) or Common name
     */
    // plant's common names
    public static Cursor getPlantAliasByName(Context context, String scientific) {
        scientific =  scientific.toLowerCase();
        return context.getContentResolver().query(PlantContract.AliasEntry.CONTENT_PLANT_URI, null, TripDBHelper.sScientificStmt,
                new String[] {scientific}, null, null);
    }

    public static long findAliasIdByName(Context context, String commonName) {
        commonName = commonName.toLowerCase();
        Cursor cursor = context.getContentResolver().query(PlantContract.AliasEntry.CONTENT_URI, sId,
                PlantContract.AliasEntry.COLUMN_NAME + QueryPS, new String[] { commonName}, null, null);
        if ((cursor != null) && (cursor.moveToFirst()) )
            return cursor.getLong(0);
        else
            return -1;
    }
}
