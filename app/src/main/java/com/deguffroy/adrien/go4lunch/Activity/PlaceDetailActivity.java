package com.deguffroy.adrien.go4lunch.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
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
import com.deguffroy.adrien.go4lunch.Api.RestaurantsHelper;
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.BASE_URL;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_HEIGHT_LARGE;

public class PlaceDetailActivity extends BaseActivity {

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_address)TextView mRestaurantAddress;
    @BindView(R.id.restaurant_star_1)ImageView mRestaurantStar1;
    @BindView(R.id.restaurant_recycler_view)RecyclerView mRestaurantRecyclerView;
    @BindView(R.id.slider)SliderLayout mDemoSlider;
    @BindView(R.id.floatingActionButton) FloatingActionButton mFloatingActionButton;

    private Disposable mDisposable;
    private PlaceDetailsResults requestResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        ButterKnife.bind(this);

        this.retrieveObject();
        this.setFloatingActionButtonOnClickListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposeWhenDestroy();
    }

    @Override
    protected void onStop() {
        mDemoSlider.stopAutoCycle();
        super.onStop();

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
    }

    private void setFloatingActionButtonOnClickListener(){
        mFloatingActionButton.setOnClickListener(view -> bookThisRestaurant());
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
                    requestResult = ((PlaceDetailsInfo) t).getResult();
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
            DefaultSliderView defaultSliderView = new DefaultSliderView(this);
            defaultSliderView
                    .image(R.drawable.ic_no_image_available)
                    .setBackgroundColor(Color.WHITE)
                    .setProgressBarVisible(true);
            mDemoSlider.addSlider(defaultSliderView);
            mDemoSlider.stopAutoCycle();

        }
    }

    // --------------------
    // REST REQUEST
    // --------------------

    private void bookThisRestaurant(){
        if (this.getCurrentUser() != null){
            String userId = getCurrentUser().getUid();
            String restaurantId = requestResult.getPlaceId();

            UserHelper.getUsersCollection().get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        if (document.getData().get("restaurantSelected") != null){ // If there is a restaurant already booked
                            String restaurantSelected = document.getData().get("restaurantSelected").toString();
                            if (restaurantSelected.equals(requestResult.getName())){ // If booked restaurant is the same as restaurant we are trying to book

                                // Check date, if different create new booking and delete the old one OR update booking with new date

                            }else{

                                // Create new booking and delete the old one OR update booking with new name and date. Update user

                            }

                        }else{ // No restaurant already booked

                            this.createBookingAndUpdateUser(userId, restaurantId);
                        }
                    }
                }else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                }
            });

        }else{
            Log.e("TAG", "USER : DISCONNECTED" );
        }
    }

    private void createBookingAndUpdateUser(String userId, String restaurantId){
        RestaurantsHelper.createBooking(this.getTodayDate(),userId,restaurantId).addOnFailureListener(this.onFailureListener());
        UserHelper.updateBookedRestaurant(requestResult.getName(),userId);
    }

    private String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }
}
