package com.deguffroy.adrien.go4lunch.Activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.R;
import com.deguffroy.adrien.go4lunch.Utils.MinMaxFilters;
import com.deguffroy.adrien.go4lunch.Utils.Notifications.AlarmReceiver;
import com.deguffroy.adrien.go4lunch.Utils.Notifications.NotificationHelper;
import com.deguffroy.adrien.go4lunch.ViewModels.CommunicationViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.simple_toolbar) Toolbar mToolbar;
    @BindView(R.id.settings_switch) Switch mSwitch;
    @BindView(R.id.settings_save) Button mButtonSave;
    @BindView(R.id.settings_radius_edit_text) TextInputEditText mRadiusEditText;
    @BindView(R.id.settings_zoom_edit_text) TextInputEditText mZoomEditText;
    @BindView(R.id.settings_zoom_edit_layout) TextInputLayout mZoomEditLayout;
    @BindView(R.id.settings_radius_edit_layout) TextInputLayout mRadiusEditLayout;

    private NotificationHelper mNotificationHelper;

    private static final String ZOOM_MIN_VALUE = "6";
    private static final String ZOOM_MAX_VALUE = "18";
    private static final String RADIUS_MIN_VALUE = "150";
    private static final String RADIUS_MAX_VALUE = "10000";

    protected CommunicationViewModel mViewModel;

    public static final long INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this).get(CommunicationViewModel.class);

        this.configureToolbar();
        this.retrieveUserSettings();
        this.setListenerAndFilters();
        this.createNotificationHelper();
    }

    // -------------
    // CONFIGURATION
    // -------------

    private void configureToolbar(){
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void retrieveUserSettings(){
        UserHelper.getUsersCollection().document(getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("TAG", "Listen failed.", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    Log.e("TAG", "Current data: " + documentSnapshot.getData());

                    mZoomEditText.setText(documentSnapshot.getData().get("defaultZoom").toString());
                    mRadiusEditText.setText(documentSnapshot.getData().get("searchRadius").toString());

                    if (documentSnapshot.getData().get("notificationOn").equals(true)){
                        mSwitch.setChecked(true);
                        mNotificationHelper.scheduleRepeatingNotification();
                    }else{
                        mSwitch.setChecked(false);
                        mNotificationHelper.cancelAlarmRTC();
                    }
                    mViewModel.updateCurrentUserZoom(Integer.parseInt(documentSnapshot.getData().get("defaultZoom").toString()));
                    mViewModel.updateCurrentUserRadius(Integer.parseInt(documentSnapshot.getData().get("searchRadius").toString()));
                } else {
                    Log.e("TAG", "Current data: null");
                }
            }
        });
    }

    private void setListenerAndFilters(){
        mButtonSave.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> { });

        mZoomEditText.setFilters(new InputFilter[]{new MinMaxFilters(ZOOM_MIN_VALUE,ZOOM_MAX_VALUE)});
        mRadiusEditText.setFilters(new InputFilter[]{new MinMaxFilters(RADIUS_MIN_VALUE,RADIUS_MAX_VALUE)});
    }

    private void createNotificationHelper(){
        mNotificationHelper = new NotificationHelper(getBaseContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_save:
                this.saveSettings();
                break;
        }
    }

    private void saveSettings(){
        boolean error = false;
        int zoom = MainActivity.DEFAULT_ZOOM;
        int radius = MainActivity.DEFAULT_SEARCH_RADIUS;
        if (!(mZoomEditText.getText().toString().equals(""))){
            zoom = Integer.parseInt(mZoomEditText.getText().toString());
            if (zoom < Integer.parseInt(ZOOM_MIN_VALUE) || zoom > Integer.parseInt(ZOOM_MAX_VALUE)){
                mZoomEditLayout.setError(getResources().getString(R.string.settings_save_error_zoom,ZOOM_MIN_VALUE,ZOOM_MAX_VALUE));
                error = true;
            }else{
                mZoomEditLayout.setError(null);
            }
        }else{
            mZoomEditLayout.setError(getResources().getString(R.string.settings_save_error_zoom,ZOOM_MIN_VALUE,ZOOM_MAX_VALUE));
            error = true;
        }

        if (!(mRadiusEditText.getText().toString().equals(""))){
            radius = Integer.parseInt(mRadiusEditText.getText().toString());
            if (radius < Integer.parseInt(RADIUS_MIN_VALUE) || radius > Integer.parseInt(RADIUS_MAX_VALUE)){
                mRadiusEditLayout.setError(getResources().getString(R.string.settings_save_error_radius,RADIUS_MIN_VALUE,RADIUS_MAX_VALUE));
                error = true;
            }else{
                mRadiusEditLayout.setError(null);
            }
        }else{
            mRadiusEditLayout.setError(getResources().getString(R.string.settings_save_error_radius,RADIUS_MIN_VALUE,RADIUS_MAX_VALUE));
            error = true;
        }

        if (mSwitch.isChecked()){
            mNotificationHelper.scheduleRepeatingNotification();
        }else{
            mNotificationHelper.cancelAlarmRTC();
        }
        if (!(error)){
            UserHelper.updateUserSettings(getCurrentUser().getUid(),zoom,mSwitch.isChecked(),radius).addOnSuccessListener(
                    updateTask ->{
                        Log.e("SETTINGS_ACTIVITY", "saveSettings: DONE" );
                        Toast.makeText(this, getResources().getString(R.string.settings_save_ok), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
