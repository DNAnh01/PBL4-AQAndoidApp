package com.dnanh.pbl4_aqandoidapp.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dnanh.pbl4_aqandoidapp.R;
import com.dnanh.pbl4_aqandoidapp.activities.ChatActivity;
import com.dnanh.pbl4_aqandoidapp.activities.IncomingInvitationActivity;
import com.dnanh.pbl4_aqandoidapp.models.User;
import com.dnanh.pbl4_aqandoidapp.utilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Log.d("FCM", "Token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
        chỉ in mã thông báo và tin nhắn từ xa,
        để kiểm tra xem tin nhắn đám mây firebase có được thiết lập chính xác hay không
         */
        //Log.d("FCM", "Message: " + remoteMessage.getNotification().getBody());
        User user = new User();
        user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
        user.name = remoteMessage.getData().get(Constants.KEY_NAME);
        user.token= remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId,builder.build());

        //call

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        if(type != null) {
            if(type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent intentCall = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intentCall.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intentCall.putExtra(
                        Constants.KEY_NAME,
                        remoteMessage.getData().get(Constants.KEY_NAME)
                );
                intentCall.putExtra(
                        Constants.KEY_EMAIL,
                        remoteMessage.getData().get(Constants.KEY_EMAIL)
                );
                intentCall.putExtra(
                        Constants.KEY_USER_ID,
                        remoteMessage.getData().get(Constants.KEY_USER_ID)
                );
                intentCall.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intentCall.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                intentCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCall);
            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
//                để gửi lại các hành động chấp nhận từ chối hủy cho người dùng đối diện.
//                Tại đây, các hành động chấp nhận và từ chối sẽ gửi lại cho người gửi để thông báo
//                cho người gửi về việc người nhận đã chấp nhận hoặc từ chối lời mời
//                và hành động hủy sẽ được gửi đến người nhận để hủy cuộc gọi
                Intent intentAcceptRejectCancel = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intentAcceptRejectCancel.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentAcceptRejectCancel);
            }
        }
    }
}
