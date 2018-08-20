package com.deguffroy.adrien.go4lunch.Api;

import com.deguffroy.adrien.go4lunch.Models.Booking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public static Task<DocumentReference> createBooking(String bookingDate, String userId, String restaurantId){
        Booking bookingToCreate = new Booking(bookingDate, userId, restaurantId);
        return RestaurantsHelper.getBookingCollection().add(bookingToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getBooking(String bookingId){
        return RestaurantsHelper.getBookingCollection().document(bookingId).get();
    }
}
