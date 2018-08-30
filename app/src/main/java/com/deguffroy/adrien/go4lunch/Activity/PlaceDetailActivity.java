package com.deguffroy.adrien.go4lunch.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Api.RestaurantsHelper;
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.DividerItemDecoration;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.deguffroy.adrien.go4lunch.Views.DetailAdapter;
import com.deguffroy.adrien.go4lunch.Views.MatesAdapter;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.BASE_URL;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_HEIGHT_LARGE;

public class PlaceDetailActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_address)TextView mRestaurantAddress;
    @BindView(R.id.restaurant_star_1)ImageView mRestaurantStar1;
    @BindView(R.id.restaurant_recycler_view)RecyclerView mRestaurantRecyclerView;
    @BindView(R.id.slider)SliderLayout mDemoSlider;
    @BindView(R.id.floatingActionButton) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.restaurant_item_call) Button mButtonCall;
    @BindView(R.id.restaurant_item_like) Button mButtonLike;
    @BindView(R.id.restaurant_item_website) Button mButtonWebsite;

    private Disposable mDisposable;
    private PlaceDetailsResults requestResult;

    private List<User> mDetailUsers;
    private DetailAdapter mDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);
        ButterKnife.bind(this);

        this.retrieveObject();
        this.setFloatingActionButtonOnClickListener();
        this.configureButtonClickListener();
        this.configureRecyclerView();
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

    // Configure RecyclerView, Adapter, LayoutManager & glue it together
    private void configureRecyclerView(){
        this.mDetailUsers = new ArrayList<>();
        this.mDetailAdapter = new DetailAdapter(this.mDetailUsers);
        this.mRestaurantRecyclerView.setAdapter(this.mDetailAdapter);
        this.mRestaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void retrieveObject(){
        String result = getIntent().getStringExtra("PlaceDetailResult");
        //Log.e("TAG", "retrieveObject: " + result );
        this.executeHttpRequestWithRetrofit(result);
    }

    private void setFloatingActionButtonOnClickListener(){
        mFloatingActionButton.setOnClickListener(view -> bookThisRestaurant());
    }

    private void configureButtonClickListener(){
        mButtonCall.setOnClickListener(this);
        mButtonLike.setOnClickListener(this);
        mButtonWebsite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.restaurant_item_call:
                if (requestResult.getFormattedPhoneNumber() != null){
                    Toast.makeText(this, "Phone number : " + requestResult.getFormattedPhoneNumber(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, getResources().getString(R.string.restaurant_detail_no_phone), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.restaurant_item_like:
                Toast.makeText(this, "Click on Like", Toast.LENGTH_SHORT).show();
                break;

            case R.id.restaurant_item_website:
                if (requestResult.getWebsite() != null){
                    Intent intent = new Intent(this,WebViewActivity.class);
                    intent.putExtra("Website", requestResult.getWebsite());
                    startActivity(intent);
                }else{
                    Toast.makeText(this, getResources().getString(R.string.restaurant_detail_no_website), Toast.LENGTH_SHORT).show();
                }
                break;
        }
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

    // -------------------
    // UPDATE UI
    // -------------------

    private void updateUI(PlaceDetailsInfo results){
        if (results != null){
            this.displaySlider(results);
            mRestaurantName.setText(results.getResult().getName());
            mRestaurantAddress.setText(results.getResult().getVicinity());
            mDetailUsers.clear();
            this.updateUIWithRecyclerView(results.getResult().getPlaceId());
        }
    }

    private void updateUIWithRecyclerView(String placeId){
        RestaurantsHelper.getTodayBooking(placeId, getTodayDate()).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                for (QueryDocumentSnapshot restaurant : restaurantTask.getResult()){
                    Log.e("TAG", "DETAIL_ACTIVITY | Restaurant : " + restaurant.getData() );
                    UserHelper.getUser(restaurant.getData().get("userId").toString()).addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful()){
                            Log.e("TAG", "DETAIL_ACTIVITY | User : " + userTask.getResult() );
                            String uid = userTask.getResult().getData().get("uid").toString();
                            String username = userTask.getResult().getData().get("username").toString();
                            String urlPicture = userTask.getResult().getData().get("urlPicture").toString();
                            User userToAdd = new User(uid,username,urlPicture, MainActivity.DEFAULT_SEARCH_RADIUS,MainActivity.DEFAULT_ZOOM,false);
                            mDetailUsers.add(userToAdd);
                        }
                        mDetailAdapter.notifyDataSetChanged();
                    });
                }
            }
        });
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
            String restaurantName = requestResult.getName();
            this.checkIfUserAlreadyBookedRestaurant(userId,restaurantId, restaurantName);
        }else{
            Log.e("TAG", "USER : DISCONNECTED" );
        }
    }

    // ---------------------------------
    // PROCESS TO BOOK A RESTAURANT
    // ---------------------------------

    private void checkIfUserAlreadyBookedRestaurant(String userId, String restaurantId, String restaurantName){
        RestaurantsHelper.getBooking(userId, getTodayDate()).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                if (restaurantTask.getResult().size() == 1){ // User already booked a restaurant today

                    for (QueryDocumentSnapshot restaurant : restaurantTask.getResult()) {
                       if (restaurant.getData().get("restaurantName").equals(restaurantName)){ // If booked restaurant is the same as restaurant we are trying to book

                           Toast.makeText(this, "You already booked this restaurant for today!", Toast.LENGTH_SHORT).show();

                       }else{ // If user is trying to book an other restaurant for today

                           this.manageBooking(userId, restaurantId, restaurantName,restaurant.getId(),false,true);
                           Toast.makeText(this, "You change your booked restaurant for today!", Toast.LENGTH_SHORT).show();
                       }
                    }

                }else{ // No restaurant booked for this user today

                    this.manageBooking(userId, restaurantId, restaurantName,null,true,false);
                    Toast.makeText(this, "You have booked a new restaurant for today!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void manageBooking(String userId, String restaurantId,String restaurantName,@Nullable String bookingId, boolean toCreate, boolean toUpdate){
        if(toUpdate){
            RestaurantsHelper.deleteBooking(bookingId);
            RestaurantsHelper.createBooking(this.getTodayDate(),userId,restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
        }else if(toCreate){
            RestaurantsHelper.createBooking(this.getTodayDate(),userId,restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
        }
    }

    private String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }
}
