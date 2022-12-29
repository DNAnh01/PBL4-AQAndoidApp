package com.dnanh.pbl4_aqandoidapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dnanh.pbl4_aqandoidapp.R;
import com.dnanh.pbl4_aqandoidapp.databinding.ActivityOutgoingInvitationBinding;
import com.dnanh.pbl4_aqandoidapp.models.User;
import com.dnanh.pbl4_aqandoidapp.network.ApiClient;
import com.dnanh.pbl4_aqandoidapp.network.ApiService;
import com.dnanh.pbl4_aqandoidapp.utilities.Constants;
import com.dnanh.pbl4_aqandoidapp.utilities.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ActivityOutgoingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    //private String inviterToken = null;
    //private FirebaseFirestore database;
    private User receiverUser;
    //private String callId;
    private String meetingRoom = null;
    private String meetingType = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        //database = FirebaseFirestore.getInstance();

        meetingType = getIntent().getStringExtra("type");
        User user = (User) getIntent().getSerializableExtra("user");

        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_video);
            } else {
                binding.imageMeetingType.setImageResource(R.drawable.ic_audio);
            }
        }

        if (user != null) {
            binding.imageProfileCall.setImageBitmap(getBitmapFromEncodedString(user.image));
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
        }


        binding.imageStopInvitation.setOnClickListener(v -> {
            if(user != null) {
                cancelInvitation(user.token);
            }
        });

        if(!(preferenceManager.getString(Constants.KEY_FCM_TOKEN).isEmpty())) {

            if(meetingType != null && user != null) {
                initiateMeeting(meetingType,user.token);
            }
        }
    }

//    private void addCall(HashMap<String, Object> call) {
//        database.collection(Constants.KEY_COLLECTION_CALL).add(call).addOnSuccessListener(documentReference -> callId = documentReference.getId());
//    }


    private void showToast(String message) {
        Toast.makeText(OutgoingInvitationActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void initiateMeeting(String meetingType, String receiverToken) {
//        if(callId == null) {
//            HashMap<String, Object> call = new HashMap<>();
//            call.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
//            call.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
//            call.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
//            call.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
//            call.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
//            call.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
//            call.put(Constants.KEY_TIMESTAMP, new Date());
//            addCall(call);
//        }

        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
            data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));

            meetingRoom =
                    preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                            UUID.randomUUID().toString().substring(0, 5);

            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION);

        }catch (Exception exception) {
            showToast(exception.getMessage().toString());
            finish();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    if(type.equals(Constants.REMOTE_MSG_INVITATION)) {
                        showToast("Invitation sent successfully");
                    } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                        showToast("Invitation Cancelled");
                        finish();
                    }
                }else {
                    showToast(response.message().toString());
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage().toString());
                finish();
            }
        });
    }


    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception exception) {
            showToast(exception.getMessage());
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        URL serverURL = new URL("https://meet.jit.si");
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if(meetingType.equals("audio")) {
                            builder.setVideoMuted(true);
                        }
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                        finish();

                    }catch (Exception exception) {
                        showToast(exception.getMessage().toString());
                        finish();
                    }

                }else if(type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    showToast("Invitation Rejected");
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}