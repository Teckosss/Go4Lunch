package com.deguffroy.adrien.go4lunch.Activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.deguffroy.adrien.go4lunch.Api.UserHelper;
import com.deguffroy.adrien.go4lunch.R;
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
    @BindView(R.id.settings_zoom_edit) EditText mZoomEdit;
    @BindView(R.id.settings_radius_edit) EditText mRadiusEdit;

    private NotificationHelper mNotificationHelper;

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
        this.setOnClickListener();
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
                    mZoomEdit.setText(documentSnapshot.getData().get("defaultZoom").toString());
                    mRadiusEdit.setText(documentSnapshot.getData().get("searchRadius").toString());
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

    private void setOnClickListener(){
        mButtonSave.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> { });
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
        int zoom = Integer.parseInt(mZoomEdit.getText().toString());
        int radius = Integer.parseInt(mRadiusEdit.getText().toString());
        if (mSwitch.isChecked()){
            mNotificationHelper.scheduleRepeatingNotification();
        }else{
            mNotificationHelper.cancelAlarmRTC();
        }
        UserHelper.updateUserSettings(getCurrentUser().getUid(),zoom,mSwitch.isChecked(),radius).addOnSuccessListener(
                updateTask -> Toast.makeText(this, "Document successfully updated!", Toast.LENGTH_SHORT).show()
        );
    }

}
