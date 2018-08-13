package com.deguffroy.adrien.go4lunch.Activity;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.BASE_URL;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_HEIGHT_LARGE;

public class PlaceDetailActivity extends AppCompatActivity {

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_address)TextView mRestaurantAddress;
    @BindView(R.id.restaurant_main_picture)ImageView mRestaurantMainPicture;
    @BindView(R.id.restaurant_star_1)ImageView mRestaurantStar1;
    @BindView(R.id.restaurant_item_list)BottomNavigationView mRestaurantItemList;
    @BindView(R.id.restaurant_recycler_view)RecyclerView mRestaurantRecyclerView;
    @BindView(R.id.slider)SliderLayout mDemoSlider;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        ButterKnife.bind(this);

        this.retrieveObject();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeWhenDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDemoSlider.stopAutoCycle();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }

    private void retrieveObject(){
        String result = getIntent().getStringExtra("PlaceDetailResult");
        Log.e("TAG", "retrieveObject: " + result );
        this.executeHttpRequestWithRetrofit(result);
        //this.configureBottomView(result);
    }

    private void configureBottomView(PlaceDetailsResults result){
        mRestaurantItemList.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.call:
                                if (result.getFormattedPhoneNumber() != null){
                                    Toast.makeText(PlaceDetailActivity.this, result.getFormattedPhoneNumber(), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PlaceDetailActivity.this, R.string.restaurant_detail_no_phone, Toast.LENGTH_SHORT).show();
                                }

                                break;
                            case R.id.like:

                                break;
                            case R.id.website:
                                if (result.getWebsite() != null){
                                    Toast.makeText(PlaceDetailActivity.this, result.getWebsite(), Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(PlaceDetailActivity.this, R.string.restaurant_detail_no_website, Toast.LENGTH_SHORT).show();
                                }

                                break;
                        }
                        return true;
                    }
                });
    }

    // -------------------
    // HTTP (RxJAVA)
    // -------------------

    private void executeHttpRequestWithRetrofit(String placeId){
        this.mDisposable = PlacesStreams.streamSimpleFetchPlaceInfo(placeId,MapFragment.API_KEY).subscribeWith(createObserver());
    }

    private <T> DisposableObserver<T> createObserver(){
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T t) {
                if (t instanceof PlaceDetailsInfo) {
                    updateUI(((PlaceDetailsInfo) t));
                }else{
                    Log.e("TAG", "onNext: " + t.getClass() );
                }
            }
            @Override
            public void onError(Throwable e) {handleError(e);}
            @Override
            public void onComplete() {}
        };
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int statusCode = httpException.code();
            Log.e("HttpException", "Error code : " + statusCode);
            Toast.makeText(this, "HttpException, Error code : " + statusCode, Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof SocketTimeoutException) {
            Log.e("SocketTimeoutException", "Timeout from retrofit");
            Toast.makeText(this, "Request timeout, please check your internet connection", Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof IOException) {
            Log.e("IOException", "Error");
            Toast.makeText(this, "An error occurred, please check your internet connection", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Generic handleError", "Error");
            Toast.makeText(this, "An error occurred, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(PlaceDetailsInfo results){
       this.displaySlider(results);
        mRestaurantName.setText(results.getResult().getName());
        mRestaurantAddress.setText(results.getResult().getVicinity());
    }

    private void displaySlider(PlaceDetailsInfo results){
        if (results.getResult().getPhotos() != null){
            mRestaurantMainPicture.setVisibility(View.GONE);
            mDemoSlider.setVisibility(View.VISIBLE);
            ArrayList<String> listUrl = new ArrayList<>();
            for (int i =0; i < results.getResult().getPhotos().size();i++){
                String url = BASE_URL+"?maxheight="+MAX_HEIGHT_LARGE+"&photoreference="+results.getResult().getPhotos().get(i).getPhotoReference()+"&key="+ MapFragment.API_KEY;
                listUrl.add(url);
            }
            for (int i = 0; i < listUrl.size();i++){
                DefaultSliderView defaultSliderView = new DefaultSliderView(this);
                defaultSliderView
                        .image(listUrl.get(i))
                        .setBackgroundColor(Color.WHITE)
                        .setProgressBarVisible(true);
                mDemoSlider.addSlider(defaultSliderView);
            }
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.setDuration(4000);
        }else{
            mDemoSlider.setVisibility(View.GONE);
            mRestaurantMainPicture.setVisibility(View.VISIBLE);
            Glide.with(getBaseContext()).load(R.drawable.ic_no_image_available).apply(RequestOptions.centerCropTransform()).into(mRestaurantMainPicture);
        }
    }
}
