package com.coderming.naturalisthike.ui;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coderming.naturalisthike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SetupLoginFragment extends DialogFragment {
    private static final String LOG_TAG = SetupLoginFragment.class.getSimpleName();


    public SetupLoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup_login, container, false);
    }

}
