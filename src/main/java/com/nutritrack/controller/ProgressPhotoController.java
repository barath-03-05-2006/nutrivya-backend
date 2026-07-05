package com.nutritrack.controller;

import com.nutritrack.entity.ProgressPhoto;
import com.nutritrack.entity.User;
import com.nutritrack.repository.ProgressPhotoRepository;
import com.nutritrack.security.AccessGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/photos")
public class ProgressPhotoController {

    @Autowired private ProgressPhotoRepository photoRepo;
    @Autowired private AccessGuard guard;

    /** Client uploads a progress photo */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "label", defaultValue = "") String label,
            @RequestParam(value = "date", required = false) String dateStr,
            Authentication auth) {
        try {
            User client = guard.currentUser(auth);
            if (client.getRole() != User.Role.CLIENT) {
                return ResponseEntity.status(403).body(Map.of("error", "Only clients can upload progress photos"));
            }
            if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
            String ct = file.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only image files allowed"));
            }
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("error", "Image must be under 5MB"));
            }

            ProgressPhoto photo = new ProgressPhoto();
            photo.setClient(client);
            photo.setImageData(file.getBytes());
            photo.setImageType(ct);
            photo.setLabel(label.trim().isEmpty() ? null : label.trim());
            photo.setPhotoDate(dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : LocalDate.now());
            photoRepo.save(photo);

            return ResponseEntity.ok(Map.of("success", true, "id", photo.getId(),
                "message", "Photo uploaded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** List metadata (no image bytes) for a client's photos */
    @GetMapping("/my")
    public ResponseEntity<?> myPhotos(Authentication auth) {
        User client = guard.currentUser(auth);
        List<ProgressPhoto> photos = photoRepo.findByClientIdOrderByDateDesc(client.getId());
        return ResponseEntity.ok(photos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("label", p.getLabel());
            m.put("photoDate", p.getPhotoDate());
            m.put("uploadedAt", p.getUploadedAt());
            m.put("imageType", p.getImageType());
            return m;
        }).toList());
    }

    /** Dietitian views client photos — ownership enforced */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> clientPhotos(@PathVariable Long clientId, Authentication auth) {
        guard.requireClientAccess(auth, clientId);
        List<ProgressPhoto> photos = photoRepo.findByClientIdOrderByDateDesc(clientId);
        return ResponseEntity.ok(photos.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("label", p.getLabel());
            m.put("photoDate", p.getPhotoDate());
            m.put("uploadedAt", p.getUploadedAt());
            m.put("imageType", p.getImageType());
            return m;
        }).toList());
    }

    /** Serve the actual image bytes */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id, Authentication auth) {
        try {
            ProgressPhoto photo = photoRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));
            // Allow either the client themselves or their dietitian
            guard.requireClientAccess(auth, photo.getClient().getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(photo.getImageType()));
            headers.setCacheControl(CacheControl.maxAge(java.time.Duration.ofDays(30)));
            return new ResponseEntity<>(photo.getImageData(), headers, HttpStatus.OK);
        } catch (AccessGuard.AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Delete a photo — client can only delete their own */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        try {
            User requester = guard.currentUser(auth);
            ProgressPhoto photo = photoRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            if (!photo.getClient().getId().equals(requester.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "You can only delete your own photos"));
            }
            photoRepo.delete(photo);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
