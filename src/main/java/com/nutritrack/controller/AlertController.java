package com.nutritrack.controller;
import com.nutritrack.entity.User; import com.nutritrack.repository.UserRepository;
import com.nutritrack.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/alerts") public class AlertController {
    @Autowired private AlertService alertService;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> getAll(Authentication auth){
        User d=userRepo.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(alertService.getDietitianAlerts(d.getId()));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnread(Authentication auth){
        User d=userRepo.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(alertService.getUnread(d.getId()));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCount(Authentication auth){
        User d=userRepo.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(Map.of("count", alertService.getUnreadCount(d.getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id){
        alertService.markRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(Authentication auth){
        User d=userRepo.findByEmail(auth.getName()).orElseThrow();
        alertService.markAllRead(d.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Manual trigger for testing — runs all alert checks immediately
    @PostMapping("/trigger")
    public ResponseEntity<?> triggerAlerts(Authentication auth){
        alertService.runAllChecks();
        return ResponseEntity.ok(Map.of("message", "Alert checks triggered successfully"));
    }
}
