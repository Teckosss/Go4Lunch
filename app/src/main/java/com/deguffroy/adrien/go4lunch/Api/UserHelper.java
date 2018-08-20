package com.deguffroy.adrien.go4lunch.Api;

import com.deguffroy.adrien.go4lunch.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by Adrien Deguffroy on 12/07/2018.
 */
public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture, String restaurantSelected, int searchRadius, int defaultZoom, boolean isNotificationOn) {
        User userToCreate = new User(uid, username, urlPicture, restaurantSelected, searchRadius, defaultZoom, isNotificationOn);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateBookedRestaurant(String restaurant, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("restaurantSelected", restaurant);
    }
}
