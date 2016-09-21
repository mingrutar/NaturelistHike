package com.coderming.naturalisthike.model;

import com.coderming.naturalisthike.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by linna on 9/18/2016.
 */
public class PnRAddress extends BaseAddress
        implements Serializable {

    String county;
    String city;
    String address;

    public PnRAddress() {}

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void loadJsonObj(JSONObject jobj) throws JSONException {
        super.loadJsonObj(jobj);
        county = jobj.getString(Constants.JSON_ADDR_COUNTY);
        city = jobj.getString(Constants.JSON_ADDR_CITY);
        address = jobj.getString(Constants.JSON_ADDR_ADDRESS);

    }
}
