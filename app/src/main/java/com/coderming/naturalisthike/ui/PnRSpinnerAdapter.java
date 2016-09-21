package com.coderming.naturalisthike.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.coderming.naturalisthike.R;
import com.coderming.naturalisthike.model.BaseAddress;

import java.util.List;

/**
 * Created by linna on 9/18/2016.
 */
public class PnRSpinnerAdapter extends ArrayAdapter<BaseAddress>{

    List<BaseAddress> mList;
    TextView mTextView;
    int mTripPos;
    public PnRSpinnerAdapter(Context context, int resource, List<BaseAddress> list, int tripPos) {
        super(context, resource);
        mList = list;
        mTripPos = tripPos;
    }

    @Override
    public int getCount() {
        return mList.size() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.spinner_item, parent, false);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.spinner_item);
        String str;
        if (position == mTripPos) {
            str = "* " + mList.get(position).getName();
        } else {
            str = mList.get(position).getName();
        }
        tv.setText(str);
        //        } else {
//            Log.v("SpinnerAdapter","getView wrong view="+ convertView.getClass().getSimpleName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.spinner_item, parent, false);
        }
        if (convertView instanceof TextView) {
            TextView tv = (TextView) convertView;
            tv.setText(mList.get(position).getName());
            tv.setTextColor(Color.BLACK);
//        } else {
//            Log.v("SpinnerAdapter","getView wrong view="+ convertView.getClass().getSimpleName());
        }
        return convertView;
    }
}
