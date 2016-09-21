package com.coderming.naturalisthike.data_retriever;

import android.content.Context;
import android.util.Log;

import com.coderming.naturalisthike.model.BaseAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linna on 9/18/2016.
 */
// TODO: this is for now, like to use firebase
public class MeetingPlaceRetriever   {
    private static final String LOG_TAG = MeetingPlaceRetriever.class.getSimpleName();

    public static <T extends BaseAddress> List<T> loadAddress(Context context, int resId, Class<T> cls) {
        List<T> ret = new ArrayList<>();
        try {
            InputStream is = context.getResources().openRawResource(resId);
            int length = is.available();
            byte[] buff = new byte[length];
            is.read(buff, 0, length);
            is.close();
            String jsonStr = new String(buff);

            if (!jsonStr.isEmpty()) {
                JSONArray jsonArray = new JSONArray(jsonStr);
                JSONObject jobj;
                T address;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jobj = jsonArray.getJSONObject(i);
                    address = cls.getDeclaredConstructor().newInstance();
                    address.loadJsonObj(jobj);
                    ret.add(address);
                }
            } else {
                Log.w(LOG_TAG, "no PnR address info");
            }
        } catch (JSONException | IOException |NoSuchMethodException |IllegalAccessException | InstantiationException |InvocationTargetException jex) {
            Log.w(LOG_TAG, "caught exception:" + jex.getMessage());
        }
        return ret;
    }
}
