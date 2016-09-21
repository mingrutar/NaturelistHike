package com.coderming.naturalisthike.data_retriever;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by linna on 8/28/2016.
 */
public abstract  class DataRetriever {
    private static final String LOG_TAG = DataRetriever.class.getSimpleName();

    protected Context mContext;

    public abstract void parseData(String str);

    //  detail tags
    public static final String[] TRIP_Tag = new String[]{"club", "check_list", "images",};

    protected String fetchFromLocal(int resId) throws IOException {
//        Log.v(LOG_TAG, "++++ fetchFromLocal: resId=" + mContext.getResources().getResourceEntryName(resId));
        InputStream is = mContext.getResources().openRawResource(resId);
        int length = is.available();
        byte[] buff = new byte[length];
        is.read(buff, 0, length);
        is.close();
        String data = new String(buff);
        if (!data.isEmpty()) {
            parseData(data);
        } else {
            Log.w(LOG_TAG, "fetchFromLocal: empty file");
        }
        return null;
    }

    protected String fetchFromRemote(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
 }