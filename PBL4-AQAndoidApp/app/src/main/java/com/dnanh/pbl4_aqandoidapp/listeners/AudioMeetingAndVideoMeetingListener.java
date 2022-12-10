package com.dnanh.pbl4_aqandoidapp.listeners;

import com.dnanh.pbl4_aqandoidapp.models.User;

public interface AudioMeetingAndVideoMeetingListener {
    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);
}
