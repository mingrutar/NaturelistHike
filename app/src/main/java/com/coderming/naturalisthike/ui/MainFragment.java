package com.coderming.naturalisthike.ui;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.chrome_tab.CustomTabActivityHelper;
import com.coderming.naturalisthike.chrome_tab.WebviewFallback;
import com.coderming.naturalisthike.data.TripContract;
import com.coderming.naturalisthike.utils.Constants;
import com.coderming.naturalisthike.utils.Utility;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    private static final String UNIQUE_ID = "UNIQUE_ID";

    private static String[] QueryProjection = {
            TripContract.TripEntry.COLUMN_DISTANCE,
            TripContract.TripEntry.COLUMN_ELEVATION,
            TripContract.TripEntry.COLUMN_TRIP_URL,
            TripContract.TripEntry.COLUMN_HIKE_DATE,

            TripContract.TripEntry.COLUMN_TH_LATITUDE,  //plant and weather
            TripContract.TripEntry.COLUMN_TH_LONGITUDE,

            TripContract.TripEntry.COLUMN_MY_MEETING_PLACE,
            TripContract.TripEntry.COLUMN_MY_MEETING_TIME,
            TripContract.TripEntry.COLUMN_HOME_MP_DRIVINGTIME,
            TripContract.TripEntry.COLUMN_AT_TRAILHEAD_TIME
    };

    static final int COL_DISTANCE = 0;
    static final int COL_ELEVATION = 1;
    static final int COL_TRIP_URL = 2;
    static final int COL_HIKE_DATE = 3;
    static final int COL_TH_LATITUDE = 4;  //plant and weather
    static final int COL_TH_LONGITUDE = 5;
    static final int COL_MEETING_PLACE = 6;
    static final int COL_MEETING_TIME = 7;
    static final int COL_HOME_MP_TIME = 8;
    static final int COL_AT_TRAILHEAD_TIME = 9;

    // trip info
    @BindView(R.id.mileage) TextView tvMilege;
    @BindView(R.id.elevation) TextView tvElevation;
    @BindView(R.id.hike_date) TextView tvHikeDate;
    // plant list
    @BindView(R.id.num_leader_favor) TextView tvNumLeaderFavor;
    @BindView(R.id.num_my_favor) TextView tvNumMyFavor;
    // carpool
    @BindView(R.id.meeting_time) TextView tvMeetingTime;
    @BindView(R.id.meeting_place) TextView tvMeetingPlace;
    @BindView(R.id.num_car_mate) TextView tvNumCarMates;
    @BindView(R.id.at_trail_head) TextView tvTimeToTH;

    // checklist
    @BindView(R.id.num_todos) TextView tvNumToDos;
    // reminder
    @BindView(R.id.reminder_time) TextView tvReminderTime;
    @BindView(R.id.reminder_switch) SwitchCompat mRemindSwitch;
    // alarm time
    @BindView(R.id.alarm_time) TextView tvAlarmTime;
    @BindView(R.id.alarm_switch) SwitchCompat mAlamSwitch;
    @BindView(R.id.driving_time) TextView tv_home2MPTime;
    // weather
    @BindView(R.id.ll_weather_segment) LinearLayout mWeather_segment;
    @BindView(R.id.weather_divider) View mweather_divider;

    private Cursor mCursor;
    private int mUniqId;

    String mHikeUrl;
    long mTripDate;
    double[] mTHGeoData = new double[2];
    long mTripId;

    public MainFragment() {
        // Required empty public constructor
    }

    static public MainFragment newInstance(long recId, int uniqId) {
        MainFragment ret = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.DBREC_ID_KEY, recId);
        bundle.putInt(UNIQUE_ID, uniqId);
        ret.setArguments(bundle);
        return ret;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mTripId = getArguments().getLong(Constants.DBREC_ID_KEY);
        ButterKnife.bind( this, rootView );

        mUniqId = getArguments().getInt(UNIQUE_ID);

        setClickHandlers(rootView);

        return rootView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(mUniqId, getArguments(), this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.v(LOG_TAG, String.format("-+++onCreateLoader called"));
        Loader<Cursor> ret = null;
        if ((id == mUniqId) && (args != null)) {
            long itemId = args.getLong(Constants.DBREC_ID_KEY);
            Uri uri = TripContract.TripEntry.buildUri(itemId);
            ret = new CursorLoader(getContext(), uri, QueryProjection, null, null, null);
        }
        return ret;
    }
    void updatViews() {
        mHikeUrl = mCursor.getString(COL_TRIP_URL);
//        Log.v(LOG_TAG, "++++onLoadFinished Trip_url=" + mHikeUrl);
        mTHGeoData[Constants.GEO_LAT] = mCursor.getDouble(COL_TH_LATITUDE);
        mTHGeoData[Constants.GEO_LON] = mCursor.getDouble(COL_TH_LONGITUDE);

        mTripDate = mCursor.getLong(COL_HIKE_DATE);
        int visible = Utility.isWeatherAvailable(mTripDate) ? View.VISIBLE : View.INVISIBLE;
        mWeather_segment.setVisibility( visible);
        mweather_divider.setVisibility(visible);

        String str;
        str = Utility.format_double_1f(mCursor.getDouble(COL_DISTANCE));
        tvMilege.setText(str);       // tripInfo
        str = Utility.format_double_1f(mCursor.getDouble(COL_ELEVATION));
        tvElevation.setText(str);
        str = Utility.formatDateOnly(mTripDate);
        tvHikeDate.setText(str);
        long timeVal = mCursor.getLong(COL_MEETING_TIME);
        str = Utility.formatTimeOnly(timeVal);
        tvMeetingTime.setText(str); //Meeting
        str = mCursor.getString(COL_MEETING_PLACE);
        tvMeetingPlace.setText(str);
        timeVal = mCursor.getLong(COL_AT_TRAILHEAD_TIME);
        str = Utility.formatAtTrailHeader(timeVal);
        tvTimeToTH.setText(str);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int reminderDay = pref.getInt(Constants.KEY_PREF_REMINDER_DAYS, Constants.DEFAULT_REMINDER);
        tvReminderTime.setText( String.format("%d Day%s", reminderDay, (reminderDay>1)?"s":""));

        long wakeupTime = getWakeupTime();
        tvAlarmTime.setText(Utility.formatTimeOnly(wakeupTime));
        timeVal = mCursor.getLong(COL_HOME_MP_TIME);
        str = Utility.formatHome2MPTime(timeVal);
        tv_home2MPTime.setText(str);

        if (mRemindSwitch.isChecked()) {
            Utility.setReminder(getContext(), mTripDate);
        }
        if (mAlamSwitch.isChecked()) {
            if (Utility.setAlarm(getContext(), mTripDate)) {
                Utility.setWakeupTime(getContext(), getWakeupTime());
            }
        }
     }
    long getWakeupTime() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if ((mCursor != null) && mCursor.moveToFirst()) {
            long meetTime = mCursor.getLong(COL_MEETING_TIME);
            long home2mp = mCursor.getLong(COL_HOME_MP_TIME);
            int prepareMin = pref.getInt(Constants.KEY_PREF_PREPARE_TIME, Constants.DEFAULT_PREPARE);
            return meetTime - home2mp - (prepareMin * Constants.MINUTE_INMILLIS);
        } else {
            return -1;
        }
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ((data != null) && data.moveToFirst()){
            mCursor = data;
            updatViews();

        } else {
            Log.e(LOG_TAG, "onLoadFinished trip onfo not found");
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       if (mCursor != null) {
           mCursor.close();
           mCursor = null;
       }
    }
    //TODO: is there better place to check?
   @Override
    public void onStop() {
        super.onStop();
       if ((mCursor != null) && mCursor.moveToFirst()) {
           long tripData = mCursor.getLong(COL_HIKE_DATE);
           if (mRemindSwitch.isChecked()) {
               Utility.setReminder(getContext(), tripData);
           }
           if (mAlamSwitch.isChecked()) {
                Utility.setAlarm(getContext(), tripData);
           }
       }
   }

    private void setClickHandlers(View rootView ) {
        rootView.findViewById(R.id.ll_hike_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(mHikeUrl);
                showInChromeBrowser(uri);
            }
        });
        rootView.findViewById(R.id.ll_plant_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCursor.moveToFirst();
                Utility.startPlantListActivity(getContext(), mTripId,
                    mCursor.getDouble(COL_TH_LATITUDE),
                    mCursor.getDouble(COL_TH_LONGITUDE), false );
            }
        });
        rootView.findViewById(R.id.ll_carpool).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCarpooling(view);
            }
        });
        rootView.findViewById(R.id.ll_checklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChecklistActivity.class);
                Uri uri = TripContract.TripEntry.buildUri(mTripId);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        final View reminderView = rootView.findViewById(R.id.ll_reminder);
        reminderView.setEnabled(mRemindSwitch.isChecked());
        mRemindSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean isEnable = mRemindSwitch.isChecked();
                reminderView.setEnabled(isEnable);
                reminderView.findViewById(R.id.reminder_inner).setVisibility(isEnable ? View.VISIBLE : View.INVISIBLE);
                reminderView.findViewById(R.id.disable_reminder).setVisibility(isEnable ? View.INVISIBLE : View.VISIBLE);
                int colorRid = isEnable ? 0xFF : 0x5F;
                AppCompatImageView image = (AppCompatImageView)reminderView.findViewById(R.id.icon_reminder);
                int temp = image.getImageAlpha();
//                Log.v(LOG_TAG, "+++@@@ alpha is " + Integer.toString(temp));
                image.setImageAlpha(colorRid);
            }
        });
        reminderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            changeReminder();
            }
        });

        setAlarmView(rootView.findViewById(R.id.ll_set_alarm));

        rootView.findViewById(R.id.ll_weather_segment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Utility.getWeatherInfoUrl( mTHGeoData[0], mTHGeoData[1]);
                showInChromeBrowser(uri);
            }
        });
    }
    void changeReminder() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        int days = pref.getInt(Constants.KEY_PREF_REMINDER_DAYS, Constants.DEFAULT_REMINDER);
        final EditText input = new EditText(getContext());
        input.setHint(Integer.toString(days));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        final AlertDialog dlg = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.set_reminder_dialog_title)).setMessage(getString(R.string.set_reminder_dialog_prompt))
                .setView(input).setPositiveButton(getString(R.string.button_ok), null).setNegativeButton(R.string.button_cancel, null)
                .create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int days = Integer.parseInt(input.getText().toString());
                        if (days > 0) {
                            pref.edit().putInt(Constants.KEY_PREF_REMINDER_DAYS, days).apply();
                            tvReminderTime.setText( Utility.formatDays(days));
                             Utility.setReminder(getContext(), mTripDate);

                            dlg.dismiss();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.reminder_toast), Toast.LENGTH_SHORT).show();
                            input.setText("");
                        }
                    }
                });
            }
        });
        dlg.show();
    }
    // Alarm
    View mAlarmView ;
    void setAlarmView(View alarmView) {
        mAlarmView = alarmView;
        mAlarmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUniqId == 0) {            //only the current trip can
                    onSetClockAlarm(view);
                } else {
                    Toast.makeText(getContext(), getString(R.string.alarm_toast), Toast.LENGTH_LONG).show();
                }
            }
        });
        enableAlarmView(mAlamSwitch.isChecked());
        mAlamSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                enableAlarmView(b);
            }
        });
    }
    void enableAlarmView(boolean isEnable) {
        mAlarmView.setEnabled(isEnable);
        mAlarmView.findViewById(R.id.disable_alarm).setVisibility(isEnable ? View.INVISIBLE : View.VISIBLE);
        mAlarmView.findViewById(R.id.ll_alarm_tvs).setVisibility(isEnable ? View.VISIBLE : View.INVISIBLE);
        int colorRid = isEnable ? 0xFF : 0x5F;
        ((AppCompatImageView) mAlarmView.findViewById(R.id.icon_alarm)).setImageAlpha(colorRid);
        if (mAlamSwitch.isChecked() != isEnable ) {
            mAlamSwitch.setChecked(isEnable);
        }
    }
    // see ChromeTebDemo and MyChromeTabDemo
    private void showInChromeBrowser(Uri uri) {
        int color1 = getResources().getColor(R.color.colorPrimary);
        int color2 = getResources().getColor(R.color.colorPrimaryDark);
        CustomTabsIntent tabIntent = CustomTabActivityHelper.createCustomTabsInstent(getContext(),
                color1, color2);
        CustomTabActivityHelper.openCustomTab(getActivity(), tabIntent, uri, new WebviewFallback());

    }
    public void onSetClockAlarm(View view) {
        long wakeupTime = getWakeupTime();
        if (Utility.isAlarmClockSet(getContext())) {    // alarmclock alrady set, show it
            Intent alarmIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            getContext().startActivity(alarmIntent);
        } else if (mCursor.moveToFirst()) {
            Calendar caltime = Calendar.getInstance();
            if (wakeupTime != -1) {
                caltime.setTimeInMillis(wakeupTime);
                int hour = caltime.get(Calendar.HOUR_OF_DAY);
                int minute = caltime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        onWakeupTimeSetListener, hour, minute, false);
                timePickerDialog.setTitle(getString(R.string.wakeup_time_dialog_title));
                timePickerDialog.show();
            }
        }
    }
    TimePickerDialog.OnTimeSetListener onWakeupTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar caltime = Calendar.getInstance();
            if (mTripDate != -1) {
                caltime.setTimeInMillis(mTripDate);
            }
            caltime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            caltime.set(Calendar.MINUTE, minute);
            caltime.set(Calendar.SECOND, 0);
            long wakeupTime =  caltime.getTimeInMillis();
            Utility.setWakeupTime(getContext(), wakeupTime);
            tvAlarmTime.setText(Utility.formatTimeOnly(wakeupTime));
        }
    };
    public void onCarpooling(View view) {
        if (mCursor.moveToFirst()) {
            Intent intent = new Intent(getContext(), CarpoolActivity.class);
            intent.putExtra(Constants.DBREC_ID_KEY, mTripId);
            getContext().startActivity(intent);
//        } else {
//            Log.v(LOG_TAG, "onCarpooling: no data avialable");
        }
    }
}
