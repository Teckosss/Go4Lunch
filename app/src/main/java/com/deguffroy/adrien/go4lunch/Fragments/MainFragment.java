package com.deguffroy.adrien.go4lunch.Fragments;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deguffroy.adrien.go4lunch.MainActivity;
import com.deguffroy.adrien.go4lunch.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    @BindView(R.id.mapView) MapView mMapView;

    public static final String IDENTIFIER = "Identifier";

    private int identifierID;
    private GoogleMap googleMap;
    ArrayList<LatLng> markerPoints;

    public static MainFragment newInstance(int Identifier) {
        MainFragment mainFragment = new MainFragment();

        Bundle args = new Bundle();
        args.putInt(IDENTIFIER, Identifier);
        mainFragment.setArguments(args);

        return mainFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        if (getIdentifier() == MainActivity.FRAGMENT_MAPVIEW){
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();
            this.configureMapView();
            Log.e("MainFragment", "onCreateView: " + getIdentifier() );
        }else if (getIdentifier() == MainActivity.FRAGMENT_LISTVIEW){
            Log.e("MainFragment", "onCreateView: " + getIdentifier() );
        }else if (getIdentifier() == MainActivity.FRAGMENT_MATES){
            Log.e("MainFragment", "onCreateView: " + getIdentifier() );
        }else{
            Log.e("MainFragment", "onCreateView: ERREUR" );
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getIdentifier() == MainActivity.FRAGMENT_MAPVIEW){
            mMapView.onResume();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (getIdentifier() == MainActivity.FRAGMENT_MAPVIEW){
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getIdentifier() == MainActivity.FRAGMENT_MAPVIEW){
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (getIdentifier() == MainActivity.FRAGMENT_MAPVIEW){
            mMapView.onLowMemory();
        }
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private int getIdentifier(){
        return identifierID = getArguments().getInt(IDENTIFIER, 0);
    }

    private void configureMapView(){
        try {
            MapsInitializer.initialize(getActivity().getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;


                if (checkLocationPermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        googleMap.setMyLocationEnabled(true);
                    }
                }


                markerPoints = new ArrayList<LatLng>();

                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);

            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("")
                        .setMessage("")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),new String[]
                                        {android.Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {


                        googleMap.setMyLocationEnabled(true);
                    }

                } else {



                }
                return;
            }

        }
    }
}
