package com.dnanh.pbl4_aqandoidapp.network;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send")
    Call<String> sendMessage(
            // URL
            // https://fcm.googleapis.com/fcm/send
            @HeaderMap HashMap<String, String> headers,
            // Authorization: "key=your_server_key"
            // Content-Type: "application/json"
            @Body String messageBody
            /*
            Body
            {
                "data": {
                    "type": "invitation",
                    "meetingType": "video",
                    "name": "ABC",
                    "email": "ABC@gmail.com",
                      ...
                },
                "registration_ids": ["receiver_token"]
            }
            */
    );
}
