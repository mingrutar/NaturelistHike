package com.coderming.naturalisthike.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.data.TripDataHelper;
import com.coderming.naturalisthike.data_retriever.MeetingPlaceRetriever;
import com.coderming.naturalisthike.model.BaseAddress;
import com.coderming.naturalisthike.model.DistanceInfo;
import com.coderming.naturalisthike.service.GoogleMapDistanceTask;
import com.coderming.naturalisthike.utils.CalculateTimes;
import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class CarpoolActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = SetupPersonalInfoActivity.class.getSimpleName();
    private static final int REQ_HOME_MP_ID = 30081;
    private static final int REQ_MP_TH_ID = 30082;
    private static final String HOME = "home";
    private static final String MEETING_PLACE = "meeting place";

    private static String[] QueryProjection = {
            TripContract.TripEntry.COLUMN_MEETING_PLACE,
            TripContract.TripEntry.COLUMN_MEETING_TIME,
            TripContract.TripEntry.COLUMN_MEETING_PLACE_LAN,
            TripContract.TripEntry.COLUMN_MEETING_PLACE_LON,
            TripContract.TripEntry.COLUMN_MY_MEETING_PLACE,
            TripContract.TripEntry.COLUMN_MY_MEETING_TIME,
            TripContract.TripEntry.COLUMN_MY_MP_LATITUDE,
            TripContract.TripEntry.COLUMN_MY_MP_LONGITUDE,
            TripContract.TripEntry.COLUMN_AT_TRAILHEAD_TIME,
            TripContract.TripEntry.COLUMN_TH_LATITUDE,
            TripContract.TripEntry.COLUMN_TH_LONGITUDE,
    };

    static final int COL_MEETING_PLACE = 0;
    static final int COL_MEETING_TIME = 1;
    static final int COL_MEETING_PLACE_LAN = 2;
    static final int COL_MEETING_PLACE_LON = 3;
    static final int COL_MY_MEETING_PLACE = 4;
    static final int COL_MY_MEETING_TIME = 5;
    static final int COL_MY_MP_LATITUDE = 6;
    static final int COL_MY_MP_LONGITUDE = 7;
    static final int COL_AT_TRAILHEAD = 8;
    static final int COL_TH_LAT = 9;
    static final int COL_TH_LON = 10;

    long mTripId;
    Cursor mCursor;

    String mUserSelectedTag;

    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;

    private Marker mHomeMarker;
    private Marker mTripMeetMarker;
    private Marker mLocationMarker;

    private List<BaseAddress> mPnRlist;
    private Spinner mSpinner;

    private CameraPosition mStreetCamera;
    private int mMapPadding;

    BaseAddress mMyMeetingInfo;
    BaseAddress mTripMeetingInfo;
    BaseAddress mHome;
    BaseAddress mTrailHeadGeo;

    long myMeetingTime;
    long mTimeAtTH;
    DistanceInfo mMp2THInfo;

    int mTripMPPos;

    TextView tvMeetTime;

    private DistanceInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);  //show back button
        mInfo = new DistanceInfo();
        mTripId = getIntent().getLongExtra(Constants.DBREC_ID_KEY, -1 );
        if (mTripId == -1) {
            Log.e(LOG_TAG, "invalid tripId,... exit");
            finish();
        }
        //
        mUserSelectedTag = getString(R.string.user_selected_location);
        mMapPadding = getResources().getDisplayMetrics().widthPixels / 10;

        tvMeetTime = (TextView) findViewById(R.id.tv_meet_time);

        // list
        mPnRlist = MeetingPlaceRetriever.loadAddress(this, R.raw.meeting_place, BaseAddress.class);
        loadDBData( );
//        Log.v(LOG_TAG, "++++++ # item of PnRlist="+Integer.toString(mPnRlist.size()));
        mSpinner = (Spinner) findViewById(R.id.retrieve_unit);
        PnRSpinnerAdapter  adapter = new PnRSpinnerAdapter (this, R.layout.spinner_item, mPnRlist, mTripMPPos);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                mMyMeetingInfo = mPnRlist.get(pos);
                setLocationMark(mMyMeetingInfo);

                int lastPos = mPnRlist.size() - 1;
                if ( pos <lastPos ) {
                    BaseAddress last = mPnRlist.get(lastPos);
                    if (mUserSelectedTag.equals(last.getName())) {
                        mPnRlist.remove(last);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (mLocationMarker != null) {
                    mLocationMarker.remove();
                }
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.pnr_map);
        mMapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnMapClickListener(clickListener);
        mGoogleMap.setOnMapLongClickListener(longTapListener);
        mGoogleMap.setMinZoomPreference(Constants.DEFAULT_MIN_ZOOM);
        mGoogleMap.setMaxZoomPreference(Constants.DEFAULT_MAX_ZOOM);

        mHome = new BaseAddress(getString(R.string.home_location), Utility.getHomeGeo(this));
        mStreetCamera = new CameraPosition.Builder().target(mHome.getGeo()).zoom(13.0f).bearing(0).tilt(0).build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mStreetCamera));
        MarkerOptions markerOptions = new MarkerOptions().position(mHome.getGeo()).title(mHome.getName())
                .snippet(mHome.toString());
        mHomeMarker = mGoogleMap.addMarker(markerOptions);

        markerOptions = new MarkerOptions().position(mTripMeetingInfo.getGeo()).title(mTripMeetingInfo.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(mTripMeetingInfo.toString());
        mTripMeetMarker = mGoogleMap.addMarker(markerOptions);
        int pos = getItemListPosition(mPnRlist, mMyMeetingInfo);
        if (pos != -1) {
            mSpinner.setSelection(pos);
        } else {
            Log.w(LOG_TAG, "!!!++++onMapReady, pos = -1");
        }
    }
    void loadDBData() {
        Uri uri = TripContract.TripEntry.buildUri(mTripId);
        mCursor = getContentResolver().query(uri, QueryProjection, null, null, null, null);
        if ((mCursor!= null) && mCursor.moveToFirst()) {
            mTimeAtTH = mCursor.getLong(COL_AT_TRAILHEAD);
            myMeetingTime = mCursor.getLong(COL_MY_MEETING_TIME);

            double lat = mCursor.getDouble(COL_MEETING_PLACE_LAN);
            double lon = mCursor.getDouble(COL_MEETING_PLACE_LON);
            String name = mCursor.getString(COL_MEETING_PLACE);
            mTripMeetingInfo = new BaseAddress(name, new LatLng(lat, lon));
            int pos = getItemListPosition(mPnRlist, mTripMeetingInfo);
            if (pos == -1) {
                mPnRlist.add(mTripMeetingInfo);
                mTripMPPos = mPnRlist.size() - 1;
            } else {
                mTripMPPos = pos;
            }
            lat = mCursor.getDouble(COL_MY_MP_LATITUDE);
            lon = mCursor.getDouble(COL_MY_MP_LONGITUDE);
            name = mCursor.getString(COL_MY_MEETING_PLACE);
            mMyMeetingInfo = new BaseAddress(name, new LatLng(lat, lon));
            pos = getItemListPosition(mPnRlist, mMyMeetingInfo);
            if (pos == -1) {
                mPnRlist.add(mMyMeetingInfo);
            }
            lat = mCursor.getDouble(COL_TH_LAT);
            lon = mCursor.getDouble(COL_TH_LON);
            mTrailHeadGeo = new BaseAddress(getString(R.string.trailhead_location), new LatLng(lat, lon));
        } else {
            Log.e(LOG_TAG, "invalid trip data,... exit");
            finish();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        long home2mp = (mInfo != null) ? mInfo.duration : -1;
        long mp2th = (mMp2THInfo != null) ? mMp2THInfo.duration : -1;
        TripDataHelper.updateMyMeetingPlace(this, mTripId, mMyMeetingInfo.getName(),
                myMeetingTime, mMyMeetingInfo.getGeo(), home2mp, mp2th );
    }
    GoogleMap.OnMapClickListener clickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
        if (mLocationMarker != null)
            mLocationMarker.showInfoWindow();
        }
    };
    GoogleMap.OnMapLongClickListener longTapListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            addUserLocation(latLng);
        }
    };
    void addUserLocation(LatLng latLng ) {
        int pos = mPnRlist.size() - 1;
        if (mUserSelectedTag.equals(mPnRlist.get(pos).getName())) {
            mMyMeetingInfo = mPnRlist.get(pos);
            mMyMeetingInfo.setGeo(latLng);
        } else {
            mMyMeetingInfo.setName(mUserSelectedTag);
            mMyMeetingInfo.setGeo(latLng);
            mPnRlist.add(mMyMeetingInfo);
            pos = mPnRlist.size() - 1;
        }
        mSpinner.setSelection(pos);
        setLocationMark(mMyMeetingInfo);
    }
    int getItemListPosition(List<BaseAddress> list, BaseAddress address) {
        if (address != null) {
            for (int i = 0; i < list.size(); i++) {
                if (address.getName().equals(list.get(i).getName())) {
                    return i;
                }
            }
        }
        return -1;
    }
    GoogleMapDistanceTask.UpdateResult mDistanceHandler = new GoogleMapDistanceTask.UpdateResult() {
        @Override
        public void updateDrving(int recPos, DistanceInfo info) {
            if (recPos == REQ_HOME_MP_ID) {
                mInfo = info;
                if (mMarkerOptions != null) {
                    updateMap();
                }
            } else if (recPos == REQ_MP_TH_ID) {
                mMp2THInfo = info;
                myMeetingTime = CalculateTimes.calcMeetTime(mTimeAtTH, info.duration);
                String str = Utility.formatTimeOnly(myMeetingTime);
                tvMeetTime.setText(str);
            }
        } };
    MarkerOptions mMarkerOptions;
    void setLocationMark(BaseAddress address) {
        if ((address == null) || (address.getGeo() == null))
            return;
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }
        mMarkerOptions = null;
        new GoogleMapDistanceTask(REQ_HOME_MP_ID, mDistanceHandler)
                .execute(mHomeMarker.getPosition(), address.getGeo());
        new GoogleMapDistanceTask(REQ_MP_TH_ID, mDistanceHandler)
                .execute(address.getGeo(), mTrailHeadGeo.getGeo());
        mMarkerOptions = new MarkerOptions()
                .position(address.getGeo())
                .title(address.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        Log.d(LOG_TAG, "setLocationMark " + address.toString());
    }
    void updateMap() {
        mMarkerOptions.snippet(mInfo.toString(HOME, MEETING_PLACE));
        mLocationMarker = mGoogleMap.addMarker(mMarkerOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mHomeMarker.getPosition());
        builder.include(mLocationMarker.getPosition());
        builder.include(mTripMeetMarker.getPosition());
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, mMapPadding);
        mGoogleMap.moveCamera(cu);
        mHomeMarker.showInfoWindow();
        mTripMeetMarker.showInfoWindow();
        mLocationMarker.showInfoWindow();
    }
}
