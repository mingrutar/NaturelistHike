package com.coderming.naturalisthike.data_retriever;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.coderming.naturalisthike.data.PlantContract;
import com.coderming.naturalisthike.data.PlantDataHelper;
import com.coderming.naturalisthike.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linna on 8/28/2016.
 */
public class PlantDataFetcher extends DataRetriever {
    private static final String LOG_TAG = PlantDataFetcher.class.getSimpleName();

    private static final String TAG_PLANTS = "plants";
    private static final String TAG_IMAGE_STORE = "image_store";
    private static final String TAG_PLANT_LIST = "plant_list";
    private static final String TAG_COMMON_NAME = "cn";
    private static final String TAG_SCIENTIFIC_NAME = "sn";
    private static final String TAG_IMAGE_URL = "img";

    private static final String HTTP = "http";
    private static final String EmpltyString = "";
    private static final String PathSlash = "/";

    private static PlantDataFetcher sInstance;

    private PlantDataFetcher() {
    }
    public static PlantDataFetcher instance() {
        while (sInstance == null) {
            synchronized (LOG_TAG) {
                sInstance = new PlantDataFetcher();
                break;
            }
        }
        return sInstance;
    }
    public void fetchData(Context context, double lan, double lon) {
        mContext = context;
        try {
            if (Constants.OFFLINE) {
                fetchFromLocal(Constants.sPlantLocalResourceId);
            }  else {
            // TODO from remote,
            }
        } catch (IOException ioex) {
            Log.e(LOG_TAG, "fetchData caught exception: " + ioex.getMessage(), ioex);
        }
    }
    @Override
    public void parseData(String jsonStr) {
        try {
            JSONObject jobj = new JSONObject(jsonStr);
            jobj = jobj.getJSONObject(TAG_PLANTS);
            String imageStore = jobj.getString(TAG_IMAGE_STORE);
            if (!imageStore.startsWith(HTTP)) {
                imageStore = EmpltyString ;
            }
            JSONArray jarr = jobj.getJSONArray(TAG_PLANT_LIST);
            String family = EmpltyString, commonName=EmpltyString, scientificName=EmpltyString, imageUrl=EmpltyString;
            List<ContentValues> list = new ArrayList<>();
            ContentValues values;
            for (int i = 0; i < jarr.length(); i++) {
                jobj = jarr.getJSONObject(i);
                scientificName = jobj.getString(TAG_SCIENTIFIC_NAME);
                if ((scientificName == null) || (scientificName.isEmpty()) ) {
                    continue;
                }
                commonName = jobj.getString(TAG_COMMON_NAME) ;
                imageUrl = jobj.getString(TAG_IMAGE_URL);
                if (!imageUrl.startsWith(imageStore) && !imageStore.isEmpty() ) {
                    imageUrl = imageStore + PathSlash + imageUrl;
                }
                long plantId = PlantDataHelper.findPlant(mContext, scientificName);
                if (plantId == -1 ) {
                    values = new ContentValues();
                    // TODO: ;lookup family name
                    values.put(PlantContract.PlantEntry.COLUMN_FAMILY, family.toLowerCase());
                    values.put(PlantContract.PlantEntry.COLUMN_SCIENTIFIC, scientificName.toLowerCase());
                    values.put(PlantContract.PlantEntry.COLUMN_COMMON, commonName.toLowerCase());
                    values.put(PlantContract.PlantEntry.COLUMN_IMAGE_URL, imageUrl);
                    list.add(values);
                } else {
                    // TODO builkInsert?
                    PlantDataHelper.upsertPlantAlias(mContext, plantId, commonName);
                }
            }
            if (list.size() > 0) {
                ContentValues[] T = new ContentValues[1];
                mContext.getContentResolver().bulkInsert(PlantContract.PlantEntry.CONTENT_URI, list.toArray(T));
            }
        } catch (JSONException jex) {
            Log.w(LOG_TAG, "parseData caught exception " + jex.getMessage(), jex);
        }
    }
}
