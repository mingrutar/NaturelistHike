package com.coderming.naturalisthike.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by linna on 9/6/2016.
 */
public class TestTripPlant extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtils.deleteAllRecordsFromProvider(mContext);
    }

        // Leadet trip species list
    public static long[] insertTripSpecies(Context context, long[] plantDBIds, long tripId, int numPlant, int numOther) {
        long[] ret = new long[numPlant + numOther];

//        int idx2 = plantDBIds.length - 1;
        //       long plantId = plantDBIds[idx2];

        for (int i = 0; i < numPlant; i++) {
            String iStr = Integer.toString(i + 1);
            String specisName = "Scientific Name " + iStr;
            TripContract.SpeciesType type = TripContract.SpeciesType.Plant;
            Uri uri = TripPlantDataHelper.insertLeaderSpeciesList(context, tripId, specisName, type);
            ret[i] = Long.parseLong(uri.getLastPathSegment());
            assertNotNull(uri);
        }
        for (int i = 0; i < numOther; i++) {
            String iStr = Integer.toString(i + 1);
            String specisName = "Other specis Name " + iStr;
            TripContract.SpeciesType type = TripContract.SpeciesType.Other;
            Uri uri = TripPlantDataHelper.insertLeaderSpeciesList(context, tripId, specisName, type);
            ret[i+numPlant] = Long.parseLong(uri.getLastPathSegment());
            assertNotNull(uri);
        }
        return ret;
    }

    public void testInsertLeaderPlantList() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 0, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        int numPlant = 3;
        int numOther = 0;
        long[] plantDBIds = TestUtils.insertTestData(mContext, numPlant, 0);
        long[] dbIds = insertTripSpecies(mContext, plantDBIds, tripId, numPlant, numOther);   // #plant, #others

        Cursor cursor = TripPlantDataHelper.getLeaderSpeciesList(mContext, tripId);
        assertEquals(numPlant+numOther, cursor.getCount());
    }

    public void testInsertLeaderSpeciesList() {     //plant + others
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 2;
        long tripId = tripDBIds[idx];

        int numPlant = 3;
        int numOther = 2;
        long[] plantDBIds = TestUtils.insertTestData(mContext, numPlant, 1);
        long[] dbIds = insertTripSpecies(mContext, plantDBIds, tripId, numPlant, numOther);   // #plant, #others
        Cursor cursor = TripPlantDataHelper.getLeaderSpeciesList(getContext(), tripId);
        assertEquals(numPlant+numOther, cursor.getCount());
    }

    public void testAddObservedPlant() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 2;
        long tripId = tripDBIds[idx];

        long[] plantDBIds = TestUtils.insertTestData(mContext, 4, 1);
        long[] dbIds = insertTripSpecies(mContext, plantDBIds, tripId, 3, 2);   // #plant, #others

        long plantId = plantDBIds[2];
        long dbId = TripPlantDataHelper.findIdPlantOnTripPlantList(getContext(), tripId, plantId);
        assertEquals("findIdPlantOnTripPlantList failed", dbIds[2], dbId);

        Uri uri = TripPlantDataHelper.upsertObservedPlant(mContext, tripId, plantId);
        assertNotNull("upsertObservedPlant", uri);

        Cursor cursor = TripPlantDataHelper.getObservedPlantList(mContext);
        assertEquals("getObservedPlantList", 1, cursor.getCount());
    }

    public void testUpsertPlantPhotowoPlantId() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        long[] plantDBIds = TestUtils.insertTestData(mContext, 4, 1);
//        long[] dbIds = insertTripSpecies(mContext, plantDBIds, tripId, 3, 2);   // #plant, #others

        long plantId = -1;

        String photoUri = "/path/to/photo";
        String voiceUri = "/path/to/voice";
        Uri uri = TripPlantDataHelper.upsertPlantPhoto(mContext, tripId, plantId, photoUri, voiceUri);
        assertNotNull("upsertPlantPhoto", uri);
        long dbId = Long.parseLong(uri.getLastPathSegment());
        assertTrue("parse Uri", (dbId != -1) );

        Cursor cursor = TripPlantDataHelper.getAllTripPlantList(mContext, tripId);
        assertEquals("getObservedPlantList", 1, cursor.getCount());
    }

    public void testremoveObservedFlag() {
        long[] tripDBIds = TestUtils.insertTrips(mContext, 1, 3);
        int idx = 1;
        long tripId = tripDBIds[idx];

        long[] plantDBIds = TestUtils.insertTestData(mContext, 4, 1);
        long[] dbIds = insertTripSpecies(mContext, plantDBIds, tripId, 3, 2);   // #plant, #others

        long plantId = plantDBIds[2];
        Uri uri = TripPlantDataHelper.upsertObservedPlant(mContext, tripId, plantId);
        assertNotNull("upsertObservedPlant", uri);

        long dbId = Long.parseLong(uri.getLastPathSegment());
        uri = TripPlantDataHelper.removeObservedFlag(mContext, dbId);
        assertNotNull("upsertObservedPlant", uri);
        Cursor cursor = TripPlantDataHelper.getObservedPlantList(mContext);
        assertFalse("getObservedPlantList empty cursor", cursor.moveToFirst());
    }


}
