package com.coderming.naturalisthike.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by linna on 8/26/2016.
 */
public class PlantContract {
    private static final String LOG_TAG = PlantContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.coderming.naturalisthike.plantlist";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PLANTS = "plants";
    public static final String PATH_PLAN_ALLIAS = "alias";

    public static final String SCIENTIFIC = "scientic";

    public static final String DEFAULT_SORT = PlantEntry.COLUMN_SCIENTIFIC + " ASC";

    public static final class PlantEntry implements BaseColumns {
        public static final String TABLE_NAME = "plant_list";
        public static final String FAVORITE_VIEW = "favorite_plant";

        public static final String PROJECTION="family, scientific, common, image_url, is_favor";
        public static final String FAVORITE = "favorite";

        public static final String COLUMN_FAMILY = "family";
        public static final String COLUMN_SCIENTIFIC = "scientific";
        public static final String COLUMN_COMMON = "common";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_IS_FAVOR = "is_favor";
        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();  // plant
        public static final Uri CONTENT_FAVORITE_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS)
                .appendPath(FAVORITE).build();                      // plant favority
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLANTS;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSearchUri(String scientificName) {
            return CONTENT_URI.buildUpon().appendPath(SCIENTIFIC).appendPath(scientificName).build();
//        public static Uri buildSearchUri(String scientificName) {
//            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_SCIENTIFIC, scientificName).build();
        }
    }
    public static final class AliasEntry implements BaseColumns {
        public static final String TABLE_NAME = "plant_alias";

        public static final String COLUMN_PLANT_ID = "plant_id";
        public static final String COLUMN_NAME = "name";
        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAN_ALLIAS).build();
        public static final Uri CONTENT_PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAN_ALLIAS)
                .appendPath(SCIENTIFIC).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        public static Uri buildUri(long aliasId) {
            return ContentUris.withAppendedId(CONTENT_URI, aliasId);
        }
    }
}