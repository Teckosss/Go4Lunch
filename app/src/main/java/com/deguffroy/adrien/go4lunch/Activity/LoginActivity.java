package com.deguffroy.adrien.go4lunch.Activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;

import static com.deguffroy.adrien.go4lunch.Activity.MainActivity.DEFAULT_NOTIFICATION;
import static com.deguffroy.adrien.go4lunch.Activity.MainActivity.DEFAULT_SEARCH_RADIUS;
import static com.deguffroy.adrien.go4lunch.Activity.MainActivity.DEFAULT_ZOOM;

public class LoginActivity extends BaseActivity {

    // FOR DATA
    private static final int RC_SIGN_IN = 1000;

    private CommunicationViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        if (!this.isCurrentUserLogged()){
            this.startSignInActivity();
        }else{
            this.mViewModel.updateCurrentUserUID(getCurrentUser().getUid());
            this.launchMainActivity();
        }
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

    // --------------------
    // REST REQUEST
    // --------------------

    // Http request that create user in firestore
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){
            Log.e("TAG", "createUserInFirestore: LOGGED" );
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            this.mViewModel.updateCurrentUserUID(uid);
            UserHelper.createUser(uid, username, urlPicture, DEFAULT_SEARCH_RADIUS, DEFAULT_ZOOM, DEFAULT_NOTIFICATION).addOnFailureListener(this.onFailureListener());
        }else{
            Log.e("TAG", "createUserInFirestore: NOT LOGGED" );
        }
    }

    // --------------------
    // UTILS
    // --------------------

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                this.launchMainActivity();
            } else { // ERRORS
                if (response == null) {
                    Toast.makeText(this, getString(R.string.error_authentication_canceled), Toast.LENGTH_SHORT).show();
                    startSignInActivity();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, getString(R.string.error_unknown_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // --------------------
    // ACTION
    // --------------------

    private void launchMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
