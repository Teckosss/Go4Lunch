package com.deguffroy.adrien.go4lunch.Activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.HttpException;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
    }

    // --------------------
    // UTILS
    // --------------------

    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    protected String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }

    protected void handleError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int statusCode = httpException.code();
            Log.e("HttpException", "Error code : " + statusCode);
            Toast.makeText(this, getResources().getString(R.string.http_error_message,statusCode), Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof SocketTimeoutException) {
            Log.e("SocketTimeoutException", "Timeout from retrofit");
            Toast.makeText(this, getResources().getString(R.string.timeout_error_message), Toast.LENGTH_SHORT).show();
        } else if (throwable instanceof IOException) {
            Log.e("IOException", "Error");
            Toast.makeText(this, getResources().getString(R.string.exception_error_message), Toast.LENGTH_SHORT).show();
        } else {
            Log.e("Generic handleError", "Error");
            Toast.makeText(this, getResources().getString(R.string.generic_error_message), Toast.LENGTH_SHORT).show();
        }
    }
}
