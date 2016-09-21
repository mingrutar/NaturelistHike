package com.coderming.naturalisthike.model;

import com.coderming.naturalisthike.utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by linna on 9/18/2016.
 */
public class BaseAddress {
    public void setName(String name) {
        this.mName = name;
    }

    private static final String sGeoFormatter = "%f, %f";
    private static final String sFullFormatter = "%s(%f,%f)";
    String mName;
    LatLng mGeo;

    public BaseAddress() { }

    public BaseAddress(String name, LatLng geo) {
        this.mName = name;
        this.mGeo = geo;
    }
    public LatLng getGeo() {
        return mGeo;
    }
    public String getName() {
        return mName;
    }
    public void setGeo(LatLng geo) {
        this.mGeo = geo;
    }

    public String toGeoString() {
        return String.format(sGeoFormatter, mGeo.latitude, mGeo.longitude);
    }
    @Override
    public String toString() {
        return String.format(sFullFormatter, mName, mGeo.latitude, mGeo.longitude);
    }
    public void loadJsonObj(JSONObject jobj) throws JSONException {
        mName = (jobj.getString(Constants.JSON_ADDR_NAME));
        double latitude = jobj.getDouble(Constants.JSON_ADDR_LATITUDE);
        double longitude = jobj.getDouble(Constants.JSON_ADDR_LONGITUDE);
        mGeo = new LatLng(latitude, longitude);
    }

    // TODO: for now. ask S.H.
    public boolean isSameLocation(LatLng right) {
        return ( (Math.abs(mGeo.latitude - right.latitude) < 0.005) &&
                (Math.abs(mGeo.longitude - right.longitude) < 0.005) );
    }
//    public void save(Context context, long tripId, String mName, long meeTime, LatLng geo) {
//        TripDataHelper.updateMyMeetingPlace(context, tripId, mName, meeTime, geo);
//    }
}
