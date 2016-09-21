package com.coderming.naturalisthike.utils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.data.TripDataHelper;
import com.coderming.naturalisthike.model.DistanceInfo;
import com.coderming.naturalisthike.service.GoogleMapDistanceTask;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by linna on 9/7/2016.
 */
public class CalculateTimes {
    private static final String LOG_TAG = CalculateTimes.class.getSimpleName();

    private static final String JSON_DATE = "date";
    private static final String JSON_MEETING_TIME = "meeting_time";
    private static final String JSON_MEETING_PLACE = "meeting_place";
    private static final String JSON_MP_LATITUDE = "mp_latitude";
    private static final String JSON_MP_LONGITUDE = "mp_longitude";
    // from input json
    String mMeetingPlace;
    long mMeetingTime = -1;
    long mHikeDate = -1;
    LatLng mTrailHeadLanLon;       // lan, lon
    LatLng mMeetingPlaceLanLon;     // lan

    public CalculateTimes(Context context, JSONObject jsonObject)
            throws JSONException, ParseException{
        mTrailHeadLanLon = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("logitude") );

        //TODO: validate ?
        String str = jsonObject.getString(JSON_DATE);
        mHikeDate = Utility.sDateFormat.parse(str).getTime();
        str = str + " " + jsonObject.getString(JSON_MEETING_TIME);
        mMeetingTime = Utility.sDateTimeFormat.parse(str).getTime();
        str = jsonObject.getString(JSON_MEETING_PLACE);
        mMeetingPlace = (str!=null) ? str : Constants.STR_TRAIL_HEAD;   // set trail head as meeting place
        mMeetingPlaceLanLon = new LatLng(jsonObject.getDouble("mp_latitude"), jsonObject.getDouble(JSON_MP_LONGITUDE));

//        Log.v(LOG_TAG, String.format("ctor: mTrailHeadLanLon=(%f, %f),HikeDate=%d,mMeetingTime=%d ",
//                mTrailHeadLanLon.latitude, mTrailHeadLanLon.longitude, mHikeDate, mMeetingTime));
    }
    public void addToContentValues(ContentValues cv) throws CalculateTimeException {
        cv.put(TripContract.TripEntry.COLUMN_TH_LATITUDE, mTrailHeadLanLon.latitude);
        cv.put(TripContract.TripEntry.COLUMN_TH_LONGITUDE, mTrailHeadLanLon.longitude);
        cv.put(TripContract.TripEntry.COLUMN_HIKE_DATE, mHikeDate);
        cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE, mMeetingPlace);
        cv.put(TripContract.TripEntry.COLUMN_MEETING_TIME, mMeetingTime);
        cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LAN, mMeetingPlaceLanLon.latitude);
        cv.put(TripContract.TripEntry.COLUMN_MEETING_PLACE_LON, mMeetingPlaceLanLon.longitude);
        // add my place as trip's
        cv.put(TripContract.TripEntry.COLUMN_MY_MEETING_PLACE, mMeetingPlace);
        cv.put(TripContract.TripEntry.COLUMN_MY_MEETING_TIME, mMeetingTime);
        cv.put(TripContract.TripEntry.COLUMN_MY_MP_LATITUDE, mMeetingPlaceLanLon.latitude);
        cv.put(TripContract.TripEntry.COLUMN_MY_MP_LONGITUDE, mMeetingPlaceLanLon.longitude);
    }

    public void getDrivingDistance(Context context, long tripId) {
        LatLng homeGeo = Utility.getHomeGeo(context);
        DrivingTimeHandler handler = new DrivingTimeHandler(context, tripId, mMeetingTime);
        new GoogleMapDistanceTask(DrivingTimeHandler.REQ_HOME2MP, handler).execute(homeGeo, mMeetingPlaceLanLon);
        new GoogleMapDistanceTask(DrivingTimeHandler.REQ_MP2TH, handler).execute(mMeetingPlaceLanLon, mTrailHeadLanLon);
    }

    public static class DrivingTimeHandler implements GoogleMapDistanceTask.UpdateResult {
        public static final int REQ_HOME2MP = 20001;
        public static final int REQ_MP2TH = 20002;

        long mTripId;
        long mMeetAt;
        Context mContext;

        public DrivingTimeHandler(Context context, long tripId, long meetingTime) {
            mTripId = tripId;
            mContext = context;
            mMeetAt = meetingTime;
        }
        @Override
        public void updateDrving(int recPos, DistanceInfo info) {
            int ret = -1;
            long drivingTime = info.duration;
            if (recPos == REQ_HOME2MP) {
                ret =TripDataHelper.updateHomeMpDrivingTime(mContext, mTripId, drivingTime);
            } else if (recPos == REQ_MP2TH) {
                long atTrailHeadTime = calcHikeStartTime(mMeetAt, drivingTime);
                ret = TripDataHelper.updateTHTimeandDriving(mContext, mTripId, atTrailHeadTime, drivingTime);
            }
            Log.i(LOG_TAG, String.format("mDistanceHandler reqId=%d, numRecUpdated=%d",recPos, ret));
        }
    };

    public static long calcHikeStartTime(long meetingTime, long driving) {
        long dtMinute = driving / Constants.MINUTE_INMILLIS;
        long bufftime;
        if (dtMinute < 30) {
            bufftime = 5;
        } else if (dtMinute < 60) {
            bufftime = 15;
        } else if (dtMinute < 90) {
            bufftime = 30;
        } else {
            bufftime = 45;
        }
        return  meetingTime + driving + (bufftime * Constants.MINUTE_INMILLIS);

    }
    // TODO: ask S. H.
    public static long calcMeetTime(long startHikeTime, long driving) {
        long dtMinute = driving / Constants.MINUTE_INMILLIS;
        long bufftime;
        if (dtMinute < 30) {
            bufftime = 5;
        } else if (dtMinute < 60) {
            bufftime = 15;
        } else if (dtMinute < 90) {
            bufftime = 30;
        } else {
            bufftime = 45;
        }
        return  startHikeTime - driving - (bufftime * Constants.MINUTE_INMILLIS);

    }

}
