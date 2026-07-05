package com.nutritrack.controller;
import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import com.nutritrack.security.AccessGuard;
import com.nutritrack.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/analytics") public class AnalyticsController {
    @Autowired private AnalyticsService analyticsService;
    @Autowired private UserRepository userRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private WeightLogRepository weightLogRepo;
    @Autowired private ProgressNoteRepository noteRepo;
    @Autowired private AccessGuard guard;

    // ── CLIENT'S OWN DAILY SUMMARY ──────────────────────────────────
    @GetMapping("/my/daily")
    public ResponseEntity<?> myDaily(Authentication auth,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){
        User u = guard.currentUser(auth);
        return ResponseEntity.ok(analyticsService.getDailySummary(u.getId(), date != null ? date : LocalDate.now()));
    }

    // ── DIETITIAN VIEWS A CLIENT'S DAILY SUMMARY ───────────────────
    @GetMapping("/daily/{clientId}")
    public ResponseEntity<?> clientDaily(@PathVariable Long clientId, Authentication auth,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){
        guard.requireClientAccess(auth, clientId);
        return ResponseEntity.ok(analyticsService.getDailySummary(clientId, date != null ? date : LocalDate.now()));
    }

    // ── DIETITIAN VIEWS A CLIENT'S WEEKLY SUMMARY ──────────────────
    @GetMapping("/weekly/{clientId}")
    public ResponseEntity<?> weekly(@PathVariable Long clientId, Authentication auth,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate weekStart){
        guard.requireClientAccess(auth, clientId);
        LocalDate ws = weekStart != null ? weekStart : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return ResponseEntity.ok(analyticsService.getWeeklySummary(clientId, ws));
    }

    // ── CLIENT'S OWN WEEKLY SUMMARY ────────────────────────────────
    @GetMapping("/my/weekly")
    public ResponseEntity<?> myWeekly(Authentication auth,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate weekStart){
        User u = guard.currentUser(auth);
        LocalDate ws = weekStart != null ? weekStart : LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        return ResponseEntity.ok(analyticsService.getWeeklySummary(u.getId(), ws));
    }

    // ── DIETITIAN VIEWS A CLIENT'S FULL OVERVIEW ───────────────────
    @GetMapping("/client-overview/{clientId}")
    public ResponseEntity<?> clientOverview(@PathVariable Long clientId, Authentication auth){
        guard.requireClientAccess(auth, clientId);
        return ResponseEntity.ok(analyticsService.getClientOverview(clientId));
    }

    // ── DIETITIAN VIEWS ALL THEIR OWN CLIENTS' OVERVIEWS ──────────
    @GetMapping("/dietitian/clients")
    public ResponseEntity<?> allClientsOverview(Authentication auth){
        User d = guard.currentUser(auth);
        // Only returns clients assigned to THIS dietitian — no cross-dietitian leakage
        List<ClientProfile> clients = profileRepo.findByDietitian(d);
        List<Map<String,Object>> overviews = clients.stream()
            .map(c -> analyticsService.getClientOverview(c.getUser().getId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(overviews);
    }

    // ── CLIENT LOGS THEIR OWN WATER ────────────────────────────────
    @PostMapping("/water")
    public ResponseEntity<?> logWater(@RequestBody Map<String,Object> body, Authentication auth){
        User u = guard.currentUser(auth);
        double amount = Double.parseDouble(body.get("waterAmount").toString());
        LocalDate date = body.containsKey("date") ? LocalDate.parse(body.get("date").toString()) : LocalDate.now();
        analyticsService.updateWater(u.getId(), amount, date);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── CLIENT LOGS THEIR OWN WEIGHT ───────────────────────────────
    @PostMapping("/weight")
    public ResponseEntity<?> logWeight(@RequestBody Map<String,Object> body, Authentication auth){
        User u = guard.currentUser(auth);
        double weight = Double.parseDouble(body.get("weight").toString());
        LocalDate date = body.containsKey("date") ? LocalDate.parse(body.get("date").toString()) : LocalDate.now();
        String notes = body.containsKey("notes") ? body.get("notes").toString() : "";
        return ResponseEntity.ok(analyticsService.logWeight(u.getId(), weight, date, notes));
    }

    // ── DIETITIAN VIEWS A CLIENT'S WEIGHT HISTORY ─────────────────
    @GetMapping("/weight-history/{clientId}")
    public ResponseEntity<?> weightHistory(@PathVariable Long clientId, Authentication auth){
        guard.requireClientAccess(auth, clientId);
        return ResponseEntity.ok(weightLogRepo.findByClientIdOrderByLogDateAsc(clientId));
    }

    // ── CLIENT VIEWS THEIR OWN WEIGHT HISTORY ──────────────────────
    @GetMapping("/my/weight-history")
    public ResponseEntity<?> myWeightHistory(Authentication auth){
        User u = guard.currentUser(auth);
        return ResponseEntity.ok(weightLogRepo.findByClientIdOrderByLogDateAsc(u.getId()));
    }

    // ── DIETITIAN ADDS A NOTE FOR A CLIENT ─────────────────────────
    @PostMapping("/progress-note/{clientId}")
    public ResponseEntity<?> addNote(@PathVariable Long clientId,
            @RequestBody Map<String,String> body, Authentication auth){
        guard.requireDietitianOwnership(auth, clientId); // only that client's actual dietitian
        User d = guard.currentUser(auth);
        User client = userRepo.findById(clientId).orElseThrow();

        String noteText = body.get("note");
        if (noteText == null || noteText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Note cannot be empty"));
        }
        if (noteText.length() > 1000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Note is too long (max 1000 characters)"));
        }

        ProgressNote n = new ProgressNote();
        n.setClient(client);
        n.setDietitian(d);
        n.setNote(noteText.trim());
        noteRepo.save(n);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ── DIETITIAN READS A CLIENT'S NOTES ───────────────────────────
    @GetMapping("/progress-notes/{clientId}")
    public ResponseEntity<?> getNotes(@PathVariable Long clientId, Authentication auth){
        guard.requireDietitianOwnership(auth, clientId);
        List<ProgressNote> notes = noteRepo.findByClientIdOrderByCreatedAtDesc(clientId);
        return ResponseEntity.ok(notes.stream().map(this::toNoteDto).collect(Collectors.toList()));
    }

    // ── CLIENT READS THEIR OWN NOTES FROM DIETITIAN ───────────────
    @GetMapping("/my-progress-notes")
    public ResponseEntity<?> myProgressNotes(Authentication auth){
        User client = guard.currentUser(auth);
        List<ProgressNote> notes = noteRepo.findByClientIdOrderByCreatedAtDesc(client.getId());
        return ResponseEntity.ok(notes.stream().map(this::toNoteDto).collect(Collectors.toList()));
    }

    private Map<String, Object> toNoteDto(ProgressNote n) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", n.getId());
        dto.put("note", n.getNote());
        dto.put("createdAt", n.getCreatedAt());
        dto.put("dietitianName", n.getDietitian() != null ? n.getDietitian().getFullName() : null);
        return dto;
    }
}
