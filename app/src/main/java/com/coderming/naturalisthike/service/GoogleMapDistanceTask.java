package com.coderming.naturalisthike.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.coderming.naturalisthike.model.DistanceInfo;
import com.coderming.naturalisthike.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by linna on 9/13/2016.
 */
/*
 *  works, see https://developers.google.com/maps/documentation/distance-matrix/intro
 https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=47.6851897,-122.2660169&destinations=47.7070313,-122.3451633&key=AIzaSyCNHtRuTOH1xhaR6F_a7BgveA11eNrSOSY
 result:
 {
 "destination_addresses" : [ "10711 Aurora Ave N, Seattle, WA 98133, USA" ],
 "origin_addresses" : [ "7714 58th Ave NE, Seattle, WA 98115, USA" ],
 "rows" : [ {  "elements" : [
    {  "distance" : { "text" : "5.4 mi",  "value" : 8664 },
       "duration" : { "text" : "19 mins",  "value" : 1114 },
       "status" : "OK" } ]  }  ],
 "status" : "OK"
 }
 */

public class GoogleMapDistanceTask extends AsyncTask<LatLng, DistanceInfo, DistanceInfo> {
    private static final String LOG_TAG = GoogleMapDistanceTask.class.getSimpleName();

    String GoogleMapeDistancebaseURL ="https://maps.googleapis.com/maps/api/distancematrix/";

    public interface UpdateResult {
        void updateDrving(int callId, DistanceInfo distanceInfo);
    }

    static final String TAG_UNIT = "units";
    static final String TAG_ORIGIN = "origins";
    static final String TAG_DESTINATION = "destinations";
    static final String TAG_KEY = "key";
    String ApiKey = "AIzaSyCNHtRuTOH1xhaR6F_a7BgveA11eNrSOSY";

    static final String JSON_DEST = "destination_addresses";    // [ "10711 Aurora Ave N, Seattle, WA 98133, USA" ],
    static final String JSON_ORIG = "origin_addresses";         // [ "7714 58th Ave NE, Seattle, WA 98115, USA" ],
    static final String JSON_ROWS = "rows";
    static final String JSON_ELEMENT = "elements";
    static final String JSON_DISTANCE = "distance";            // { "text" : "5.4 mi",  "value" : 8664 } in meter
    static final String JSON_DURATION = "duration";           //{ "text" : "19 mins",  "value" : 1114 } in sec
    static final String JSON_TEXT = "text";
    static final String JSON_VALUE = "value";
    static final String JSON_STATUS = "status";
    static final String Status_OK= "OK";

    static final String mFormat = "json";
    static final String mUnit = "imperial";

    UpdateResult mCallback;
    int mCallId;

    public GoogleMapDistanceTask(int callId, UpdateResult callback) {
        mCallId = callId;
        mCallback = callback;
    }

    public static DistanceInfo parseResponse(String jsonStr) {
        DistanceInfo info = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String str = jsonObject.getString(JSON_STATUS);
            if (Status_OK.equals(str)) {
                info = new DistanceInfo();
                JSONArray jarr = jsonObject.getJSONArray(JSON_DEST);
                info.destAddress = jarr.getString(0);
                jarr = jsonObject.getJSONArray(JSON_ORIG);
                info.origAddress = jarr.getString(0);
                jarr = jsonObject.getJSONArray(JSON_ROWS);
                JSONArray jarr2;
                JSONObject jobj, jobj2;
                boolean bFound = false;
                for (int i =0; i < (jarr.length()) && !bFound; i++) {
                    jsonObject = jarr.getJSONObject(i);
                    jarr2 = jsonObject.getJSONArray(JSON_ELEMENT);
                     for (int j = 0; j < jarr2.length() && !bFound; j++) {
                        jobj = jarr2.getJSONObject(j);
                         if (Status_OK.equals(jobj.getString(JSON_STATUS))) {
                             bFound = true;
                             jobj2 = jobj.getJSONObject(JSON_DISTANCE);
                             info.distanceText = jobj2.getString(JSON_TEXT);
                             info.distance = jobj2.getInt(JSON_VALUE);
                             jobj2 = jobj.getJSONObject(JSON_DURATION);
                             info.durationText = jobj2.getString(JSON_TEXT);
                             info.duration = jobj2.getInt(JSON_VALUE) * Constants.SECOND_INMILLIS;
                         } else {
                             Log.i(LOG_TAG, String.format("Googl Map for row %d, element %d got error status", i, j, str));
                         }
                     }
                }
                if ( !bFound ) {
                    Log.e(LOG_TAG, "++++no valid entry in resp");
                    return null;
                } else {
 //                   Log.v(LOG_TAG, "++++Got it:" + info.toString());
                    return info;
                }
            } else {
                Log.e(LOG_TAG, "Googl Map error status = "+ str);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "+++Caught exception:"+e.getMessage(), e);
        }
        return null;
    }
    String printLatLng(LatLng latLng) {
        return String.format("%f,%f", latLng.latitude, latLng.longitude);
    }
    @Override
    protected DistanceInfo doInBackground(LatLng... latLngs) {
        DistanceInfo ret = null;
        if ( latLngs.length != 2) {
            Log.e(LOG_TAG, "+++wrong number params, expected 2");
            return null;
        }
        LatLng origin = latLngs[0];
        LatLng distination = latLngs[1];
        if ((origin == null) || (distination == null)) {
            Log.e(LOG_TAG, "+++invalid nparams");
            return null;
        }
        try {
            // todo find out how to get Api from manifest
            Uri builtUri = Uri.parse(GoogleMapeDistancebaseURL).buildUpon()
                    .appendPath(mFormat)
                    .appendQueryParameter(TAG_UNIT, mUnit)
                    .appendQueryParameter(TAG_ORIGIN, printLatLng(origin))
                    .appendQueryParameter(TAG_DESTINATION, printLatLng(distination))
                    .appendQueryParameter(TAG_KEY, ApiKey).build();
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Url="+url);

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while (( line=reader.readLine()) != null){
                    sb.append(line);
                }
                reader.close();
                String response = sb.toString();
                Log.v(LOG_TAG, "Google resp="+response);
                return parseResponse(response);
            }
        } catch (ProtocolException | MalformedURLException e) {
            Log.e(LOG_TAG, "Caught exception:"+e.getMessage(), e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Caught exception:"+e.getMessage(), e);
        }
        Log.w(LOG_TAG, "+++return null");
        return ret;
    }

    @Override
    protected void onPostExecute(DistanceInfo info) {
        if (info != null) {
            Log.e(LOG_TAG, "null DistanceInfo obj");
        }
        mCallback.updateDrving(mCallId, info);
    }
}