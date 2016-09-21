package com.coderming.naturalisthike.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by linna on 9/3/2016.
 */
public class TestTripProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestTripProvider.class.getSimpleName();

    HashMap<Uri, String> mUris;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mUris = new HashMap<>();

        mUris.put(TripContract.TripEntry.CONTENT_URI, TripContract.TripEntry.CONTENT_TYPE);
        mUris.put(TripContract.HikerEntry.CONTENT_URI, TripContract.HikerEntry.CONTENT_TYPE);
        mUris.put(TripContract.RosterEntry.CONTENT_URI, TripContract.RosterEntry.CONTENT_TYPE);
        mUris.put(TripContract.PlantTripEntry.CONTENT_URI, TripContract.PlantTripEntry.CONTENT_TYPE);
        mUris.put(TripContract.CheckListEntry.CONTENT_URI, TripContract.CheckListEntry.CONTENT_TYPE);
        mUris.put(TripContract.CheckListEntry.CONTENT_CLUB_URI, TripContract.CheckListEntry.CONTENT_TYPE);
        mUris.put(TripContract.CheckListEntry.CONTENT_PERSONAL_URI, TripContract.CheckListEntry.CONTENT_TYPE);

        long dbId = 22;
        Uri uri = TripContract.TripEntry.buildUri(dbId);    // trip by Id
        mUris.put(uri, TripContract.TripEntry.CONTENT_ITEM_TYPE);

        dbId = 3;
        uri = TripContract.HikerEntry.buildUri(dbId);    //
        mUris.put(uri, TripContract.HikerEntry.CONTENT_ITEM_TYPE);
        String email = "hiker.two@gmail.com";

        dbId = 9;
        uri = TripContract.RosterEntry.buildUri(dbId);    //
        mUris.put(uri, TripContract.RosterEntry.CONTENT_TYPE);
        long hikeId = 2;

       // PlantTrip
        long tripId=302;

        dbId = 2;
        uri = TripContract.PlantTripEntry.buildUri(dbId);    // by dbId
        mUris.put(uri, TripContract.PlantTripEntry.CONTENT_ITEM_TYPE);
        // CheckList
        dbId = 2;
        uri = TripContract.CheckListEntry.buildUri(dbId);    // by dbId
        mUris.put(uri, TripContract.CheckListEntry.CONTENT_ITEM_TYPE);
        tripId = 32;                                     // leader's
        uri = TripContract.CheckListEntry.buildLeaderCLByTripUri(tripId);
        Log.v(LOG_TAG, "+++ buildLeaderCLByTripUri(2)="+uri.toString());
        mUris.put(uri, TripContract.CheckListEntry.CONTENT_TYPE);
                                                            // mine single item
        uri = TripContract.CheckListEntry.buildPersonalCLByIdUri(hikeId);
        mUris.put(uri, TripContract.CheckListEntry.CONTENT_ITEM_TYPE);

        TestUtils.deleteAllRecordsFromProvider(mContext);
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();
        String providerName = TripContentProvider.class.getName();
        ComponentName componentName = new ComponentName(mContext.getPackageName(), providerName);
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + TripContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TripContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue(String.format("Error: %s not registered at %s", providerName, mContext.getPackageName()), false);
        }
    }

    public void testGetType() {
        for (Uri uri : mUris.keySet()) {
            // content://com.example.android.sunshine.app/weather/
            String type = mContext.getContentResolver().getType(uri);
            Log.v(LOG_TAG, "--testGetType: uri="+uri.toString()+",type="+type);
            // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
            assertEquals(String.format("Error: uri=%s", uri),
                    mUris.get(uri), type);
        }
    }

    //
    public void testGetTripList() {
        int numPastTrips = 2;
        int numCurrentTrips = 3;
        long[] total = TestUtils.insertTrips(mContext, numPastTrips, numCurrentTrips);
        Cursor cursor = TripDataHelper.getTripList(mContext);
        assertEquals("testGetTripList: ", numCurrentTrips, cursor.getCount());
    }

    public void testGetAllTrips() {
        int numPastTrips = 2;
        int numCurrentTrips = 3;
        long[] total = TestUtils.insertTrips(mContext, numPastTrips, numCurrentTrips);
        assertEquals("testGetAllTrips: after insertTrips", numPastTrips+numCurrentTrips, total.length);
        Cursor cursor = TripDataHelper.getAllTrips(mContext);
        int expected = numPastTrips + numCurrentTrips;
        assertEquals("testGetAllTrips: wrong #rec", expected, cursor.getCount());
    }
    long genAtripWithId(int numCurrentTrips) {
        int numPastTrips = 0;
        long[] tripDBIds = TestUtils.insertTrips(mContext, numPastTrips, numCurrentTrips);
        assertEquals("testGetAllTrips: after insertTrips", numPastTrips+numCurrentTrips, tripDBIds.length);
        int idx = 1;
        long tripId = tripDBIds[idx];
        assertTrue(tripId != -1);
        return tripId;
    }
    public void testHome2MPDrive() {
        long tripId = genAtripWithId( 3 );
        long duration = 15 * Constants.SECOND_INMILLIS;
        int ret = TripDataHelper.updateHomeMpDrivingTime(getContext(), tripId, duration);
        assertEquals(1, ret);
        Cursor cursor = TripDataHelper.getATrip(getContext(), tripId );
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        int col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_HOME_MP_DRIVINGTIME);
        int fetched = cursor.getInt(col);
        assertEquals(duration, fetched);
    }
    public void testAddMyMeetingPlace() {
        long tripId = genAtripWithId( 3 );
        String name ="My meeting place";
        long meettime = Calendar.getInstance().getTimeInMillis();
        LatLng geo = Utility.getHomeGeo(getContext());
        long home2mp = 20 * Constants.MINUTE_INMILLIS;
        long mp2th = 1 * Constants.HOUR_INMILLIS;
        int ret = TripDataHelper.updateMyMeetingPlace(getContext(), tripId, name, meettime, geo, home2mp, mp2th);
        assertEquals(1, ret);
        Cursor cursor = TripDataHelper.getATrip(getContext(), tripId );
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        int col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_MY_MEETING_PLACE);
        String fetchedName = cursor.getString(col);
        assertEquals(name, fetchedName);
        col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_MY_MP_LATITUDE);
        double fetchedVal = cursor.getDouble(col);
        assertEquals(fetchedVal, geo.latitude);
        col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_MY_MP_LONGITUDE);
        fetchedVal = cursor.getDouble(col);
        assertEquals(fetchedVal, geo.longitude);

        col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_HOME_MP_DRIVINGTIME);
        long fetchedLong = cursor.getLong(col);
        assertEquals(fetchedLong, home2mp);

        col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_MP_TH_DRIVINGTIME);
        long fetchedMpTh = cursor.getLong(col);
        assertEquals(mp2th, fetchedMpTh );
    }
    public void testMP2THDrive() {
        long tripId = genAtripWithId( 3 );
        long duration = Constants.HOUR_INMILLIS + 23*Constants.MINUTE_INMILLIS;
        long meettime = Calendar.getInstance().getTimeInMillis();
        int ret = TripDataHelper.updateTHTimeandDriving(getContext(), tripId, meettime, duration);
        assertEquals(1, ret);
        Cursor cursor = TripDataHelper.getATrip(getContext(), tripId );
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        int col = cursor.getColumnIndex(TripContract.TripEntry.COLUMN_MP_TH_DRIVINGTIME);
        int fetched = cursor.getInt(col);
        assertEquals(duration, fetched);
    }
    // Hiker /Roster
    public long[] insertLeader(Context context, long tripId, int num) {
        long[] ret = new long[num];
        String name, email;
        for (int i = 0; i < num; i++) {
            String iStr = Integer.toString(i + 1);
            name = "Leader num" + iStr;
            email = "leader.hiker" + iStr + "@test.com";
            TripContract.HikerType hikerType = TripContract.HikerType.Leader;
            Uri uri = TripDataHelper.upsertHiker(mContext, tripId, name, email, hikerType);
            assertNotNull("insertLeader upsertHiker not expect uri = null", uri);
            ret[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return ret;
    }

    public long[] insertCoLeader(Context context, long tripId, int num) {
        long[] ret = new long[num];
        String name, email;
        for (int i = 0; i < num; i++) {
            String iStr = Integer.toString(i + 1);
            name = "CoLeader Num" + iStr;
            email = "coleader.hiker" + iStr + "@test.com";
            TripContract.HikerType hikerType = TripContract.HikerType.CoLeader;
            Uri uri = TripDataHelper.upsertHiker(mContext, tripId, name, email, hikerType);
            assertNotNull("insertCoLeader upsertHiker not expect uri = null", uri);
            ret[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return ret;
    }

    public long[] insertParticipant(Context context, long tripId, int num) {
        long[] ret = new long[num];
        String name, email;
        for (int i = 0; i < num; i++) {
            String iStr = Integer.toString(i + 1);
            name = "Participant Num" + iStr;
            email = "hiker.me" + iStr + "@test.com";
            TripContract.HikerType hikerType = TripContract.HikerType.Participant;
            Uri uri = TripDataHelper.upsertHiker(mContext, tripId, name, email, hikerType);
            assertNotNull("insertParticipant upsertHiker not expect uri = null", uri);
            ret[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return ret;
    }

    public void testInsertCoLeader() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 0, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        long[] hikerDBIds = insertCoLeader(mContext, tripId, 1);
        String email1 = "coleader.hiker1@test.com";
        Cursor cursor = TripDataHelper.findHikerByEmail(mContext, email1);
        assertNotNull("testInsertLeader getTripLeaders not expect cursor = null", cursor);
        assertTrue("testInsertLeader getTripLeaders rec#=1", cursor.moveToFirst());
         String email2 = cursor.getString(cursor.getColumnIndex(TripContract.HikerEntry.COLUMN_EMAIL));
        assertEquals(String.format("testInsertCoLeader findHikerByEmail expected %s, but got %s", email1, email2),
                email1, email2);
    }

// //Hikers are immutable
//    public void testUpdateHiker() {         //test finHikerByEmail and insert
//        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
//        int idx = 1;
//        long tripId = tripDBIds[idx];
//
//        long[] hikerDBIds = insertParticipant(mContext, tripId, 4);
//        String email = "hiker.me2@test.com";
//        TripContract.HikerType hikerType = TripContract.HikerType.CoLeader;
//        Uri uri = TripDataHelper.upsertHiker(mContext, tripId, "what ever", email, hikerType);
//        assertNotNull("testInsertLeader upsertHiker not expect uri = null", uri);
//        long dbId = Long.parseLong(uri.getLastPathSegment());
//        assertEquals(String.format("testUpdateHiker expect %d, but got %d", hikerDBIds[1], dbId),
//                hikerDBIds[1], dbId);
//
//        Cursor cursor = TripDataHelper.getTripRoster(mContext, tripId);
//        assertTrue("testUpdateHiker:getTripRoster cursor has no rec", cursor.moveToFirst());
//        assertEquals("getTripRoster: ", hikerDBIds.length, cursor.getCount());
//    }

    public void testInsertLeader() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        long[] hikerDBIds = insertLeader(mContext, tripId, 2);

        Cursor cursor = TripDataHelper.getTripLeaders(mContext, tripId);
        assertTrue("testInsertLeader:getTripLeaders cursor has no rec", cursor.moveToFirst());
        TripContract.HikerType expected = TripContract.HikerType.Leader;
        int hikerTypeVal = cursor.getInt(cursor.getColumnIndex(TripContract.RosterEntry.COLUMN_TYPE));
        assertEquals(String.format("testInsertLeader getTripLeaders expected type %d[%s], but got %d",
                expected.getValue(), expected.toString(), hikerTypeVal),
                expected.getValue(), hikerTypeVal);
    }
    public void testGetTripRoster() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        assertEquals("testGetTripRoster: insertTrips:", 4, TestUtils.countOKInsertion(tripDBIds));
        int idx = 1;
        long tripId = tripDBIds[idx];

//        long[] leaderDBIds = insertLeader(mContext, tripId, 1);
//        assertEquals("testGetTripRoster: insertLeader: tripId="+Long.toString(tripId), 1, TestUtils.countOKInsertion(leaderDBIds));
//        long[] coleaderDBIds = insertCoLeader(mContext, tripId, 2);
//        assertEquals("testGetTripRoster: insertCoLeader: tripId="+Long.toString(tripId), 2, TestUtils.countOKInsertion(coleaderDBIds));
        long[] hikerDBIds = insertParticipant(mContext, tripId, 1);
        assertEquals("testGetTripRoster: insertParticipant: tripId="+Long.toString(tripId), 1, TestUtils.countOKInsertion(hikerDBIds));

        int expected = 1 ;  //+ 2 + 1;

        Cursor cursor = TripDataHelper.getTripRoster(mContext, tripId);
        assertEquals(String.format("testGetTripRoster: expect %d hiker, but got %d ", expected
                , cursor.getCount()), expected, cursor.getCount());
    }
}