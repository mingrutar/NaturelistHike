package com.coderming.naturalisthike.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.PlantContract;
import com.coderming.naturalisthike.utils.Constants;

public class PlantListActivity extends AppCompatActivity {

    long mTripId;
    private FloatingActionButton mFab;
    boolean mIsOnTrail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);  //show back button

        if (savedInstanceState == null) {
            String str = getIntent().getData().getLastPathSegment();
            mTripId = Long.parseLong(str);

            double lat = getIntent().getDoubleExtra(Constants.TAG_TRAILHEAD_LAT,
                    Constants.DEFAULT_HOME_ADDRESS_LAT);
            double lon = getIntent().getDoubleExtra(Constants.TAG_TRAILHEAD_LON,
                    Constants.DEFAULT_HOME_ADDRESS_LON);
            mIsOnTrail = getIntent().getBooleanExtra(Constants.TAG_IS_ON_TRAIL, false);
            PlantListFragment fragment = PlantListFragment.newInstance(
                    PlantContract.PlantEntry.CONTENT_URI, lat, lon, mIsOnTrail);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.plant_list_container, fragment)
                    .commit();
            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setVisibility( mIsOnTrail ? View.VISIBLE : View.INVISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PlantListActivity.this, "coming soon", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
