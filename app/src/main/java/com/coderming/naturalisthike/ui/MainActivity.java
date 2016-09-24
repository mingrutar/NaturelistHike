package com.coderming.naturalisthike.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.service.DataRetrieverService;
import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;

import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String CURRENT_PAGE_TAG = "CURRENT_PAGE";
    static final String sCurrentTripSelection = TripContract.TripEntry.COLUMN_HIKE_DATE + ">=?" ;

    static final int LOADER_ID = 10;
    private static String[] QueryProjection = {
            TripContract.TripEntry._ID,
            TripContract.TripEntry.COLUMN_NAME,
            TripContract.TripEntry.COLUMN_HIKE_DATE,
            TripContract.TripEntry.COLUMN_TH_LATITUDE,
            TripContract.TripEntry.COLUMN_TH_LONGITUDE,
            TripContract.TripEntry.COLUMN_AT_TRAILHEAD_TIME };
    static final int COL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_HIKE_DATE = 2;
    static final int COL_TH_LAN = 3;
    static final int COL_TH_LON = 4;
    static final int COL_AT_TH = 5;

    ViewPager mViewPager;

    // TODO: hardcoded for now
    static int[] sBackDropImages = new int[] {R.drawable.rainier, R.drawable.carkeek, R.drawable.teanaway};
    Cursor mCursor;
    MainPagerAdapter mAdapter;
    CollapsingToolbarLayout mCollapsingToolbar;

    ImageView mBackDrop;
    int mCurrentPagePos;
    MenuItem mStartHikeMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasSetup()) {
            Intent setupIntent = new Intent(this, SetupPersonalInfoActivity.class);
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//            stackBuilder.addParentStack(MainActivity.class);
//            stackBuilder.addNextIntent(setupIntent);
            startActivity(setupIntent);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Is it from notification? TODO: checklist if not all filled
        Intent intent = getIntent();
        int actionId = intent.getIntExtra(Constants.KEY_ACTION_SOURCE, -1);
        if (actionId == Constants.ACTION_TRIP_REMINDER) {
//            Log.v(LOG_TAG, "!!! We got reminder");
        }
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mBackDrop = (ImageView) findViewById(R.id.backdrop);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        mAdapter = new MainPagerAdapter( getSupportFragmentManager() );
        mViewPager.setAdapter((mAdapter));
        mViewPager.addOnPageChangeListener( mPageListener );
    }
    boolean hasSetup() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        float homeLan = pref.getFloat(Constants.KEY_PREF_HOME_ADDRESS_LAT, -1.0f);
        float homeLon = pref.getFloat(Constants.KEY_PREF_HOME_ADDRESS_LON, -1.0f);
        return (homeLan != -1.0f) && (homeLon != 1.0f);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCurrentPagePos = PreferenceManager.getDefaultSharedPreferences(this).getInt(CURRENT_PAGE_TAG, -1);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mStartHikeMenuItem = (MenuItem) menu.findItem(R.id.action_start_hike);
        mStartHikeMenuItem.setEnabled(mCursor!=null);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_start_hike) {
                mCursor.moveToPosition(0);
                Utility.startPlantListActivity(this, mCursor.getLong(COL_ID),
                        mCursor.getDouble(COL_TH_LAN), mCursor.getDouble(COL_TH_LON), true);
//                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: initiall version does not support post hike
    private void updateEmptyView() {
        int msgId = R.string.empty_list_no_trip;
        if (Utility.isNetworkAvailable(this)) {    // fetch trips
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
//                    Log.v(LOG_TAG, "+++MainActivity retrieving data..");
                    DataRetrieverService.startFetchTripData(MainActivity.this);
                    return null;
                }
            }.execute();
            msgId = R.string.empty_list_loading;
            // TODO: show progress load data
        }  else {
            msgId = R.string.empty_list_no_network;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.v(LOG_TAG, String.format("-+++onCreateLoader called"));
        Loader<Cursor> ret = null;
        if (id == LOADER_ID) {
            ret = new CursorLoader(this,  // load trip today or later
                    TripContract.TripEntry.CONTENT_URI,
                    QueryProjection,
                    sCurrentTripSelection,
                    new String[]{Long.toString(new Date().getTime())},
                    TripContract.TripEntry.DEFAULT_SORT);
        }
        return ret;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() != LOADER_ID) {
            return;
        }
        mCursor = data;
        if ( (mCursor != null) && (mCursor.moveToFirst()) ) {   // we have trip
            mBackDrop.setImageResource(sBackDropImages[0]);     // TODO, need photo with cursor
            String title = mCursor.getString(COL_TITLE);
            long hikeDate = mCursor.getLong(COL_HIKE_DATE);
            double thLan = mCursor.getDouble(COL_TH_LAN);
            double thLon = mCursor.getDouble(COL_TH_LON);
            if ( Utility.resetIfNewTrip(this, hikeDate, thLan, thLon)) {  //new trip, fetch plants
                new AsyncTask<Double, Void, Void>() {
                    @Override
                    protected Void doInBackground(Double... params) {
                        DataRetrieverService.startFetchPlantData(MainActivity.this, params[0], params[1]);
                        return null;
                    }
                }.execute(thLan, thLon);
            }
            if (mStartHikeMenuItem != null) {
                mStartHikeMenuItem.setEnabled(true);
            }
            if (mCurrentPagePos == -1) {
//                Log.v(LOG_TAG, "++++-onLoadFinished, mCurrentPage = -1, title =" + title);
                mCollapsingToolbar.setTitle(title);
                mCurrentPagePos = 0;
            }
            long hikeStartTime = mCursor.getLong(COL_AT_TH);
            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.setCurrentItem(mCurrentPagePos);
        } else {
            updateEmptyView();
        }
    }
    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putInt(CURRENT_PAGE_TAG, mCurrentPagePos);
        super.onStop();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    /***
     *    OnPageChangeListener
     */
    ViewPager.OnPageChangeListener mPageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int state) {
//            Log.v(LOG_TAG, String.format("-+++onPageScrollStateChanged, state=%d",state));
        }
        @Override
        public void onPageSelected(int position) {
            CharSequence title = mViewPager.getAdapter().getPageTitle(position);
//            Log.v(LOG_TAG, String.format("+++-onPageSelected, pos=%d, mCursor=%s, title=%s", position
//                    , (mCursor==null)?"null":"yes",title ));
            mCollapsingToolbar.setTitle( (title != null) ? title.toString() : getString(R.string.default_title));
            mCurrentPagePos = position;
            int alpha = (position == 0) ? 0xFF : 0x5F;
            mBackDrop.setImageAlpha(alpha);
            mCollapsingToolbar.setEnabled(position == 0);
        }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };

    /***
     *  MainPagerAdapter
     */
    public class MainPagerAdapter extends FragmentStatePagerAdapter {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            long recId;
            synchronized (mCursor) {
                mCursor.moveToPosition(position);
                recId = mCursor.getLong(COL_ID);
            }
//            Log.v(LOG_TAG, String.format("++++getItem: pos=%d, recId=%d", position, recId));
            return MainFragment.newInstance(recId, position);
        }
        @Override
        public int getCount() {
            if (mCursor==null) {
                return 0;
            } else {
                return mCursor.getCount();
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getString(COL_TITLE);
        }
    }
}
