package com.nutritrack.controller;

import com.nutritrack.entity.User;
import com.nutritrack.repository.UserRepository;
import com.nutritrack.security.AccessGuard;
import com.nutritrack.service.WeeklyDigestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/digest")
public class DigestController {

    @Autowired private WeeklyDigestService digestService;
    @Autowired private AccessGuard guard;
    @Autowired private UserRepository userRepo;

    /** Dietitian can manually trigger their own weekly digest email for testing */
    @PostMapping("/send-my-digest")
    public ResponseEntity<?> sendMyDigest(Authentication auth) {
        try {
            User dietitian = guard.currentUser(auth);
            if (dietitian.getRole() != User.Role.DIETITIAN) {
                return ResponseEntity.status(403).body(Map.of("error", "Only dietitians can request a digest"));
            }
            digestService.sendDigestForDietitian(dietitian);
            return ResponseEntity.ok(Map.of("success", true, "message", "Weekly digest sent to " + dietitian.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
