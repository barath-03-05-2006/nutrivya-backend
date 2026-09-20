package com.nutritrack.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * One-time Firebase Admin SDK init, used by PushNotificationService to send
 * push notifications. FIREBASE_SERVICE_ACCOUNT_BASE64 is the service
 * account JSON key — Firebase Console → Project Settings → Service accounts
 * → "Generate new private key" — base64-encoded into a single line so it
 * fits in a normal Render env var (Render env vars are one value per key,
 * and base64 avoids any issue with the raw JSON's newlines/quotes).
 *
 * If the env var isn't set, push notifications are silently disabled rather
 * than crashing the app — the same "degrade, don't break" approach as the
 * existing SMTP config.
 */
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-base64:}")
    private String serviceAccountBase64;

    @PostConstruct
    public void init() {
        if (serviceAccountBase64 == null || serviceAccountBase64.isBlank()) {
            System.err.println("[Firebase] FIREBASE_SERVICE_ACCOUNT_BASE64 not set — push notifications disabled.");
            return;
        }
        try {
            if (!FirebaseApp.getApps().isEmpty()) return;
            byte[] decoded = Base64.getDecoder().decode(serviceAccountBase64.trim());
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(decoded)))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("[Firebase] Initialized — push notifications enabled.");
        } catch (Exception e) {
            System.err.println("[Firebase] Failed to initialize: " + e.getMessage());
        }
    }
}
