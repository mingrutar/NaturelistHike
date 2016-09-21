package com.coderming.naturalisthike;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coderming.naturalisthike.data.PlantContract;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.service.DataRetrieverService;
import com.coderming.naturalisthike.ui.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

// TODO: 1) test GeoDataUtil
//       2) change trip date
//

public class TestActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String LOG_TAG = MainActivity.class.getSimpleName();

        private static final int TRIP_DBID = 1;
        private static final int PLANT_DBID = 2;

        @BindView(R.id.trip_info) TextView mTripInfo;
        @BindView(R.id.plant_info) TextView mPlantInfo;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_test);

            ButterKnife.setDebug(true);
            ButterKnife.bind(this, this);

            // load data
            getSupportLoaderManager().initLoader(TRIP_DBID, null, this);
            getSupportLoaderManager().initLoader(PLANT_DBID, null, this);
        }

    public void onLoadTrip(View view) {
        DataRetrieverService.startFetchTripData(this);
    }
    public void onLoadPlant(View view) {
        DataRetrieverService.startFetchPlantData(this, 47.102, -121.190);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TRIP_DBID) {
            Uri uri = TripContract.TripEntry.CONTENT_URI;
            String[] projection = null;
            return new CursorLoader(this, uri, projection ,null,null,null );
        } else if (id == PLANT_DBID) {
            Uri uri = PlantContract.PlantEntry.CONTENT_URI;
            String[] projection = null;
            return new CursorLoader(this, uri, projection ,null,null,null );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return ;
        }
        int loadId = loader.getId();
        int count = (data == null) ? -1 : data.getCount();
        if (loadId == TRIP_DBID) {
            String str = String.format("#trip=%d", count);
            mTripInfo.setText(str);
            if (count <= 0) {
                DataRetrieverService.startFetchTripData(this);
                Toast.makeText(this, "no trip data, retrieving...", Toast.LENGTH_LONG).show();
            }
        } else if (loadId == PLANT_DBID) {
            String str = String.format("#plant=%d", count);
            mPlantInfo.setText(str);
            if (count <= 0) {
                DataRetrieverService.startFetchPlantData(this, 47.102, -121.190);
                Toast.makeText(this, "no plant data, retrieving...", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(LOG_TAG, String.format("onLoadFinished: unknown id=%d, #=", loadId, count));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onCleanPrefs(View view) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().clear().commit();
    }
}
