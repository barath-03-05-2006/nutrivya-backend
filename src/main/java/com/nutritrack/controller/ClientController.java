package com.nutritrack.controller;
import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import com.nutritrack.security.AccessGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/clients") public class ClientController {
    @Autowired private UserRepository userRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private AccessGuard guard;

    // ── CLIENT READS THEIR OWN PROFILE ─────────────────────────────
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(Authentication auth){
        User u = guard.currentUser(auth);
        return profileRepo.findByUser(u)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ── DIETITIAN OR CLIENT READS A SPECIFIC CLIENT'S PROFILE ──────
    @GetMapping("/profile/{clientId}")
    public ResponseEntity<?> getProfile(@PathVariable Long clientId, Authentication auth){
        guard.requireClientAccess(auth, clientId); // 403 if not authorized
        return profileRepo.findByUserId(clientId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ── CLIENT SAVES THEIR OWN PROFILE (or dietitian saves on behalf via userId field) ─
    @PostMapping("/profile")
    public ResponseEntity<?> saveProfile(@RequestBody Map<String,Object> body, Authentication auth){
        try {
            User requester = guard.currentUser(auth);
            User targetUser;

            if (body.containsKey("userId") && body.get("userId") != null) {
                // Dietitian is saving on behalf of a client — verify ownership
                Long targetId = Long.valueOf(body.get("userId").toString());
                guard.requireDietitianOwnership(auth, targetId);
                targetUser = userRepo.findById(targetId).orElseThrow();
            } else {
                // Client saving their own profile — only allowed for themselves
                targetUser = requester;
            }

            ClientProfile p = profileRepo.findByUser(targetUser).orElse(new ClientProfile());
            p.setUser(targetUser);

            if (body.containsKey("dietitianId") && body.get("dietitianId") != null)
                userRepo.findById(Long.valueOf(body.get("dietitianId").toString())).ifPresent(p::setDietitian);

            if (v(body,"currentWeight"))  p.setCurrentWeight(d(body,"currentWeight"));
            if (v(body,"startingWeight")) p.setStartingWeight(d(body,"startingWeight"));
            if (v(body,"goalWeight"))     p.setGoalWeight(d(body,"goalWeight"));
            if (v(body,"height"))         p.setHeight(d(body,"height"));
            if (v(body,"age"))            p.setAge(i(body,"age"));
            if (v(body,"gender"))         p.setGender(body.get("gender").toString());
            if (v(body,"targetCalories")) p.setTargetCalories(i(body,"targetCalories"));
            if (v(body,"targetProtein"))  p.setTargetProtein(i(body,"targetProtein"));
            if (v(body,"targetCarbs"))    p.setTargetCarbs(i(body,"targetCarbs"));
            if (v(body,"targetFat"))      p.setTargetFat(i(body,"targetFat"));
            if (v(body,"targetFiber"))    p.setTargetFiber(i(body,"targetFiber"));
            if (v(body,"targetWater"))    p.setTargetWater(i(body,"targetWater"));
            if (v(body,"notes"))          p.setNotes(body.get("notes").toString());
            if (v(body,"startDate")) {
                try { p.setStartDate(LocalDate.parse(body.get("startDate").toString())); }
                catch (Exception ignored) {}
            }
            // Questionnaire fields
            if (body.containsKey("medicalDiagnosis"))    p.setMedicalDiagnosis(bool(body,"medicalDiagnosis"));
            if (body.containsKey("regularMedicine"))     p.setRegularMedicine(bool(body,"regularMedicine"));
            if (body.containsKey("pastMedicalHistory"))  p.setPastMedicalHistory(bool(body,"pastMedicalHistory"));
            if (body.containsKey("medicalSurgery"))      p.setMedicalSurgery(bool(body,"medicalSurgery"));
            if (v(body,"workLifestyle"))         p.setWorkLifestyle(body.get("workLifestyle").toString());
            if (v(body,"socialHabits"))          p.setSocialHabits(body.get("socialHabits").toString());
            if (v(body,"physicalActivityLevel")) p.setPhysicalActivityLevel(body.get("physicalActivityLevel").toString());
            if (body.containsKey("supplements")) p.setSupplements(bool(body,"supplements"));
            if (v(body,"dietType"))              p.setDietType(body.get("dietType").toString());
            if (body.containsKey("foodAllergy")) p.setFoodAllergy(bool(body,"foodAllergy"));
            if (v(body,"foodAllergyDetails"))    p.setFoodAllergyDetails(body.get("foodAllergyDetails").toString());

            return ResponseEntity.ok(profileRepo.save(p));
        } catch (AccessGuard.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── DIETITIAN VIEWS THEIR OWN CLIENT LIST ─────────────────────
    @GetMapping("/dietitian/my-clients")
    public ResponseEntity<List<ClientProfile>> getMyClients(Authentication auth){
        User d = guard.currentUser(auth);
        return ResponseEntity.ok(profileRepo.findByDietitian(d));
    }

    @GetMapping("/dietitians")
    public ResponseEntity<List<User>> getDietitians(){
        return ResponseEntity.ok(userRepo.findByRole(User.Role.DIETITIAN));
    }

    // ── DIETITIAN SETS TARGETS FOR A SPECIFIC CLIENT ───────────────
    @PostMapping("/set-targets/{clientId}")
    public ResponseEntity<?> setTargets(
            @PathVariable Long clientId,
            @RequestBody Map<String,Integer> t,
            Authentication auth){
        try {
            guard.requireDietitianOwnership(auth, clientId); // only assigned dietitian
            ClientProfile p = profileRepo.findByUserId(clientId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
            if (t.containsKey("targetCalories")) p.setTargetCalories(t.get("targetCalories"));
            if (t.containsKey("targetProtein"))  p.setTargetProtein(t.get("targetProtein"));
            if (t.containsKey("targetCarbs"))    p.setTargetCarbs(t.get("targetCarbs"));
            if (t.containsKey("targetFat"))      p.setTargetFat(t.get("targetFat"));
            if (t.containsKey("targetFiber"))    p.setTargetFiber(t.get("targetFiber"));
            if (t.containsKey("targetWater"))    p.setTargetWater(t.get("targetWater"));
            return ResponseEntity.ok(profileRepo.save(p));
        } catch (AccessGuard.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private boolean v(Map<String,Object> m, String k) {
        return m.containsKey(k) && m.get(k) != null && !m.get(k).toString().isEmpty();
    }
    private Double d(Map<String,Object> m, String k) {
        try { return Double.parseDouble(m.get(k).toString()); } catch (Exception e) { return null; }
    }
    private Integer i(Map<String,Object> m, String k) {
        try { return Integer.parseInt(m.get(k).toString()); } catch (Exception e) { return null; }
    }
    private Boolean bool(Map<String,Object> m, String k) {
        Object val = m.get(k);
        if (val instanceof Boolean) return (Boolean) val;
        if (val != null) return Boolean.parseBoolean(val.toString());
        return null;
    }
}
