package com.coderming.naturalisthike.ui;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.utils.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetupPersonalInfoActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    private static final String LOG_TAG = SetupPersonalInfoActivity.class.getSimpleName();

    @BindView(R.id.et_prep_time)
    EditText mPrepareTime;
    @BindView(R.id.et_remind_day)
    EditText mReminderDay;
    @BindView(R.id.bt_personal_ok)
    Button bt_Ok;
    @BindView(R.id.bt_personal_cancel)
    Button bt_Cancel;

    private GoogleMap mGoogleMap;
    private SupportMapFragment mMapFragment;
    private Marker mLocationMarker;
    private LatLng mHomeLocation;        // 47.6851897,-122.2660169
    private MarkerOptions mHomeMarkerOptions;

    private final LatLng mClub;           // use club locationa asreference
    private final CameraPosition mStreetCamera;

    public SetupPersonalInfoActivity() {
        mClub = new LatLng(Constants.DEFAULT_HOME_ADDRESS_LAT, Constants.DEFAULT_HOME_ADDRESS_LON );
        mStreetCamera = new CameraPosition.Builder().target(mClub).zoom(12.0f).bearing(0).tilt(0).build();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_personal_info);

        ButterKnife.bind(this, this);

        bt_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int reminderDay = getEnteredValue(mReminderDay);
                if (reminderDay == -1) {
                    reminderDay = Constants.DEFAULT_REMINDER;
                }
                int prepareTime = getEnteredValue(mPrepareTime);
                if (prepareTime == -1) {
                    prepareTime = Constants.DEFAULT_PREPARE;
                }
                if ( (reminderDay > 0) && (prepareTime > 0) )  {
                    updateInfo(reminderDay, prepareTime );
                    finish();
                }
                if (reminderDay <= 0) {
                    mReminderDay.setText(Constants.DEFAULT_PREF_STRING);
                    Toast.makeText(SetupPersonalInfoActivity.this,
                            "Enter a valid reminder day value", Toast.LENGTH_SHORT).show();
                }
                if (prepareTime <= 0) {
                    mPrepareTime.setText(Constants.DEFAULT_PREF_STRING);
                    Toast.makeText(SetupPersonalInfoActivity.this,
                            getString(R.string.setup_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
                }
        });         // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        bt_Cancel.requestFocus();
    }
    int getEnteredValue(TextView textView) {
        String str = textView.getText().toString();
        int ret = -1;
        if (str.length() > 0) {
            ret = Integer.parseInt(str);
        }
        return ret;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
         manualEnterHomeLocation();
    }
    private void manualEnterHomeLocation() {
        mGoogleMap.setOnMapClickListener(clickListener);
        mGoogleMap.setOnMapLongClickListener(longTapListener);
        mGoogleMap.setMinZoomPreference(Constants.DEFAULT_MIN_ZOOM);
        mGoogleMap.setMaxZoomPreference(Constants.DEFAULT_MAX_ZOOM);

//        MarkerOptions markerOptions = new MarkerOptions().position(mClub).title("Mountaineers");
//        mGoogleMap.addMarker(markerOptions);
//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(mClub));
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mStreetCamera));
    }

    GoogleMap.OnMapClickListener clickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            setLocationMark(latLng);
        }
    };
    GoogleMap.OnMapLongClickListener longTapListener = new GoogleMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            setLocationMark(latLng);
        }
    };

    void setLocationMark(LatLng location) {
        bt_Ok.setEnabled(true);

        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }
        mHomeLocation = location;
//        Log.d(LOG_TAG, "setLocation " + location.toString());
        mHomeMarkerOptions = new MarkerOptions();
        mHomeMarkerOptions.position(location)
                .title(getString(R.string.home_location))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mLocationMarker = mGoogleMap.addMarker(mHomeMarkerOptions);
    }

    boolean updateInfo(int reminderDays, int prepareTime) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putFloat(Constants.KEY_PREF_HOME_ADDRESS_LAT, (float) mHomeLocation.latitude)
                .putFloat(Constants.KEY_PREF_HOME_ADDRESS_LON, (float) mHomeLocation.longitude)
                .putInt(Constants.KEY_PREF_PREPARE_TIME, prepareTime)
                .putInt(Constants.KEY_PREF_REMINDER_DAYS, reminderDays)
                .apply();
        return true;
    }
    @Override
    public void onResume() {
        super.onResume();

    }
}
