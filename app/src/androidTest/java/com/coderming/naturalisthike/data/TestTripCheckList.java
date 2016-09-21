package com.coderming.naturalisthike.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by linna on 9/6/2016.
 */
public class TestTripCheckList extends AndroidTestCase {
    public static final String LOG_TAG = TestTripCheckList.class.getSimpleName();

    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.deleteAllRecordsFromProvider(mContext);

    }
        // checklist
    public long[] insertCLClub(Context context, int numItem) {
        long[] DBIds = new long[numItem];
        String item;
        Uri uri = null;
        for (int i = 0; i < numItem; i++) {
            item = "Club Item " + Integer.toString(i);
            uri =   TripCheckListDataHelper.upsertClubCheckList(mContext, item);
            assertNotNull(uri);
            DBIds[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return DBIds;
    }

    public long[] insertCLPersonal(Context context, int numItem) {
        long[] DBIds = new long[numItem];
        String item;
        Uri uri = null;
        boolean isOptional = true;
        for (int i = 0; i < numItem; i++) {
            item = "My Item " + Integer.toString(i);
            uri =   TripCheckListDataHelper.upsertMyCheckList(mContext, item, isOptional);
            assertNotNull(uri);
            isOptional = !isOptional;
            DBIds[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return DBIds;
    }

    public long[] insertCLLeader(Context context, int numItem, long tripId) {
        long[] DBIds = new long[numItem];
        String item;
        Uri uri = null;
        boolean isOptional = false;
        for (int i = 0; i < numItem; i++) {
            item = "Leader Item " + Integer.toString(i);
            uri =   TripCheckListDataHelper.upsertLeaderCheckList(mContext, tripId, item, isOptional);
            assertNotNull(uri);
            isOptional = !isOptional;
            DBIds[i] = Long.parseLong(uri.getLastPathSegment());
        }
        return DBIds;
    }

    public void testGetAllChecklistItem() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        int numClubCL = 3;
        long[] CLClubDBIds = insertCLClub(mContext, numClubCL);
        assertEquals("insertCLCLub: ", numClubCL,  TestUtils.countOKInsertion(CLClubDBIds));
        int numMyCL = 4;
        long[] CLPersonalDBIds = insertCLPersonal(mContext, numMyCL);
        assertEquals("insertCLPersonal: ", numMyCL,  TestUtils.countOKInsertion(CLPersonalDBIds));
        int numLeaderCL = 2;
        long[] CLLeaderDBIds = insertCLLeader(mContext, numLeaderCL, tripId);
        assertEquals("insertCLLeader: ", numLeaderCL,  TestUtils.countOKInsertion(CLLeaderDBIds));

        Cursor curesor =   TripCheckListDataHelper.getAllChecklistItem(mContext, tripId);
        assertEquals(numClubCL + numMyCL + numLeaderCL, curesor.getCount());
    }
    public void testGetClubChecklistItem() {
        int numClubCL = 3;
        long[] CLClubDBIds = insertCLClub(mContext, numClubCL);
        assertEquals("insertCLLeader: ", numClubCL,  TestUtils.countOKInsertion(CLClubDBIds));
        Cursor curesor =   TripCheckListDataHelper.getClubChecklistItem(mContext);
        assertEquals(numClubCL, curesor.getCount());
    }
    public void testGetLeaderChecklistItem() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        int numLeaderCL = 4;
        long[] CLLeaderDBIds = insertCLLeader(mContext, numLeaderCL, tripId);
        assertEquals("insertCLLeader: ", numLeaderCL,  TestUtils.countOKInsertion(CLLeaderDBIds));
        Cursor curesor =   TripCheckListDataHelper.getLeaderChecklistItem(mContext, tripId);
        assertEquals(numLeaderCL, curesor.getCount());
    }
    public void testGetMyChecklistItem() {
        int numMyCL = 5;
        long[] CLMyDBIds = insertCLPersonal(mContext, numMyCL);
        assertEquals("insertCLPersonal: ", numMyCL,  TestUtils.countOKInsertion(CLMyDBIds));
        Cursor curesor =   TripCheckListDataHelper.getMyChecklistItem(mContext);
        assertEquals(numMyCL, curesor.getCount());
    }

    public void testCLCheckItem() {
        long[] CLPersonalDBIds = insertCLPersonal(mContext, 4);
        int idx = 1;
        long DBIs = CLPersonalDBIds[idx];

        boolean ret =   TripCheckListDataHelper.setCLItemCheck(mContext, DBIs, true);
        assertTrue("testCLSetCheck: expected true, but got false", ret);

        ret =   TripCheckListDataHelper.setCLItemCheck(mContext, DBIs, false);
        assertTrue("testCLSetCheck: expected true, but got false", ret);
    }
    public void testCLUncheckItem() {
        long[] CLPersonalDBIds = insertCLPersonal(mContext, 1);
        int idx = 0;
        long DBIs = CLPersonalDBIds[idx];

        boolean ret =   TripCheckListDataHelper.setCLItemCheck(mContext, DBIs, true);
        assertTrue("testCLSetCheck: expected true, but got false", ret);

        ret =   TripCheckListDataHelper.setCLItemCheck(mContext, DBIs, false);
        assertTrue("testCLSetCheck, uncheck not checked item", ret);
    }

    public void testCLRemember() {
        long[] CLPersonalDBIds = insertCLPersonal(mContext, 4);
        int idx = 1;
        long DBIs = CLPersonalDBIds[idx];

        boolean ret =   TripCheckListDataHelper.setCLItemCheckAndRemember(mContext, DBIs, false);
        assertTrue("testCLSetCheck: expected true, but got false", ret);
    }

    public void testCLResetRemember() {
        long[] CLPersonalDBIds = insertCLPersonal(mContext, 4);
        int idx = 1;
        long DBIs = CLPersonalDBIds[idx];

        boolean ret =   TripCheckListDataHelper.setCLItemCheckAndRemember(mContext, DBIs, false);
        assertTrue("testCLSetCheck: expected true, but got false", ret);

        ret =   TripCheckListDataHelper.setCLItemReset(mContext, DBIs);
        assertTrue("testCLSetCheck: expected true, but got false", ret);
    }
}
