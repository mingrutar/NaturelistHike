package com.coderming.naturalisthike.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.data.TripCheckListDataHelper;
import com.coderming.naturalisthike.data.TripContract.CheckListSelectionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {
    private static final String LOG_TAG = ChecklistActivity.class.getSimpleName();

    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_IS_OPTIONAL = 2;
    public static final int COL_TYPE = 3;
    public static final int COL_TRIP_ID = 4;
    public static final int COL_IS_ALWAYS_CHECK = 5;
    public static final int COL_IS_CHECKED = 6;

    public class ChecklistItem {
        long recId;
        String name;
        boolean isOptional;
        int type;
        long tripId;
        boolean isAlwaysOn;
        boolean isChecked;
        int load(Cursor cursor) {
            type = cursor.getInt(COL_TYPE);
            if (type == CheckListSelectionType.Leader.getValue()) {
                tripId = cursor.getLong(COL_TRIP_ID);
                if (tripId != mTripId) {
                    return -1;
                }
            }
            recId = cursor.getLong(COL_ID);
            name = cursor.getString(COL_NAME);
            isOptional = (cursor.getInt(COL_IS_OPTIONAL) == 1);
            isAlwaysOn = (cursor.getInt(COL_IS_ALWAYS_CHECK) == 1);
            isChecked = (cursor.getInt(COL_IS_CHECKED) == 1);
            return type - 1;
        }
    }

    ExpandableListView mExpListView;
    ChecklistAdapter mListAdapter;
    List<String> mGroupTitles;
    HashMap<String, List<ChecklistItem>> mListDataChild;
    long mTripId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);  //show back button

        String strTripDBId = getIntent().getData().getLastPathSegment();
        if (strTripDBId != null) {
            mTripId = Long.parseLong( strTripDBId );
        }
        if (mTripId == -1) {
            Log.e(LOG_TAG, "onCreate, invalid trip db id");
            finish();
        }
        mExpListView = (ExpandableListView) findViewById(R.id.checklist_ExpListView);

        mGroupTitles = new ArrayList<>();
        mListDataChild = new HashMap<> ();
        for (int rid :new int[] {R.string.checklist_club,
                R.string.checklist_leader, R.string.checklist_my} ){
            String str = getString(rid);
            mGroupTitles.add(str);
            mListDataChild.put(str, new ArrayList<ChecklistItem>());
        }

        mListAdapter = new ChecklistAdapter();
        mExpListView.setAdapter(mListAdapter);
        getDataFromDB();
    }
    void getDataFromDB() {
        Cursor cursor = TripCheckListDataHelper.getAllChecklistItem(this, mTripId);
        ChecklistItem item;
        if ((cursor != null) && cursor.moveToFirst()) {
            do {
                item = new ChecklistItem();
                int group = item.load(cursor);                // 1-based
                if (group != -1) {
                    String groupName = this.mGroupTitles.get(group);
                    mListDataChild.get(groupName).add(item);
                }
            } while (cursor.moveToNext());
            cursor.close();
            String str;
            for (int i = 0; i < mGroupTitles.size(); i++) {
                str = mGroupTitles.get(i);
                if (mListDataChild.get(str).size() > 0) {
                    mExpListView.expandGroup(i);
                }
            }
            mListAdapter.notifyDataSetChanged();
        }
    }
    class ChecklistAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() {
            return mGroupTitles.size();
        }

        @Override
        public int getChildrenCount(int pos) {
            String key = mGroupTitles.get(pos);
            if (mListDataChild.containsKey(key)) {
                return mListDataChild.get(key).size();
            } else {
                return 0;
            }
        }
        @Override
        public Object getGroup(int pos) {
            return ((pos < mGroupTitles.size()) && (pos >= 0)) ?
                mGroupTitles.get(pos) : null ;
        }
        @Override
        public Object getChild(int gpos, int cpos) {
            if  ((gpos < mGroupTitles.size()) && (gpos >= 0)){
                String key = mGroupTitles.get(gpos);
                if (mListDataChild.containsKey(key)) {
                    List<ChecklistItem> list =  mListDataChild.get(key);
                    if ((cpos >= 0) && (cpos < list.size())) {
                        return list.get(cpos);
                    }
                }
            }
            return null;
        }
        @Override
        public long getGroupId(int pos) {
            return pos;
        }
        @Override
        public long getChildId(int gpos, int cpos) {
            return cpos;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }
        @Override
        public View getGroupView(int gpos, boolean b, View convertView, ViewGroup viewGroup) {
            String headerTitle = (String) getGroup(gpos);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) ChecklistActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.checklist_group, null);
            }
            AppCompatImageView imageView = (AppCompatImageView) convertView.findViewById(R.id.iv_header);
            if (gpos == 2) {
                imageView.setImageResource(R.drawable.ic_cl_me_24dp);
            } else if (gpos == 1) {
                imageView.setImageResource(R.drawable.ic_cl_leader_24dp);
            } else if (gpos == 0) {
                imageView.setImageResource(R.drawable.ic_cl_club_24dp);
            }
            TextView headerText = (TextView) convertView.findViewById(R.id.tv_header);
            headerText.setText(headerTitle);

            return convertView;
        }
        @Override
        public View getChildView(final int gpos, final int cpos, boolean b, View convertView, final ViewGroup viewGroup) {
            final ChecklistItem item = (ChecklistItem) getChild(gpos, cpos);
            if (item == null) {
                Log.w(LOG_TAG, "getChildView: invalid gpos or cpos");
                return convertView;
            }
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater)
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.checklist_item, null);
            }
            AppCompatCheckBox cb_item = (AppCompatCheckBox) convertView.findViewById(R.id.cb_item);
            cb_item.setText(item.name );

            SwitchCompat mRememberSwitch = (SwitchCompat) convertView.findViewById(R.id.reminder_switch);
            mRememberSwitch.setChecked(item.isAlwaysOn);
            if (item.isAlwaysOn) {
                cb_item.setChecked(item.isChecked );
            }
            AppCompatImageView mDelete = (AppCompatImageView) convertView.findViewById(R.id.remove);
            // add listeners
            cb_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    CheckListSelectionType type = CheckListSelectionType.getType(gpos+1);
                    TripCheckListDataHelper.setCheckState(ChecklistActivity.this, item.recId, type, isChecked);
                }
            });
            mRememberSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    CheckListSelectionType type = CheckListSelectionType.getType(gpos+1);
                    TripCheckListDataHelper.setAlwaysCheckState(ChecklistActivity.this, item.recId, type, isChecked);
                }
            });
            if (gpos == 2) {                // personal
                mDelete.setVisibility(View.VISIBLE);
                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                     int ret =TripCheckListDataHelper.deleteChecklist(ChecklistActivity.this, item.recId );
                     if (ret > 0) {
                         viewGroup.removeViewAt(cpos);
                         notifyDataSetChanged();
                     }
                    }
                });
            } else {
                mDelete.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
        @Override
        public boolean isChildSelectable(int gpos, int cpos) {
            return true;
        }
    }
}
