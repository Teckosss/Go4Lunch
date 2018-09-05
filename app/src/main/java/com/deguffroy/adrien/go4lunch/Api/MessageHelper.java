package com.deguffroy.adrien.go4lunch.Api;

import com.deguffroy.adrien.go4lunch.Models.Message;
import com.deguffroy.adrien.go4lunch.Models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

/**
 * Created by Adrien Deguffroy on 02/09/2018.
 */
public class MessageHelper {
    private static final String COLLECTION_NAME = "messages";

    // --- CREATE ---

    public static Task<DocumentReference> createMessageForChat(String textMessage, User userSender){

        // 1 - Create the Message object
        Message message = new Message(textMessage, userSender);

        // 2 - Store Message to Firestore
        return ChatHelper.getChatCollection()
                .add(message);
    }

    // --- GET ---

    public static Query getAllMessageForChat(){
        return ChatHelper.getChatCollection()
                .orderBy("dateCreated")
                .limit(50);
    }


}
