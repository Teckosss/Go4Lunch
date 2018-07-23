package com.deguffroy.adrien.go4lunch.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deguffroy.adrien.go4lunch.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatesFragment extends Fragment {


    public MatesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mates, container, false);
    }

}
