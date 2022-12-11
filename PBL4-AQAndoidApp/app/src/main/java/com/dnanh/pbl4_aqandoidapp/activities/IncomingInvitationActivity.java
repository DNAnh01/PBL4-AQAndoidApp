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
import com.dnanh.pbl4_aqandoidapp.databinding.ActivityIncomingInvitationBinding;
import com.dnanh.pbl4_aqandoidapp.models.User;
import com.dnanh.pbl4_aqandoidapp.network.ApiClient;
import com.dnanh.pbl4_aqandoidapp.network.ApiService;
import com.dnanh.pbl4_aqandoidapp.utilities.Constants;
import com.dnanh.pbl4_aqandoidapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private ActivityIncomingInvitationBinding binding;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        String meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType != null) {
            if (meetingType.equals("video")) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_video);
            }
        }
        // set name
        binding.textName.setText(getIntent().getStringExtra(Constants.KEY_NAME));
        // set email
        binding.textEmail.setText(getIntent().getStringExtra(Constants.KEY_EMAIL));

        // set image
        preferenceManager.putString(Constants.KEY_SENDER_ID,getIntent().getStringExtra(Constants.KEY_USER_ID));


        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(
                        task -> {
                            String currentUserId = preferenceManager.getString(Constants.KEY_SENDER_ID);
                            List<User> users = new ArrayList<>();
                            if (task.isSuccessful() && task.getResult() != null){
                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    if(!currentUserId.equals(queryDocumentSnapshot.getId())){
                                        continue;
                                    }else {
                                        preferenceManager.putString(Constants.KEY_SENDER_IMAGE,queryDocumentSnapshot.getString(Constants.KEY_IMAGE));
                                        break;
                                    }
                                }
                            }
                        }
                );
        binding.imageProfileCall.setImageBitmap(
                getBitmapFromEncodedString(
                        preferenceManager.getString(Constants.KEY_SENDER_IMAGE)
                )
        );



        binding.imageAcceptInvitation.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        binding.imageRejectInvitation.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception exception) {
            showToast(exception.getMessage());
            finish();
        }
    }

    private void showToast(String message) {
        Toast.makeText(IncomingInvitationActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
//                    if(type.equals(Constants.REMOTE_MSG_INVITATION)) {
//                        showToast("Invitation sent successfully");
//                    }
                    if(type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
//                        showToast("Invitation Accepted");
                        try {
                            URL serverURL = new URL("https://meet.jit.si");
                            JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                            .setServerURL(serverURL)
                                            .setWelcomePageEnabled(false)
                                            .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                                            .build();

                            JitsiMeetActivity.launch(IncomingInvitationActivity.this,conferenceOptions);
                            finish();
                        }catch (Exception exception) {
                            showToast(exception.getMessage().toString());
                            finish();
                        }

                    }else {
                        showToast("Invitation Rejected");
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

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    showToast("Invitation Cancelled");
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