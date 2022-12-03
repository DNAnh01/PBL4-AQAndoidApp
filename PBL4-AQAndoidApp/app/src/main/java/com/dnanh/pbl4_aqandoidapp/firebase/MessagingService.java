package com.dnanh.pbl4_aqandoidapp.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        chỉ in mã thông báo và tin nhắn từ xa,
        để kiểm tra xem tin nhắn đám mây firebase có được thiết lập chính xác hay không
         */
        Log.d("FCM", "Message: " + remoteMessage.getNotification().getBody());
    }
}
