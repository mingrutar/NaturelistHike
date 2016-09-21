package com.coderming.naturalisthike.utils;

import com.coderming.naturalisthike.R;

/**
 * Created by linna on 8/28/2016.
 */
public class Constants {
    public static final boolean OFFLINE= true;

    public static final long SECOND_INMILLIS = 1000;
    public static final long MINUTE_INMILLIS = 60 * SECOND_INMILLIS;
    public static final long HOUR_INMILLIS = 60 * MINUTE_INMILLIS;
    public static final long DAY_INMILLIS = 24 * HOUR_INMILLIS;

    /// hard set consta
    public static final long SET_CLOCK_ALARM_DAYS_INMILLIS = 3 * DAY_INMILLIS;     // set clock alarm prior trip
    public static final long WEATHER_FORECAST_LIMIT_INMILLIS = 6 * DAY_INMILLIS;   // weather info available

    public static final String URL_WILDFLOWERSEARCH = "http://www.wildflowersearch.org/";
    public static final String URL_MOUNTAINEER_BASE = "https://www.mountaineers.org/";
    public static final String URL_BURKEMUSEUM_BASE = "http://biology.burke.washington.edu/herbarium/imagecollection.php";
    public static final String URL_WEATHER_BASE = "http://forecast.weather.gov/MapClick.php";

    // SharedPreferance: presonal info
    public static final String KEY_PREF_REMINDER_DAYS = "KEY_PREF_REMINDER_DAYS";
    public static final int DEFAULT_REMINDER = 1;          // in days
    public static final String KEY_PREF_PREPARE_TIME = "KEY_PREF_PREPARE_TIME";
    public static final int DEFAULT_PREPARE = 45;           // in min

    public static final String KEY_PREF_HOME_ADDRESS_LAT = "KEY_PREF_HOME_ADDRESS_LAT";
    public static final float DEFAULT_HOME_ADDRESS_LAT = 47.6851897f;       // mountaineer club
    public static final String KEY_PREF_HOME_ADDRESS_LON = "KEY_PREF_HOME_ADDRESS_LON";
    public static final float DEFAULT_HOME_ADDRESS_LON = -122.2660169f;   //  address

    // SharedPreference: current trip only
    public static final String KEY_PREF_CURRENT_TRIP_ID = "KEY_CURRENT_TRIP_ID";      //current tripId
    public static final String DEFAULT_PREF_STRING = "";                        //current tripId
//    public static final String KEY_PREF_MY_MP_NAME = "KEY_PREF_MEETING_PLACE";
//    public static final String KEY_PREF_MY_MP_LAT = "KEY_PREF_MEETING_LAT";
//    public static final String KEY_PREF_MY_MP_LNG = "KEY_PREF_MEETING_LNG";
      public static final String KEY_PREF_WAKEUP_TIME = "KEY_PREF_WAKEUP_TIME";
    public static final long DEFAULT_PREF_TIME = -1;

//    public static final long DEFAULT_PREF_WAKEUP_TIME = -1;

    //SharedPreference: alarm rec Id wirh AlarmManager. used for determination of set and cancel
    public static final String KEY_ACTION_ID_REMINDER = "KEY_REMINDER_ACTION_ID";      // current tripId
    public static final String KEY_ACTION_ID_ALARMCLOCK = "KEY_ACTION_ID_ALARMCLOCK";   //current tripId
    public static final int DEFAULT_ACTION_ID_ALARMACTION = -1;
    public static final int FINISHED_ACTION_ID = -1000;                  //

    // for BroadcatReceiver
    public static final String KEY_ACTION_SOURCE  = "KEY_ACTION_SOURCE";
    public static final int ACTION_TRIP_REMINDER  =  1;                // whatever user set
    public static final int ACTION_SET_ALARM_REMINDER  = 2;            // 3 days prior trip, set alarm
    public static final int ACTION_CHROME_TAB_BUTTON  = 10;            // 3 days prior trip, set alarm

   //
    public static final String STR_TRAIL_HEAD = "Trail Head";
    public static final String  TID_PREFIX = "TID";
    public static final int GEO_LAT = 0;
    public static final int GEO_LON = 1;
    public static final int MIN_TRIP_NAME_LENGTH = 5;

    public static final String TAG_IS_ON_TRAIL = "TAG_IS_ON_TRAIL";
    public static final String TAG_TRAILHEAD_LAT = "TAG_TRAILHEAD_LAT";
    public static final String TAG_TRAILHEAD_LON = "TAG_TRAILHEAD_LON";
    public static final String TAG_DATA_SOURCE_URI = "TAG_DATA_SOURCE_URI";

    // json file names
    public static final int sTripLocalResourceId = R.raw.trips;
    public static final int sPlantLocalResourceId = R.raw.wfs_47_102_121_190;
    public static final int sMeetingPlace = R.raw.meeting_place;

    public static final String sRealFormatter1 = "%.1f";
    // used for favorite plant
    public static final String CURRENT_PLANT_ID = "CURRENT_PLANT_ID" ;

    // GoogleMap
    public static final float DEFAULT_MIN_ZOOM = 7.0f;
    public static final float DEFAULT_MAX_ZOOM = 21.0f;

    public static final String TAG_LATITUDE = "TAG_LATITUDE";
    public static final String TAG_LONGITUDE = "TAG_LONGITUDE";
    public static final String TAG_ADDRESS_NAME = "TAG_ADDRESS_NAME";

    public static final String JSON_ADDR_COUNTY  = "county" ;
    public static final String JSON_ADDR_CITY  = "city" ;
    public static final String JSON_ADDR_NAME  = "name" ;
    public static final String JSON_ADDR_ADDRESS  = "address" ;
    public static final String JSON_ADDR_LATITUDE = "latitude";
    public static final String JSON_ADDR_LONGITUDE = "longitude";

    public static final String DBREC_ID_KEY = "DBREC_ID_KEY";
}
