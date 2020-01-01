package com.example.roome;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireMsgService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        Log.v("Firebase MSG", ""+remoteMessage.getNotification().toString());



    }
}
//todo delete file and in manifest??????