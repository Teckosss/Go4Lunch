package com.deguffroy.adrien.go4lunch.Api;

import com.deguffroy.adrien.go4lunch.Models.Booking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

/**
 * Created by Adrien Deguffroy on 16/08/2018.
 */
public class RestaurantsHelper {
    private static final String COLLECTION_NAME = "booking";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getBookingCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<DocumentReference> createBooking(String bookingDate, String userId, String restaurantId, String restaurantName){
        Booking bookingToCreate = new Booking(bookingDate, userId, restaurantId, restaurantName);
        return RestaurantsHelper.getBookingCollection().add(bookingToCreate);
    }

    // --- GET ---

    public static Task<QuerySnapshot> getBooking(String userId, String bookingDate){
        return RestaurantsHelper.getBookingCollection().whereEqualTo("userId", userId).whereEqualTo("bookingDate", bookingDate).get();
    }

    public static Task<QuerySnapshot> getTodayBooking(String restaurantId, String bookingDate){
        return RestaurantsHelper.getBookingCollection().whereEqualTo("restaurantId", restaurantId).whereEqualTo("bookingDate", bookingDate).get();
    }

    // --- DELETE ---
    public static Task<Void> deleteBooking(String bookingId){
        return RestaurantsHelper.getBookingCollection().document(bookingId).delete();
    }
}
