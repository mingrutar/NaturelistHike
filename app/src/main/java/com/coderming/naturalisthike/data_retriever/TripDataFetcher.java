package com.coderming.naturalisthike.data_retriever;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.coderming.naturalisthike.data.TripCheckListDataHelper;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.data.TripDataHelper;
import com.coderming.naturalisthike.data.TripPlantDataHelper;
import com.coderming.naturalisthike.utils.CalculateTimeException;
import com.coderming.naturalisthike.utils.CalculateTimes;
import com.coderming.naturalisthike.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by linna on 8/28/2016.
 */
public class TripDataFetcher extends DataRetriever {
    private static final String LOG_TAG = TripDataFetcher.class.getSimpleName();

    private static TripDataFetcher sInstance;
    private static final String JSON_NAME = "name";
    private static final String JSON_EMAIL = "email";
    private static final String JSON_CHECK_LIST = "club_check_list";
    private static final String JSON_TRIPS = "trips";
    private static final String JSON_TRIP_URL = "trip_url";
    private static final String JSON_REGION = "region";
    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_ELEVATION = "elevation";
    private static final String JSON_LEADER = "leader";
    private static final String JSON_PARTICIPANTS = "participants";
    private static final String JSON_LEADER_CHECK_LIST = "leader_check_list";
    private static final String JSON_LEADER_PLANTS_LIST = "leader_plants_list";
    private static final String JSON_LEADER_OTHER_LIST = "leader_other_list";

    private TripDataFetcher() {
    }

    public static TripDataFetcher instance() {
        while (sInstance == null) {
            synchronized (LOG_TAG) {
                sInstance = new TripDataFetcher();
                break;
            }
        }
        return sInstance;
    }
    public void fetchData(Context context) {
        mContext = context;
        try {
            if (Constants.OFFLINE) {
                fetchFromLocal(Constants.sTripLocalResourceId);
            }  else {
                // TODO from remote,
            }
        } catch (IOException ioex) {
            Log.e(LOG_TAG, "fetchData caught exception " + ioex.getMessage(), ioex);
        }
    }
    void addHikes(long tripId, JSONArray jarr, boolean isLeader) throws JSONException {
        JSONObject jobj;
        String name;
        String email;
        TripContract.HikerType type;
        for (int i = 0; i < jarr.length(); i++) {
            jobj = jarr.getJSONObject(i);
            name = jobj.getString(JSON_NAME);
            email = jobj.getString(JSON_EMAIL);
            if (isLeader) {
                type = (i==0) ? TripContract.HikerType.Leader : TripContract.HikerType.CoLeader;
            } else {
                type = TripContract.HikerType.Participant;
            }
            TripDataHelper.upsertHiker(mContext, tripId, name, email, type);
        }
    }
    void addLeaderCheckList(long tripId, JSONArray jarr) throws JSONException {
        JSONObject jobj;
        String item;
        boolean isOptional;
        for (int i = 0; i < jarr.length(); i++) {
            jobj = jarr.getJSONObject(i);
            item = jobj.getString("item");
            isOptional = "no".equals(jobj.getString("required").toLowerCase());
            TripCheckListDataHelper.upsertLeaderCheckList(mContext, tripId, item, isOptional);
        }
    }
    void addLeaderSpecisList(long tripId, JSONArray jarr, boolean isPlant) throws JSONException {
        TripContract.SpeciesType type = isPlant ? TripContract.SpeciesType.Plant : TripContract.SpeciesType.Other;
        for (int i = 0; i < jarr.length(); i++) {
            TripPlantDataHelper.insertLeaderSpeciesList(mContext, tripId, jarr.getString(i), type);
        }
    }
    @Override
    public void parseData(String jsonStr) {
        try {
            JSONObject jobjRoot = new JSONObject(jsonStr);
            JSONArray jsonArray = jobjRoot.getJSONArray(JSON_CHECK_LIST);
            for (int i = 0; i < jsonArray.length(); i++) {
                TripCheckListDataHelper.upsertClubCheckList(mContext, jsonArray.getString(i));
            }
//            Log.v(LOG_TAG, "parseData: #club_checklist=" + Integer.toString(jsonArray.length()));
            jsonArray = jobjRoot.getJSONArray(JSON_TRIPS);
            String str, meetingPlace;
            ContentValues cv;
            JSONArray jsonSubArray;
            Uri uri;
            CalculateTimes calculateTimes;
            double lantitude, longitude;
            JSONObject jobj;
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    cv = new ContentValues();
                    jobj = jsonArray.getJSONObject(i);
                    str = jobj.getString(JSON_TRIP_URL);
                    if ((str == null) || !str.startsWith(Constants.URL_MOUNTAINEER_BASE)) {
                        Log.w(LOG_TAG, "Invalid trip: url ="+str);
                        continue;
                    }
                    //TODO: update trip not done
                    Cursor cursor = TripDataHelper.findTripByUrl(mContext, str);
                    if ( (cursor == null) || (cursor.getCount()==0 )) {    // new trip
                        cv.put(TripContract.TripEntry.COLUMN_TRIP_URL, str);
                        cv.put(TripContract.TripEntry.COLUMN_NAME, jobj.getString(JSON_NAME));

                        cv.put(TripContract.TripEntry.COLUMN_SUBTITLE, jobj.getString(JSON_REGION));
                        cv.put(TripContract.TripEntry.COLUMN_DISTANCE, jobj.getDouble(JSON_DISTANCE));
                        cv.put(TripContract.TripEntry.COLUMN_ELEVATION, jobj.getDouble(JSON_ELEVATION));

                        calculateTimes = new CalculateTimes(mContext, jobj);
                        calculateTimes.addToContentValues(cv);

                        uri = mContext.getContentResolver().insert(TripContract.TripEntry.CONTENT_URI, cv);
                        if (uri != null) {
                            str = uri.getLastPathSegment();
                            long tripId = Long.parseLong(str);
                            calculateTimes.getDrivingDistance(mContext, tripId);

                            jsonSubArray = jobj.getJSONArray(JSON_LEADER);
                            addHikes(tripId, jsonSubArray, true);
                            jsonSubArray = jobj.getJSONArray(JSON_PARTICIPANTS);
                            addHikes(tripId, jsonSubArray, false);
                            jsonSubArray = jobj.getJSONArray(JSON_LEADER_CHECK_LIST);
                            addLeaderCheckList(tripId, jsonSubArray);

                            jsonSubArray = jobj.getJSONArray(JSON_LEADER_PLANTS_LIST);
                            addLeaderSpecisList(tripId, jsonSubArray, true);

                            jsonSubArray = jobj.getJSONArray(JSON_LEADER_OTHER_LIST);
                            addLeaderSpecisList(tripId, jsonSubArray, false);
                        }
                    }
                }catch (JSONException | ParseException | CalculateTimeException jjex) {
                    Log.i(LOG_TAG, "parseData bad data for trip " + Integer.toString(i), jjex);
                }
            }
//            Log.v(LOG_TAG, "parseData: #trip=" + Integer.toString(jsonArray.length()));
        } catch (JSONException jex) {
            Log.e(LOG_TAG, "parseData caught exception:"+jex.getMessage(), jex);
        }
    }
}
