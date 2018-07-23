package com.deguffroy.adrien.go4lunch.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.MapPlacesInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.Result;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.DividerItemDecoration;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.deguffroy.adrien.go4lunch.Views.RestaurantAdapter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment {

    @BindView(R.id.list_recycler_view) RecyclerView mRecyclerView;

    private Disposable disposable;
    private List<Result> mResults;
    private RestaurantAdapter adapter;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);

        this.configureRecyclerView();
        this.executeHttpRequestWithRetrofit();
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
        this.adapter = new RestaurantAdapter(this.mResults);
        this.mRecyclerView.setAdapter(this.adapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private void executeHttpRequestWithRetrofit(){
        this.disposable = PlacesStreams.streamFetchNearbyPlaces(MapFragment.location, MapFragment.SEARCH_RADIUS, MapFragment.SEARCH_TYPE,MapFragment.API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver<T> createObserver(){
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof MapPlacesInfo){
                    updateUI(((MapPlacesInfo) t));
                }
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int statusCode = httpException.code();
            Log.e("HttpException", "Error code : " + statusCode);
            Toast.makeText(getContext(), "HttpException, Error code : " + statusCode, Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof SocketTimeoutException) {
            Log.e("SocketTimeoutException", "Timeout from retrofit");
            Toast.makeText(getContext(), "Request timeout, please check your internet connection", Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof IOException) {
            Log.e("IOException", "Error");
            Toast.makeText(getContext(), "An error occurred, please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Generic handleError", "Error");
            Toast.makeText(getContext(), "An error occurred, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(MapPlacesInfo result){
        mResults.clear();
        mResults.addAll(result.getResults());
        adapter.notifyDataSetChanged();
    }
}