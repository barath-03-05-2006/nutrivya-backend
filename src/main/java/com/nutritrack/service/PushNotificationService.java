package com.nutritrack.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nutritrack.entity.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PushNotificationService {

    /**
     * Sends a push notification to a user's registered device, if any.
     * Never throws — a push failure should never break whatever request
     * triggered it, same defensive pattern as EmailService calls elsewhere
     * (e.g. MealPlanService wraps its email send in its own try/catch).
     */
    public void send(User user, String title, String body, Map<String, String> data) {
        if (user == null || user.getFcmToken() == null || user.getFcmToken().isBlank()) return;
        if (FirebaseApp.getApps().isEmpty()) return; // not configured yet — skip quietly

        try {
            Message.Builder builder = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null) builder.putAllData(data);
            FirebaseMessaging.getInstance().send(builder.build());
        } catch (FirebaseMessagingException e) {
            System.err.println("[Push] Failed to notify user " + user.getId() + ": " + e.getMessage());
        }
    }

    public void send(User user, String title, String body) {
        send(user, title, body, null);
    }
}
