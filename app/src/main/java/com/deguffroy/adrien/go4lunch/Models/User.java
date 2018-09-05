package com.deguffroy.adrien.go4lunch.Models;

import android.support.annotation.Nullable;

/**
 * Created by Adrien Deguffroy on 12/07/2018.
 */
public class User {
    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    @Nullable
    private int searchRadius;
    @Nullable
    private int defaultZoom;
    @Nullable
    private boolean isNotificationOn;

    public User() { }

    public User(String uid){
        this.uid = uid;
    }

    public User(String uid, String username, @Nullable String urlPicture, @Nullable int searchRadius, @Nullable int defaultZoom, @Nullable boolean isNotificationOn) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.searchRadius = searchRadius;
        this.defaultZoom = defaultZoom;
        this.isNotificationOn = isNotificationOn;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }

    @Nullable
    public int getSearchRadius() {
        return searchRadius;
    }

    @Nullable
    public int getDefaultZoom() {
        return defaultZoom;
    }

    @Nullable
    public boolean isNotificationOn() {
        return isNotificationOn;
    }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }

    public void setSearchRadius(@Nullable int searchRadius) {
        this.searchRadius = searchRadius;
    }

    public void setDefaultZoom(@Nullable int defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public void setNotificationOn(@Nullable boolean notificationOn) {
        isNotificationOn = notificationOn;
    }
}
