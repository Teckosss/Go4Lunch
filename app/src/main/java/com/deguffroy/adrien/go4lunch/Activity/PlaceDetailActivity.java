package com.deguffroy.adrien.go4lunch.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Api.RestaurantsHelper;
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsInfo;
import com.deguffroy.adrien.go4lunch.Models.PlacesInfo.PlacesDetails.PlaceDetailsResults;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.PlacesStreams;
import com.deguffroy.adrien.go4lunch.Views.DetailAdapter;
import com.glide.slider.library.Animations.DescriptionAnimation;
import com.glide.slider.library.SliderLayout;
import com.glide.slider.library.SliderTypes.DefaultSliderView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.BASE_URL;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_HEIGHT_LARGE;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_RATING;
import static com.deguffroy.adrien.go4lunch.Views.RestaurantViewHolder.MAX_STAR;

public class PlaceDetailActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.restaurant_name)TextView mRestaurantName;
    @BindView(R.id.restaurant_address)TextView mRestaurantAddress;
    @BindView(R.id.restaurant_recycler_view)RecyclerView mRestaurantRecyclerView;
    @BindView(R.id.slider)SliderLayout mDemoSlider;
    @BindView(R.id.floatingActionButton) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.restaurant_item_call) Button mButtonCall;
    @BindView(R.id.restaurant_item_like) Button mButtonLike;
    @BindView(R.id.restaurant_item_website) Button mButtonWebsite;
    @BindView(R.id.item_ratingBar) RatingBar mRatingBar;

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

    private void checkIfUserLikeThisRestaurant(){
        RestaurantsHelper.getAllLikeByUserId(getCurrentUser().getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.e("TAG", "checkIfUserLikeThisRestaurant: " + task.getResult().getDocuments());
                if (task.getResult().isEmpty()){ // User don't like any restaurant
                    mButtonLike.setText(getResources().getString(R.string.restaurant_item_like));
                }else{
                    for (DocumentSnapshot restaurant : task.getResult()){
                        if (restaurant.getId().equals(requestResult.getPlaceId())){
                            mButtonLike.setText(getResources().getString(R.string.restaurant_item_dislike));
                            break;
                        } else{
                            mButtonLike.setText(getResources().getString(R.string.restaurant_item_like));
                        }
                    }
                }
            }
        });

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
        Log.e("TAG", "retrieveObject: " + result );
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
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+requestResult.getFormattedPhoneNumber()));
                    startActivity(intent);
                }else{
                    Toast.makeText(this, getResources().getString(R.string.restaurant_detail_no_phone), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.restaurant_item_like:
                if (mButtonLike.getText().equals(getResources().getString(R.string.restaurant_item_like))){
                    this.likeThisRestaurant();
                }else{
                    this.dislikeThisRestaurant();
                }
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
            if (getCurrentUser() != null){
                this.checkIfUserAlreadyBookedRestaurant(getCurrentUser().getUid(),requestResult.getPlaceId(),requestResult.getName(),false);
                this.checkIfUserLikeThisRestaurant();
            }else{
                mButtonLike.setText(R.string.restaurant_item_like);
                this.displayFAB((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
                Toast.makeText(this, getResources().getString(R.string.restaurant_error_retrieving_info), Toast.LENGTH_SHORT).show();
            }
            this.displaySlider(results);
            mRestaurantName.setText(results.getResult().getName());
            mRestaurantAddress.setText(results.getResult().getVicinity());
            this.displayRating(results);
            this.updateUIWithRecyclerView(results.getResult().getPlaceId());
        }
    }

    private void updateUIWithRecyclerView(String placeId){
        mDetailUsers.clear();
        RestaurantsHelper.getTodayBooking(placeId, getTodayDate()).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                if (restaurantTask.getResult().isEmpty()){
                    mDetailAdapter.notifyDataSetChanged();
                }else{
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
            if (listUrl.size() == 1){
                mDemoSlider.stopAutoCycle();
            }else{
                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                mDemoSlider.setDuration(4000);
            }

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

    private void likeThisRestaurant(){
        if (requestResult != null && getCurrentUser() != null){
            RestaurantsHelper.createLike(requestResult.getPlaceId(),getCurrentUser().getUid()).addOnCompleteListener(likeTask -> {
                if (likeTask.isSuccessful()) {
                    Toast.makeText(this, getResources().getString(R.string.restaurant_like_ok), Toast.LENGTH_SHORT).show();
                    mButtonLike.setText(getResources().getString(R.string.restaurant_item_dislike));
                }
            });
        }else{
            Toast.makeText(this, getResources().getString(R.string.restaurant_like_ko), Toast.LENGTH_SHORT).show();
        }
    }

    private void dislikeThisRestaurant(){
        if (requestResult != null && getCurrentUser() != null){
            RestaurantsHelper.deleteLike(requestResult.getPlaceId(), getCurrentUser().getUid());
            mButtonLike.setText(getResources().getString(R.string.restaurant_item_like));
            Toast.makeText(this, getResources().getString(R.string.restaurant_dislike_ok), Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, getResources().getString(R.string.restaurant_like_ko), Toast.LENGTH_SHORT).show();
        }
    }

    private void bookThisRestaurant(){
        if (this.getCurrentUser() != null){
            String userId = getCurrentUser().getUid();
            String restaurantId = requestResult.getPlaceId();
            String restaurantName = requestResult.getName();
            this.checkIfUserAlreadyBookedRestaurant(userId,restaurantId, restaurantName, true);
        }else{
            Log.e("TAG", "USER : DISCONNECTED" );
        }
    }

    // ---------------------------------
    // PROCESS TO BOOK A RESTAURANT
    // ---------------------------------

    private void checkIfUserAlreadyBookedRestaurant(String userId, String restaurantId, String restaurantName, Boolean tryingToBook){
        RestaurantsHelper.getBooking(userId, getTodayDate()).addOnCompleteListener(restaurantTask -> {
            if (restaurantTask.isSuccessful()){
                if (restaurantTask.getResult().size() == 1){ // User already booked a restaurant today

                    for (QueryDocumentSnapshot restaurant : restaurantTask.getResult()) {
                       if (restaurant.getData().get("restaurantName").equals(restaurantName)){ // If booked restaurant is the same as restaurant we are trying to book
                           this.displayFAB((R.drawable.baseline_clear_black_24),getResources().getColor(R.color.colorError));
                           if (tryingToBook){
                               this.manageBooking(userId, restaurantId, restaurantName,restaurant.getId(),false,false,true);
                               Toast.makeText(this, getResources().getString(R.string.restaurant_cancel_booking), Toast.LENGTH_SHORT).show();
                           }

                       }else{ // If user is trying to book an other restaurant for today
                           this.displayFAB((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
                           if (tryingToBook){
                               this.manageBooking(userId, restaurantId, restaurantName,restaurant.getId(),false,true,false);
                               Toast.makeText(this, getResources().getString(R.string.restaurant_change_booking), Toast.LENGTH_SHORT).show();
                           }
                       }
                    }

                }else{ // No restaurant booked for this user today
                    this.displayFAB((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
                    if (tryingToBook){
                        this.manageBooking(userId, restaurantId, restaurantName,null,true,false,false);
                        Toast.makeText(this, getResources().getString(R.string.restaurant_new_booking), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void manageBooking(String userId, String restaurantId,String restaurantName,@Nullable String bookingId, boolean toCreate, boolean toUpdate, boolean toDelete){
        if(toUpdate){
            RestaurantsHelper.deleteBooking(bookingId);
            RestaurantsHelper.createBooking(this.getTodayDate(),userId,restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
            this.displayFAB((R.drawable.baseline_clear_black_24),getResources().getColor(R.color.colorError));
        }else if(toCreate){
            RestaurantsHelper.createBooking(this.getTodayDate(),userId,restaurantId, restaurantName).addOnFailureListener(this.onFailureListener());
            this.displayFAB((R.drawable.baseline_clear_black_24),getResources().getColor(R.color.colorError));
        }else if(toDelete){
            RestaurantsHelper.deleteBooking(bookingId);
            this.displayFAB((R.drawable.baseline_check_circle_black_24),getResources().getColor(R.color.colorGreen));
        }

        updateUIWithRecyclerView(requestResult.getPlaceId());
    }

    private void displayFAB(int icon, int color){
        Drawable mDrawable = ContextCompat.getDrawable(getBaseContext(), icon).mutate();
        mDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        mFloatingActionButton.setImageDrawable(mDrawable);
    }

    private void displayRating(PlaceDetailsInfo results){
        if (results.getResult().getRating() != null){
            double googleRating = results.getResult().getRating();
            double rating = googleRating / MAX_RATING * MAX_STAR;
            this.mRatingBar.setRating((float)rating);
            this.mRatingBar.setVisibility(View.VISIBLE);
        }else{
            this.mRatingBar.setVisibility(View.GONE);
        }
    }
}
