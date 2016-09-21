package com.coderming.naturalisthike.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by linna on 8/26/2016.
 */
public class TripContract {
    private static final String LOG_TAG = TripContract.class.getSimpleName();

    public static final String CONTENT_AUTHORITY = "com.coderming.naturalisthike.trip";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String DelimPath = "/";

    public static final String PATH_TRIP = "trip";
    public static final String PATH_HIKER = "hiker";
    public static final String PATH_ROSTER = "roster";
    public static final String PATH_TRIP_PLANT = "trip_plant";
    public static final String PATH_CHECK_LIST = "checklist";

    public static final class TripEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SUBTITLE = "subtitle";
        public static final String COLUMN_TH_LATITUDE = "th_lantitude";
        public static final String COLUMN_TH_LONGITUDE = "th_longitude";
        public static final String COLUMN_TRIP_URL = "trip_url";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_ELEVATION = "elevation";
        public static final String COLUMN_HIKE_DATE = "hike_date";
        public static final String COLUMN_MEETING_PLACE = "meeting_place";
        public static final String COLUMN_MEETING_TIME = "meeting_time";
        public static final String COLUMN_MEETING_PLACE_LAN = "mp_latitude";
        public static final String COLUMN_MEETING_PLACE_LON = "mp_longitude";

        public static final String COLUMN_AT_TRAILHEAD_TIME = "at_trailhead_time";
        public static final String COLUMN_HOME_MP_DRIVINGTIME = "home_mp_time";
        public static final String COLUMN_MP_TH_DRIVINGTIME = "mp_th_time";
        public static final String COLUMN_MY_MEETING_PLACE = "my_meeting_place";
        public static final String COLUMN_MY_MEETING_TIME = "my_meeting_time";
        public static final String COLUMN_MY_MP_LATITUDE = "my_mp_latitude";
        public static final String COLUMN_MY_MP_LONGITUDE = "my_mp_longitude";

        public static final String DEFAULT_SORT = COLUMN_HIKE_DATE + " ASC";
         // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIP).build();  //
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_TRIP;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_TRIP;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class HikerEntry implements BaseColumns {
        public static final String TABLE_NAME = "hiker";
        public static final String TRIPS = "trips";

        public static final String COLUMN_FNAME = "fname";
        public static final String COLUMN_LNAME = "lname";
        public static final String COLUMN_EMAIL = "email";
        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HIKER).build();  //
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_HIKER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_HIKER;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
    public enum HikerType {
        Leader("leader"), CoLeader("CoLeader"), Participant("Participant");
        private final String type;
        HikerType(String str) {type = str;}
        public String toString() { return type; }
        public int getValue() {
            return type.equals("leader") ? 10 : (type.equals("CoLeader") ? 11 : 1);
        }
    }

    public static final class RosterEntry implements BaseColumns {
        public static final String TABLE_NAME = "trip_participant";
        public static final String VIEW_NAME = "trip_roster";      // a view

        // trip_participant
        public static final String COLUMN_TRIP_ID = "trip_id";
        public static final String COLUMN_HIKER_ID = "hiker_id";
        public static final String COLUMN_TYPE = "type";
        // hiker
//        public static final String COLUMN_FNAME = "fname";
//        public static final String COLUMN_LNAME = "lname";
//        public static final String COLUMN_EMAIL = "email";

        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROSTER).build();  //
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_ROSTER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_ROSTER;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
    //
    // plant trip
    //
    public enum SpeciesType {
        Plant("plant"), Other("other");
        private final String type;
        SpeciesType(String str) {
            type = str;
        }
        public String toString() { return type; }
        public int getValue() {
            return type.equals("plant") ? 1 : 2;
        }
    }
    public static class PlantTripEntry implements BaseColumns {
        public static final String TABLE_NAME = "plant_trip";           // table

        public static final String PROJECTION =
                "plant_trip._id, trip_id, plant_id, species_name, species_type,is_leader_list,is_observed,photo_uri, voice_uri";
        // table plant_trip
        public static final String COLUMN_TRIP_ID = "trip_id";
        public static final String COLUMN_PLANT_ID = "plant_id";
        public static final String COLUMN_SPECIES_NAME = "species_name";
        public static final String COLUMN_SPECIES_TYPE = "species_type";
        public static final String COLUMN_IS_LEADER_LIST = "is_leader_list";
        public static final String COLUMN_OBSERVED = "is_observed";
        public static final String COLUMN_PHOTO_URI = "photo_uri";
        public static final String COLUMN_VOICE_URI = "voice_uri";
        // table plant
//        public static final String COLUMN_FAMILY = "family";
//        public static final String COLUMN_GENUS = "genus";
//        public static final String COLUMN_SCIENTIFIC = "scientific";
//        public static final String COLUMN_COMMON = "common";

        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIP_PLANT).build();  //
        public static final Uri CONTENT_TRIP_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRIP_PLANT).
                appendPath("trip").build();  //
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_TRIP_PLANT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_TRIP_PLANT;

        public static Uri buildUri(long id) {               // for insert?
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        @Nullable public static Uri buildTripUri(long tripId) {
            return CONTENT_TRIP_URI.buildUpon().appendPath(Long.toString(tripId)).build();
        }
    }
    //
    // Check List
    private static final String TAG_CLUB = "club";
    private static final String TAG_LEADER = "leader";
    private static final String TAG_MY = "my";

    public enum CheckListSelectionType {
        Club(TAG_CLUB), Leader(TAG_LEADER), Personal(TAG_MY);
        private final String type;
        CheckListSelectionType(String str) {type = str;}
        public String toString() { return type; }
        public static CheckListSelectionType getType(int val) {
            return (val == 1) ? Club : ((val==2) ? Leader : Personal);
        }
        public int getValue() {
            return type.equals(TAG_CLUB) ? 1 : (type.equals(TAG_LEADER) ? 2 : 3);
        }
    }

    public static final class CheckListEntry implements BaseColumns {
        public static final String TABLE_NAME = "check_list";
        public static final String CLUB_VIEW = "club_check_list";
        public static final String LEADER_VIEW = "leader_check_list";
        public static final String MY_VIEW = "my_check_list";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IS_OPTIONAL = "is_optional";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TRIP_ID = "trip_id";
        public static final String COLUMN_IS_ALWAYS_CHECK = "is_always_check";
        public static final String COLUMN_IS_CHECKED = "is_checked";

        // URI
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHECK_LIST).build();  //
        public static final Uri CONTENT_CLUB_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHECK_LIST)
                .appendPath(CheckListSelectionType.Club.toString()).build();                                 // /check_list/club
        protected static final Uri CONTENT_LEADER_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHECK_LIST)
                .appendPath(CheckListSelectionType.Leader.toString()).build();                                 // /check_list/leader
        public static final Uri CONTENT_PERSONAL_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHECK_LIST)
                .appendPath(CheckListSelectionType.Personal.toString()).build();                                 // /check_list/my
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_CHECK_LIST;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + DelimPath + CONTENT_AUTHORITY + DelimPath + PATH_CHECK_LIST;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        @Nullable public static Uri buildLeaderCLByTripUri(long tripId) {
            return CONTENT_LEADER_URI.buildUpon().appendPath(Long.toString(tripId)).build();
        }
        @Nullable public static Uri buildPersonalCLByIdUri(long hikeId) {
            return CONTENT_PERSONAL_URI.buildUpon().appendPath(Long.toString(hikeId)).build();
        }
    }
}
