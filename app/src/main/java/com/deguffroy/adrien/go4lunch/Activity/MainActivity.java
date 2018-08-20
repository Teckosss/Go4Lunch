package com.deguffroy.adrien.go4lunch.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.Fragments.ListFragment;
import com.deguffroy.adrien.go4lunch.Fragments.MapFragment;
import com.deguffroy.adrien.go4lunch.Fragments.MatesFragment;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener{

    @BindView(R.id.activity_main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.bottom_navigation) BottomNavigationView mBottomNavigationView;
    @BindView(R.id.simple_toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_view) NavigationView mNavigationView;

    //FOR DATA
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;

    public static final int TITLE_HUNGRY = R.string.hungry;
    public static final int  TITLE_WORKMATES = R.string.available;

    //Identity each fragment with a number
    public static final int  FRAGMENT_MAPVIEW = 0;
    public static final int  FRAGMENT_LISTVIEW = 1;
    public static final int  FRAGMENT_MATES = 2;

    //Identity each activity with a number
    public static final int ACTIVITY_SETTINGS = 0;

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

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        if (!this.isCurrentUserLogged()){
            this.startSignInActivity();
        }else{
            this.mViewModel.updateCurrentUserUID(getCurrentUser().getUid());
        }

        this.updateUIWhenCreating();
        this.configureNavigationView();
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureBottomView();

        this.showFragment(FRAGMENT_MAPVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),      // GOOGLE
                                        new AuthUI.IdpConfig.FacebookBuilder().build()))         // FACEBOOK
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.meal)
                        .build(),
                RC_SIGN_IN);
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
                launchActivity(SettingsActivity.class);
                break;
        }
    }

    private void launchActivity(Class mClass){
        Intent intent = new Intent(this, mClass);
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

    // Configure Toolbar
    private void configureToolBar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(TITLE_HUNGRY);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu and add it to the Toolbar
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
                                getSupportActionBar().setTitle(TITLE_HUNGRY);
                                showFragment(FRAGMENT_MAPVIEW);
                                break;
                            case R.id.list:
                                getSupportActionBar().setTitle(TITLE_HUNGRY);
                                showFragment(FRAGMENT_LISTVIEW);
                                break;
                            case R.id.mates:
                                getSupportActionBar().setTitle(TITLE_WORKMATES);
                                showFragment(FRAGMENT_MATES);
                                break;
                        }
                        return true;
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
                        startSignInActivity();
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

    // Http request that create user in firestore
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            this.mViewModel.updateCurrentUserUID(uid);
            UserHelper.createUser(uid, username, urlPicture, null, DEFAULT_SEARCH_RADIUS, DEFAULT_ZOOM, DEFAULT_NOTIFICATION).addOnFailureListener(this.onFailureListener());
        }
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    // --------------------
    // UTILS
    // --------------------

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                this.updateUIWhenCreating();
            } else { // ERRORS
                if (response == null) {
                    Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
