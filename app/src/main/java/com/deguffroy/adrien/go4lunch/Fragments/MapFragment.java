package com.deguffroy.adrien.go4lunch.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Activity.PlaceDetailActivity;
import com.deguffroy.adrien.go4lunch.Api.RestaurantsHelper;
import com.deguffroy.adrien.go4lunch.BuildConfig;
import com.deguffroy.adrien.go4lunch.Activity.MainActivity;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends BaseFragment implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    @BindView(R.id.mapView) MapView mMapView;

    private static final int PERMS_FINE_COARSE_LOCATION = 100;
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String API_KEY = BuildConfig.google_maps_api_key;
    public static final String SEARCH_TYPE = "restaurant";

    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Disposable disposable;

    private CommunicationViewModel mViewModel;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(CommunicationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);

        setHasOptionsMenu(true);
        mViewModel.currentUserPosition.observe(this, latLng -> {
            executeHttpRequestWithRetrofit();
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        this.configureMapView();
        this.configureGoogleApiClient();
        this.configureLocationRequest();
        this.configureLocationCallBack();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);

        MenuItem item = menu.findItem(R.id.menu_search);
        SearchView searchView = new SearchView(((MainActivity) getContext()).getSupportActionBar().getThemedContext());
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setActionView(searchView);
        searchView.setQueryHint(getResources().getString(R.string.toolbar_search_hint));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(((MainActivity) getContext()).getComponentName()));

        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2 ){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(query,mViewModel.getCurrentUserPositionFormatted(),mViewModel.getCurrentUserRadius(),API_KEY).subscribeWith(createObserver());
                }else{
                    Toast.makeText(getContext(), getResources().getString(R.string.search_too_short), Toast.LENGTH_LONG).show();
                }
                return true;

            }
            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 2){
                    disposable = PlacesStreams.streamFetchAutoCompleteInfo(query,mViewModel.getCurrentUserPositionFormatted(),mViewModel.getCurrentUserRadius(),API_KEY).subscribeWith(createObserver());
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect(); }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewModel.currentUserPosition.removeObservers(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        this.disposeWhenDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                handleNewLocation(location);
                            } else {
                                if (EasyPermissions.hasPermissions(getContext(), perms)) {
                                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                                }

                            }
                        }
                    });
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_perm_access),
                    PERMS_FINE_COARSE_LOCATION, perms);
        }
    }

    private void handleNewLocation(Location location) {
        Log.e(TAG, "handleNewLocation: " );
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        this.mViewModel.updateCurrentUserPosition(new LatLng(currentLatitude, currentLongitude));

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(this.mViewModel.getCurrentUserPosition()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(this.mViewModel.getCurrentUserPosition(), mViewModel.getCurrentUserZoom()));
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private <T> void updateUI( T results){
        googleMap.clear();
        if(results instanceof MapPlacesInfo){
            MapPlacesInfo result = ((MapPlacesInfo) results);
            Log.e(TAG, "updateUI: " + result .getResults().size());
            if (result.getResults().size() > 0){
                for (int i = 0; i < result.getResults().size(); i++) {
                    int CurrentObject = i;
                    RestaurantsHelper.getTodayBooking(result.getResults().get(CurrentObject).getPlaceId(), getTodayDate()).addOnCompleteListener(restaurantTask -> {
                        if (restaurantTask.isSuccessful()) {
                            Double lat = result.getResults().get(CurrentObject).getGeometry().getLocation().getLat();
                            Double lng = result.getResults().get(CurrentObject).getGeometry().getLocation().getLng();
                            String title = result.getResults().get(CurrentObject).getName();

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(lat, lng));
                            markerOptions.title(title);
                            if (restaurantTask.getResult().isEmpty()) { // If there is no booking for today
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_unbook_24));
                            } else { // If there is booking for today
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_booked_24));
                            }
                            Marker marker = googleMap.addMarker(markerOptions);
                            marker.setTag(result.getResults().get(CurrentObject).getPlaceId());
                        }
                    });
                }
            }else{
                Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_SHORT).show();
            }
        }else if(results instanceof ArrayList){
            Log.e(TAG, "updateUI: SEARCH_NUMBER : " + ((ArrayList)results).size() );
            if (((ArrayList)results).size() > 0){
                for(Object result : ((ArrayList)results)){
                    PlaceDetailsResults detail = ((PlaceDetailsResults) result);
                    RestaurantsHelper.getTodayBooking(detail.getPlaceId(), getTodayDate()).addOnCompleteListener(restaurantTask -> {
                        if (restaurantTask.isSuccessful()) {
                            Double lat = detail.getGeometry().getLocation().getLat();
                            Double lng = detail.getGeometry().getLocation().getLng();
                            String title = detail.getName();

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(lat, lng));
                            markerOptions.title(title);
                            if (restaurantTask.getResult().isEmpty()) { // If there is no booking for today
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_unbook_24));
                            } else { // If there is booking for today
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_booked_24));
                            }
                            Marker marker = googleMap.addMarker(markerOptions);
                            marker.setTag(detail.getPlaceId());
                        }
                    });
                }
            }else{
                Toast.makeText(getContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_LONG).show();
            }
        }
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private void executeHttpRequestWithRetrofit(){
        String location = mViewModel.getCurrentUserPositionFormatted();
        Log.e(TAG, "Location : "+location );
        this.disposable = PlacesStreams.streamFetchNearbyPlaces(location,mViewModel.getCurrentUserRadius(),SEARCH_TYPE,API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver<T> createObserver(){
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                updateUI(t);
            }
            @Override
            public void onError(Throwable e) {handleError(e);}
            @Override
            public void onComplete() { }
        };
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private void configureMapView() {
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
                    //Request location updates:
                    googleMap.setMyLocationEnabled(true);
                }
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).
                        getParent()).findViewById(Integer.parseInt("2"));

                // and next place it, for example, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                // position on right bottom
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                rlp.setMargins(0, 0, 30, 30);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                googleMap.setOnMarkerClickListener(MapFragment.this::onClickMarker);
            }
        });
    }

    private void configureLocationCallBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    handleNewLocation(location);
                }
            }
        };
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void configureLocationRequest(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(100 * 1000)        // 100 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds
    }

    private void configureGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .enableAutoManage(getActivity(), this)
                .build();
    }

    public boolean checkLocationPermission() {
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {googleMap.setMyLocationEnabled(true);}
    }

    // -----------------
    // ACTION
    // -----------------

    private boolean onClickMarker(Marker marker){
        if (marker.getTag() != null){
            Log.e(TAG, "onClickMarker: " + marker.getTag() );
            Intent intent = new Intent(getActivity(),PlaceDetailActivity.class);
            intent.putExtra("PlaceDetailResult", marker.getTag().toString());
            startActivity(intent);
            return true;
        }else{
            Log.e(TAG, "onClickMarker: ERROR NO TAG" );
            return false;
        }
    }
}