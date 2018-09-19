package com.deguffroy.adrien.go4lunch.Api;

import android.util.Log;

import com.deguffroy.adrien.go4lunch.Models.Booking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adrien Deguffroy on 16/08/2018.
 */
public class RestaurantsHelper {
    private static final String COLLECTION_NAME = "booking";
    private static final String COLLECTION_LIKED_NAME = "restaurantLiked";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getBookingCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static CollectionReference getLikedCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_LIKED_NAME);
    }

    // --- CREATE ---

    public static Task<DocumentReference> createBooking(String bookingDate, String userId, String restaurantId, String restaurantName){
        Booking bookingToCreate = new Booking(bookingDate, userId, restaurantId, restaurantName);
        return RestaurantsHelper.getBookingCollection().add(bookingToCreate);
    }

    public static Task<Void> createLike(String restaurantId, String userId){
        Map<String,Object> user = new HashMap<>();
        user.put(userId,true);
        return RestaurantsHelper.getLikedCollection().document(restaurantId).set(user, SetOptions.merge());
    }

    // --- GET ---

    public static Task<QuerySnapshot> getBooking(String userId, String bookingDate){
        return RestaurantsHelper.getBookingCollection().whereEqualTo("userId", userId).whereEqualTo("bookingDate", bookingDate).get();
    }

    public static Task<QuerySnapshot> getTodayBooking(String restaurantId, String bookingDate){
        return RestaurantsHelper.getBookingCollection().whereEqualTo("restaurantId", restaurantId).whereEqualTo("bookingDate", bookingDate).get();
    }

    public static Task<DocumentSnapshot> getLikeForThisRestaurant(String restaurantId){
        return RestaurantsHelper.getLikedCollection().document(restaurantId).get();
    }

    public static Task<QuerySnapshot> getAllLikeByUserId(String userId){
        return RestaurantsHelper.getLikedCollection().whereEqualTo(userId,true).get();
    }

    // --- DELETE ---
    public static Task<Void> deleteBooking(String bookingId){
        return RestaurantsHelper.getBookingCollection().document(bookingId).delete();
    }

    public static Boolean deleteLike(String restaurantId, String userId){
        RestaurantsHelper.getLikeForThisRestaurant(restaurantId).addOnCompleteListener(restaurantTask -> {
           if (restaurantTask.isSuccessful()){
               Map<String,Object> update = new HashMap<>();
               update.put(userId, FieldValue.delete());
               RestaurantsHelper.getLikedCollection().document(restaurantId).update(update);
           }
        });
        return true;
    }
}
