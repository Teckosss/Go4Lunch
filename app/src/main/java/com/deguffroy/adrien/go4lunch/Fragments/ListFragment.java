package com.deguffroy.adrien.go4lunch.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Activity.MainActivity;
import com.deguffroy.adrien.go4lunch.Activity.PlaceDetailActivity;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.DividerItemDecoration;
import com.deguffroy.adrien.go4lunch.Utils.ItemClickSupport;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.deguffroy.adrien.go4lunch.ViewModels.ListViewModel;
import com.deguffroy.adrien.go4lunch.Views.RestaurantAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    @BindView(R.id.list_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.list_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private Disposable disposable;
    private List<PlaceDetailsResults> mResults;
    private RestaurantAdapter adapter;

    private CommunicationViewModel mCommunicationViewModel;
    private ListViewModel mViewModel;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        mViewModel = ViewModelProviders.of(this).get(ListViewModel.class);
        mViewModel.isLoading.observe(this, mSwipeRefreshLayout::setRefreshing);
        mViewModel.places.observe(this, this::updateUI);
        mViewModel.error.observe(this, this::handleError);
        mSwipeRefreshLayout.setOnRefreshListener(this::executeHttpRequestWithRetrofit);

        mCommunicationViewModel = ViewModelProviders.of(getActivity()).get(CommunicationViewModel.class);
        mCommunicationViewModel.currentUserPosition.observe(this, new Observer<LatLng>() {
            @Override
            public void onChanged(@Nullable LatLng latLng) {
               configureRecyclerView();
               configureOnClickRecyclerView();
               executeHttpRequestWithRetrofit();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    // Configure RecyclerView, Adapter, LayoutManager & glue it together
    private void configureRecyclerView(){
        this.mResults = new ArrayList<>();
        this.adapter = new RestaurantAdapter(this.mResults, mCommunicationViewModel.getCurrentUserPositionFormatted());
        this.mRecyclerView.setAdapter(this.adapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    // -----------------
    // ACTION
    // -----------------

    // 1 - Configure item click on RecyclerView
    private void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.fragment_list_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {

                        PlaceDetailsResults result = adapter.getRestaurant(position);
                        Intent intent = new Intent(getActivity(),PlaceDetailActivity.class);
                        intent.putExtra("PlaceDetailResult", result.getPlaceId());
                        startActivity(intent);
                    }
                });
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private void executeHttpRequestWithRetrofit(){
        //this.disposable = PlacesStreams.streamFetchNearbyPlaces(mCommunicationViewModel.getCurrentUserPositionFormatted(), MainActivity.DEFAULT_SEARCH_RADIUS, MapFragment.SEARCH_TYPE,MapFragment.API_KEY).subscribeWith(createObserver());
        mViewModel.streamFetchPlaceInfo(mCommunicationViewModel.getCurrentUserPositionFormatted(), MainActivity.DEFAULT_SEARCH_RADIUS, MapFragment.SEARCH_TYPE,MapFragment.API_KEY);
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int statusCode = httpException.code();
            Log.e("HttpException", "Error code : " + statusCode);
            Toast.makeText(getContext(), getResources().getString(R.string.http_error_message,statusCode), Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof SocketTimeoutException) {
            Log.e("SocketTimeoutException", "Timeout from retrofit");
            Toast.makeText(getContext(), getResources().getString(R.string.timeout_error_message), Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof IOException) {
            Log.e("IOException", "Error");
            Toast.makeText(getContext(), getResources().getString(R.string.exception_error_message), Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Generic handleError", "Error");
            Toast.makeText(getContext(), getResources().getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(List<PlaceDetailsResults> results){
        mResults.clear();
        mResults.addAll(results);
        adapter.notifyDataSetChanged();
    }
}