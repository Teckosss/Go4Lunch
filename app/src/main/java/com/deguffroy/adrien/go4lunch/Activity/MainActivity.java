package com.deguffroy.adrien.go4lunch.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.deguffroy.adrien.go4lunch.Api.RestaurantsHelper;
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.Fragments.ListFragment;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Fragments.MatesFragment;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.activity_main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.bottom_navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.simple_toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_view) NavigationView mNavigationView;

    //FOR DATA
    private static final int SIGN_OUT_TASK = 10;

    public static final int TITLE_HUNGRY = R.string.hungry;
    public static final int  TITLE_WORKMATES = R.string.available;
    public static final int  TITLE_CHAT = R.string.chat;

    //Identity each fragment with a number
    public static final int  FRAGMENT_MAPVIEW = 0;
    public static final int  FRAGMENT_LISTVIEW = 1;
    public static final int  FRAGMENT_MATES = 2;

    //Identity each activity with a number
    public static final int ACTIVITY_SETTINGS = 0;
    public static final int ACTIVITY_CHAT = 1 ;
    public static final int ACTIVITY_PLACE_DETAIL = 2 ;
    public static final int ACTIVITY_LOGIN = 3 ;

    //Default data to create user
    public static final int DEFAULT_ZOOM = 13;
    public static final int DEFAULT_SEARCH_RADIUS = 1000;
    public static final boolean DEFAULT_NOTIFICATION = false;

    protected CommunicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        this.updateUIWhenCreating();
        this.configureNavigationView();
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureBottomView();
        this.retrieveCurrentUser();

        this.showFragment(FRAGMENT_MAPVIEW);
    }

    // ---------------------
    // FRAGMENTS
    // ---------------------

    private void showFragment(int fragmentIdentifier){
        Fragment newFragment = new Fragment();
        switch (fragmentIdentifier){
            case MainActivity.FRAGMENT_MAPVIEW:
                newFragment = MapFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_MAPVIEW );
                break;
            case MainActivity.FRAGMENT_LISTVIEW:
                newFragment = ListFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_LISTVIEW );
                break;
            case MainActivity.FRAGMENT_MATES:
                newFragment = MatesFragment.newInstance();
                Log.e("Show Fragment", ""+MainActivity.FRAGMENT_MATES );
                break;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.fragment_view, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    // ---------------------
    // ACTIVITY
    // ---------------------

    private void showActivity(int activityIdentifier){
        switch (activityIdentifier){
            case ACTIVITY_SETTINGS:
                launchActivity(SettingsActivity.class,null);
                break;
            case ACTIVITY_CHAT:
                launchActivity(ChatActivity.class,null);
                break;
            case ACTIVITY_PLACE_DETAIL:
                RestaurantsHelper.getBooking(getCurrentUser().getUid(),getTodayDate()).addOnCompleteListener(bookingTask -> {
                   if (bookingTask.isSuccessful()){
                       if (bookingTask.getResult().isEmpty()){
                           Toast.makeText(this, getResources().getString(R.string.drawer_no_restaurant_booked), Toast.LENGTH_SHORT).show();
                       }else{
                           Map<String,Object> extra = new HashMap<>();
                           for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                               extra.put("PlaceDetailResult",booking.getData().get("restaurantId"));
                           }
                           launchActivity(PlaceDetailActivity.class,extra);
                       }

                   }
                });
                break;
            case ACTIVITY_LOGIN:
                launchActivity(LoginActivity.class,null);
                break;
        }
    }

    private void launchActivity(Class mClass, Map<String,Object> info){
        Intent intent = new Intent(this, mClass);
        if (info != null){
            for (Object key : info.keySet()) {
                String mKey = (String)key;
                String value = (String) info.get(key);
                intent.putExtra(mKey, value);
            }
        }
        startActivity(intent);
    }

    // ---------------------
    // ACTIONS
    // ---------------------

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle Navigation Item Click
        int id = item.getItemId();

        switch (id){
            case R.id.drawer_lunch :
                showActivity(ACTIVITY_PLACE_DETAIL);
                break;
            case R.id.drawer_settings:
               showActivity(ACTIVITY_SETTINGS);
                break;
            case R.id.drawer_logout:
                this.signOutUserFromFirebase();
                break;
            default:
                break;
        }

        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------

    // Configure Drawer Layout
    private void configureDrawerLayout(){
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    // Configure NavigationView
    private void configureNavigationView(){
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    // Configure Toolbar
    private void configureToolBar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TITLE_HUNGRY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public void onBackPressed() {
        // Handle back click to close menu
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void configureBottomView(){
        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.map:
                                //getSupportActionBar().setTitle(TITLE_HUNGRY);
                                showFragment(FRAGMENT_MAPVIEW);
                                break;
                            case R.id.list:
                                //getSupportActionBar().setTitle(TITLE_HUNGRY);
                                showFragment(FRAGMENT_LISTVIEW);
                                break;
                            case R.id.mates:
                                //getSupportActionBar().setTitle(TITLE_WORKMATES);
                                showFragment(FRAGMENT_MATES);
                                break;
                            case R.id.chat:
                                //getSupportActionBar().setTitle(TITLE_CHAT);
                                showActivity(ACTIVITY_CHAT);
                                break;
                        }
                        return true;
                    }
                });
    }

    private void retrieveCurrentUser(){
        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);
        this.mViewModel.updateCurrentUserUID(getCurrentUser().getUid());
        UserHelper.getUsersCollection().document(getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("MAIN_ACTIVITY", "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.e("MAIN_ACTIVITY", "Current data: " + documentSnapshot.getData());
                    mViewModel.updateCurrentUserZoom(Integer.parseInt(documentSnapshot.getData().get("defaultZoom").toString()));
                    mViewModel.updateCurrentUserRadius(Integer.parseInt(documentSnapshot.getData().get("searchRadius").toString()));
                } else {
                    Log.e("MAIN_ACTIVITY", "Current data: null");
                }
            }
        });
    }

    // --------------------
    // UI
    // --------------------

    // 1 - Update UI when activity is creating
    private void updateUIWhenCreating(){

        if (this.getCurrentUser() != null){
            View headerContainer = mNavigationView.getHeaderView(0); // This returns the container layout in nav_drawer_header.xml (e.g., your RelativeLayout or LinearLayout)
            ImageView mImageView = headerContainer.findViewById(R.id.drawer_image);
            ImageView mImageView_bk = headerContainer.findViewById(R.id.drawer_image_bk);
            TextView mNameText = headerContainer.findViewById(R.id.drawer_name);
            TextView mEmailText = headerContainer.findViewById(R.id.drawer_email);

            Glide.with(this)
                    .load(R.drawable.lunch)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
                    .into(mImageView_bk);

            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(mImageView);
            }

            //Get email from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String name = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            mEmailText.setText(email);
            mNameText.setText(name);
        }
    }

    // Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                        showActivity(ACTIVITY_LOGIN);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    // --------------------
    // REST REQUEST
    // --------------------

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }
}
