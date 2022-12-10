package com.dnanh.pbl4_aqandoidapp.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.dnanh.pbl4_aqandoidapp.R;
import com.dnanh.pbl4_aqandoidapp.databinding.ActivityIncomingInvitationBinding;
import com.dnanh.pbl4_aqandoidapp.models.User;
import com.dnanh.pbl4_aqandoidapp.utilities.Constants;
import com.dnanh.pbl4_aqandoidapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

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
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }
}