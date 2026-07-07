package com.nutritrack.controller;

import com.nutritrack.entity.ClientProfile;
import com.nutritrack.entity.User;
import com.nutritrack.exception.AccountLockedException;
import com.nutritrack.exception.InvalidCredentialsException;
import com.nutritrack.repository.ClientProfileRepository;
import com.nutritrack.repository.UserRepository;
import com.nutritrack.service.AuthService;
import com.nutritrack.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.login(body.get("email"), body.get("password")));
        } catch (AccountLockedException e) {
            return ResponseEntity.status(423).body(Map.of(
                    "error", "Too many failed attempts. Please try again in " + formatWait(e.getSecondsRemaining()) + ".",
                    "locked", true,
                    "secondsRemaining", e.getSecondsRemaining()
            ));
        } catch (InvalidCredentialsException e) {
            String msg = "Invalid email or password.";
            if (e.getAttemptsRemaining() > 0) {
                msg += " " + e.getAttemptsRemaining() + " attempt" + (e.getAttemptsRemaining() == 1 ? "" : "s")
                        + " remaining before your account is temporarily locked.";
            }
            return ResponseEntity.status(401).body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }

    private String formatWait(long seconds) {
        if (seconds < 60) return seconds + " second" + (seconds == 1 ? "" : "s");
        long minutes = (seconds + 59) / 60;
        return minutes + " minute" + (minutes == 1 ? "" : "s");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
            }

            // Verify caller is a dietitian
            User dietitian = userRepo.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Caller not found"));
            if (dietitian.getRole() != User.Role.DIETITIAN) {
                return ResponseEntity.status(403).body(Map.of("error", "Only dietitians can create client accounts"));
            }

            // Create the client user account
            Map<String, Object> result = authService.register(
                    body.get("username"),
                    body.get("email"),
                    body.get("password"),
                    body.get("fullName"),
                    body.getOrDefault("phoneNumber", "")
            );

            // Get the newly created client user
            Long newClientId = ((Number) result.get("userId")).longValue();
            User newClient = userRepo.findById(newClientId)
                    .orElseThrow(() -> new RuntimeException("New client not found"));

            // Auto-create ClientProfile linked to this dietitian
            ClientProfile profile = new ClientProfile();
            profile.setUser(newClient);
            profile.setDietitian(dietitian);
            profile.setStartDate(LocalDate.now());
            profile.setTargetCalories(2000);
            profile.setTargetProtein(150);
            profile.setTargetCarbs(200);
            profile.setTargetFat(65);
            profile.setTargetFiber(30);
            profile.setTargetWater(3000);
            profileRepo.save(profile);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.sendResetLink(body.get("email"));
            return ResponseEntity.ok(Map.of("message", "Reset link sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "If this email is registered, a reset link has been sent"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            passwordResetService.resetPassword(body.get("token"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
