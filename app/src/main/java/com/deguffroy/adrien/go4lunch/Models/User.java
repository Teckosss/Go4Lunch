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
    private String restaurantSelected;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture, @Nullable String restaurantSelected) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.restaurantSelected = restaurantSelected;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }
    @Nullable
    public String getRestaurantSelected(){ return restaurantSelected; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
    public void setRestaurantSelected(@Nullable String restaurantSelected) { this.restaurantSelected = restaurantSelected; }

}
