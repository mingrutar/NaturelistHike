package com.coderming.naturalisthike.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by linna on 9/3/2016.
 */
public class TestUtils {
    private static final String LOG_TAG = TestUtils.class.getSimpleName() ;

    public static void deleteAllRecordsFromProvider(Context context) {
        Uri[] tableUris = new Uri[]{TripContract.TripEntry.CONTENT_URI, TripContract.HikerEntry.CONTENT_URI,
                TripContract.RosterEntry.CONTENT_URI, TripContract.PlantTripEntry.CONTENT_URI,
                TripContract.CheckListEntry.CONTENT_URI};
        for (Uri uri : tableUris) {
            context.getContentResolver().delete(
                    uri,
                    null,
                    null
            );
        }
        for (Uri uri : tableUris) {
            Cursor cursor = context.getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor != null) {   //not all table can be queried
                if (cursor.getCount() > 0) {
                    Log.w(LOG_TAG, "Error: Records not deleted from %s table during delete");
//                assertEquals(String.format("Error: Records not deleted from %s table during delete", uri.getLastPathSegment()), 0, cursor.getCount());
                }
                cursor.close();
            }
        }
    }
    public static int countOKInsertion(long[] numRec) {
        int ret = 0;
        for (int i=0; i < numRec.length; i++) {
            if (numRec[i] != -1) {
                ret++;
            }
        }
        return ret;
    }

    /***
     *  Plant
     */
    public static long[] insertTestData(Context context, int numPlant, int numAlias) {
        String commonName, scientificName, imageUrl, common_alias, iStr, jStr;
        Uri uri = null;
        long[] ids = new long[numPlant];
        for (int i = 1; i <= numPlant; i++) {
            iStr = Integer.toString(i);
            commonName = "Common Name " + iStr;
            scientificName = "Scientific Name " + iStr;
            imageUrl = "http://wildflower/imagestore/12345/image_" + iStr;
            uri = PlantDataHelper.upsertPlant(context, "", scientificName, commonName, imageUrl);
            long id = Long.parseLong(uri.getLastPathSegment());
            Log.v(LOG_TAG, String.format("+++i=%d)uri=%s, id=%d", i, uri, id));
            ids[i-1] = id;
        }
        for (int i = 0; i < numPlant; i++) {
            iStr = Integer.toString(i+1);
            for (int j = 1; j <= numAlias; j++) {
                jStr = Integer.toString(j);
                common_alias = "Common" + iStr + "_alias" + jStr;
                PlantDataHelper.upsertPlantAlias(context, ids[i], common_alias);
            }
        }
        Log.v(LOG_TAG, String.format("+++insertTestData: ids=", Arrays.toString(ids)));
        return ids;
    }
    /****
     *  Trip
     */
    public static long[] insertTrips(Context context, int numPast, int numCurrent) {
        long[] tripDBId = new long[numPast + numCurrent];
        LatLng homeGeo = Utility.getHomeGeo(context);
        LatLng thGeo = new LatLng(46.9331, -121.8643);
        LatLng mpGeo = new LatLng(7.6762957, -122.3229763);
        ContentValues cv;
        Uri uri;
        Calendar now = Calendar.getInstance();
        long startTime = now.getTimeInMillis() - (90 * Constants.DAY_INMILLIS);     // 90 days back
        long delta = 7 * 24 * 60 * 60 * 1000;  // 1 week;
        for (int i = 0; i < numPast; i++) {
            String iStr = Integer.toString(i);
            cv = new ContentValues();
            cv.put(TripContract.TripEntry.COLUMN_NAME, "name_past" + iStr);
            cv.put(TripContract.TripEntry.COLUMN_SUBTITLE, "region_past" + iStr);
            cv.put(TripContract.TripEntry.COLUMN_DISTANCE, 6.0f + i);
            cv.put(TripContract.TripEntry.COLUMN_ELEVATION, 1300f + i);
            cv.put(TripContract.TripEntry.COLUMN_TH_LATITUDE, thGeo.latitude);
            cv.put(TripContract.TripEntry.COLUMN_TH_LONGITUDE, thGeo.longitude);
            cv.put(TripContract.TripEntry.COLUMN_TRIP_URL, "http://mytrip.com/trip_past_" + iStr);
            long date = startTime + i * delta;
            cv.put(TripContract.TripEntry.COLUMN_HIKE_DATE, date);
            cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE, "65th PnR");
//                long time = sTimeFormat.parse("6:30 am").getTime();
            long time = 99000000L;              // 7:30 am
            cv.put(TripContract.TripEntry.COLUMN_MEETING_TIME, time);
            // calculated values
            cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LAN, mpGeo.latitude);
            cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LON, mpGeo.longitude);
            uri = context.getContentResolver().insert(TripContract.TripEntry.CONTENT_URI, cv );
            tripDBId[i] = Long.parseLong(uri.getLastPathSegment());
        }
       startTime = now.getTimeInMillis() + 2 * Constants.DAY_INMILLIS;     //
        for (int i = 0; i < numCurrent; i++) {
            try {
                String iStr = Integer.toString(i);
                cv = new ContentValues();
                cv.put(TripContract.TripEntry.COLUMN_NAME, "name_current"+iStr);
                cv.put(TripContract.TripEntry.COLUMN_SUBTITLE, "region_current"+iStr);
                cv.put(TripContract.TripEntry.COLUMN_DISTANCE, 6.0f + i);
                cv.put(TripContract.TripEntry.COLUMN_ELEVATION, 1300f+i);
                cv.put(TripContract.TripEntry.COLUMN_TH_LATITUDE, thGeo.latitude);
                cv.put(TripContract.TripEntry.COLUMN_TH_LONGITUDE, thGeo.longitude);
                cv.put(TripContract.TripEntry.COLUMN_TRIP_URL, "http://mytrip.com/trip_"+iStr);
                long date = startTime + i * delta;
//            long date = sDateFormat.parse(String.format("September %d, 2016", (1+10*i)%30) ).getTime();
                cv.put(TripContract.TripEntry.COLUMN_HIKE_DATE, date);
                cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE, "65th PnR");
                long time = Utility.sTimeFormat.parse("6:30 AM").getTime();
                cv.put(TripContract.TripEntry.COLUMN_MEETING_TIME, time);
                cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LAN, mpGeo.latitude);
                cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LON, mpGeo.longitude);

                uri = context.getContentResolver().insert(TripContract.TripEntry.CONTENT_URI, cv );
                tripDBId[numPast+i] = Long.parseLong(uri.getLastPathSegment());
            } catch (ParseException pex) {
                Log.w(LOG_TAG, "Exception in creating trip " + Integer.toString(i) + ": " + pex, pex);
            }
        }
        return tripDBId;
    }

}
