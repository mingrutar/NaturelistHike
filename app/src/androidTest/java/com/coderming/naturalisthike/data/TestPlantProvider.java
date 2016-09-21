package com.coderming.naturalisthike.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by linna on 9/2/2016.
 */
public class TestPlantProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestPlantProvider.class.getSimpleName();

    static final int PLANT_COUNT = 3;
    static final int ALIAS_COUNT = 2;
    HashMap<Uri, String> mUris;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mUris = new HashMap<>();
        mUris.put(PlantContract.PlantEntry.CONTENT_URI, PlantContract.PlantEntry.CONTENT_TYPE);
        mUris.put(PlantContract.PlantEntry.CONTENT_FAVORITE_URI, PlantContract.PlantEntry.CONTENT_TYPE);
        // alias
        mUris.put(PlantContract.AliasEntry.CONTENT_URI, PlantContract.AliasEntry.CONTENT_TYPE);
        mUris.put(PlantContract.AliasEntry.CONTENT_PLANT_URI, PlantContract.AliasEntry.CONTENT_TYPE);

        // items plant
        Uri uri = PlantContract.PlantEntry.buildUri(212);
        mUris.put(uri, PlantContract.PlantEntry.CONTENT_ITEM_TYPE);
        uri = PlantContract.PlantEntry.buildSearchUri("scientific namr");
        mUris.put(uri, PlantContract.PlantEntry.CONTENT_ITEM_TYPE);
// TODO: not implemented in provider yet
//        uri = PlantContract.AliasEntry.buildUri(1);
//        mUris.put(uri, PlantContract.AliasEntry.CONTENT_ITEM_TYPE);

        deleteAllRecordsFromProvider();
    }

    public void deleteAllRecordsFromProvider() {
        Uri[] tableUris = new Uri[] {PlantContract.PlantEntry.CONTENT_URI, PlantContract.AliasEntry.CONTENT_URI};
        for (Uri uri : tableUris) {
            mContext.getContentResolver().delete(
                    uri,
                    null,
                    null
            );
        }
        for (Uri uri : tableUris) {
            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null
            );
            assertNotNull("Error deleteAllRecordsFromProvider: uri = null", uri);
            assertEquals(String.format("Error: Records not deleted from %s table during delete", uri.getLastPathSegment()), 0, cursor.getCount());
            cursor.close();
        }
    }
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();
        String provider = PlantContentProvider.class.getName();
        ComponentName componentName = new ComponentName(mContext.getPackageName(), provider);
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + PlantContract.CONTENT_AUTHORITY,
                    providerInfo.authority, PlantContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue(String.format("Error: %s not registered at %s", provider, mContext.getPackageName()), false);
        }
    }
    public void testGetType() {
        for (Uri uri : mUris.keySet() ) {
            // content://com.example.android.sunshine.app/weather/
            String type = mContext.getContentResolver().getType(uri);
            // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
            assertEquals(String.format("Error: testGetType expected %s, but get %s", mUris.get(uri), type ),
                    mUris.get(uri), type);
        }

        String str = "Impatiens capensis";
        Uri uri = PlantContract.PlantEntry.buildSearchUri(str);
        String type = mContext.getContentResolver().getType(uri);
        assertEquals("Error: the Plant CONTENT_URI with scientic name should return Plant.CONTENT_ITEM_TYPE",
                PlantContract.PlantEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testAllPlants() {
        TestUtils.insertTestData(mContext, PLANT_COUNT, ALIAS_COUNT);
        Cursor cursor = mContext.getContentResolver().query(PlantContract.PlantEntry.CONTENT_URI,
                null, null, null, null);
        int result = cursor.getCount();
        assertEquals(String.format("Error: experct %d rec, but got %d", PLANT_COUNT, result), PLANT_COUNT, result);
        assertTrue("Error: failed to move to first item", cursor.moveToFirst());
        assertEquals("Error: wrong scrintific name", "Scientific Name 1".toLowerCase(),
                cursor.getString(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_SCIENTIFIC)));
        cursor.close();
    }

    public void testFindPlantByDBId() {
        long[] dbids = TestUtils.insertTestData(mContext, PLANT_COUNT, ALIAS_COUNT);
        int idx = dbids.length > 1 ? 1 : 0;
        long dbid = dbids[idx];
        // plant by id
        Uri uri = PlantContract.PlantEntry.buildUri(dbid);
        Log.v(LOG_TAG, "+++testFindPlantByDBId: uri="+uri.toString());
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        int result = cursor.getCount();
        assertEquals(String.format("Error: expected 1 plant, but got %d", result), 1, result);
        assertTrue("Error: failed to move to first item", cursor.moveToFirst());
        String expected = "http://wildflower/imagestore/12345/image_"+ Integer.toString(idx+1).toLowerCase();
        assertEquals("Error: wrong imageUrl", expected,
                cursor.getString(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_IMAGE_URL)));

        // via PlantDataHelper by scientific name
        String scientificName = "Scientific Name " + Integer.toString(idx+1);
        long id = PlantDataHelper.findPlant(mContext, scientificName.toLowerCase());
        assertEquals(String.format("Error:PlantDataHelper.findPlant: expected plant Id=%d, got=%d", id, cursor.getLong(0)),
                id, cursor.getLong(0));
        cursor.close();
    }
    public void testFavoritePlant() {
        long[] dbids = TestUtils.insertTestData(mContext, PLANT_COUNT, ALIAS_COUNT);
        int idx = dbids.length > 1 ? 1 : 0;
        Uri uri = PlantContract.PlantEntry.buildUri(idx);
        Cursor cursor = getContext().getContentResolver().query(PlantContract.PlantEntry.CONTENT_URI,
                new String[] {PlantContract.PlantEntry.COLUMN_SCIENTIFIC}, null, null, null);
        assertEquals(PLANT_COUNT, cursor.getCount());
        cursor.moveToFirst();
        int colIdx = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_SCIENTIFIC);
        String scientific = cursor.getString( colIdx );
        PlantDataHelper.addToFavorite(mContext, scientific);      // add plant 1 as favorite

        cursor = PlantDataHelper.getAllFavoritePlants(mContext);
        int result = cursor.getCount();
        assertEquals(String.format("Error: getAllFavoritePlants expected 1 plant, but got %d", result), 1, result);
    }
    public void testAlias() {
        TestUtils.insertTestData(mContext, PLANT_COUNT, ALIAS_COUNT);
        // query all alias
        Cursor cursor = mContext.getContentResolver().query(PlantContract.AliasEntry.CONTENT_URI, null, null, null, null);
        int result = cursor.getCount();
        assertEquals(String.format("Error: expected %d plants, but got %d", PLANT_COUNT * ALIAS_COUNT, result), ALIAS_COUNT * PLANT_COUNT, result);
        assertTrue("Error: failed to move to first item", cursor.moveToFirst());
        assertEquals("Error: wrong common name", "Common1_alias1".toLowerCase(),
                cursor.getString(cursor.getColumnIndex(PlantContract.AliasEntry.COLUMN_NAME)));
    }
    public void testPlantAlias() {
        TestUtils.insertTestData(mContext, PLANT_COUNT, ALIAS_COUNT);
        // via PlantDataHelper
        Cursor cursor = PlantDataHelper.getPlantAliasByName(mContext,"Scientific Name 1" );
        int result = cursor.getCount();
        assertEquals(String.format("Error: plant 'Scientific Name 2': expected %d alias, but got %d", ALIAS_COUNT, result),
                ALIAS_COUNT, result);
        assertTrue("Error: getPlantAliasByScientificName failed to move to first item", cursor.moveToFirst());
        assertEquals("Error: 'Scientific Name 1': wrong common name", "Common1_alias1".toLowerCase(),
                cursor.getString(cursor.getColumnIndex(PlantContract.AliasEntry.COLUMN_NAME)));
    }

}
