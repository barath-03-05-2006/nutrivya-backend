package com.nutritrack.service;
import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {
    @Autowired private AlertRepository alertRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private DailyLogRepository dailyLogRepo;

    // Dedup window: don't re-fire the same alert type for the same client within 24 hours
    private static final int DEDUP_HOURS = 24;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkAlerts() {
        runAllChecks();
    }

    public void runAllChecks() {
        profileRepo.findAll().forEach(p -> {
            if (p.getDietitian() == null) return;
            checkMissedMeals(p);
            checkNutrition(p);
            checkCompliance(p);
        });
    }

    /** Returns true if the same alert type was already fired for this client within DEDUP_HOURS */
    private boolean alreadyFiredRecently(Long clientId, Alert.AlertType type) {
        LocalDateTime since = LocalDateTime.now().minusHours(DEDUP_HOURS);
        return alertRepo.existsByClientIdAndAlertTypeAndCreatedAtAfter(clientId, type, since);
    }

    private void checkMissedMeals(ClientProfile p) {
        Long clientId = p.getUser().getId();
        if (alreadyFiredRecently(clientId, Alert.AlertType.MISSED_MEALS)) return;

        int missed = 0;
        for (int i = 1; i <= 3; i++) {
            DailyLog l = dailyLogRepo
                .findByClientIdAndLogDate(clientId, LocalDate.now().minusDays(i))
                .orElse(null);
            if (l == null || l.getMealsCompleted() == 0) missed++;
        }
        if (missed >= 3) {
            createAlert(p.getUser(), p.getDietitian(), Alert.AlertType.MISSED_MEALS,
                p.getUser().getFullName() + " has missed meals for 3 consecutive days.");
        }
    }

    private void checkNutrition(ClientProfile p) {
        Long clientId = p.getUser().getId();
        DailyLog l = dailyLogRepo
            .findByClientIdAndLogDate(clientId, LocalDate.now().minusDays(1))
            .orElse(null);
        if (l == null) return;

        if (p.getTargetProtein() != null
                && l.getProteinConsumed() < p.getTargetProtein() * 0.7
                && !alreadyFiredRecently(clientId, Alert.AlertType.LOW_PROTEIN)) {
            createAlert(p.getUser(), p.getDietitian(), Alert.AlertType.LOW_PROTEIN,
                p.getUser().getFullName() + "'s protein intake was below 70% of target yesterday.");
        }

        if (p.getTargetCalories() != null
                && l.getCaloriesConsumed() < p.getTargetCalories() * 0.6
                && !alreadyFiredRecently(clientId, Alert.AlertType.LOW_CALORIES)) {
            createAlert(p.getUser(), p.getDietitian(), Alert.AlertType.LOW_CALORIES,
                p.getUser().getFullName() + "'s calorie intake was significantly below target yesterday.");
        }
    }

    private void checkCompliance(ClientProfile p) {
        Long clientId = p.getUser().getId();
        if (alreadyFiredRecently(clientId, Alert.AlertType.LOW_COMPLIANCE)) return;

        List<DailyLog> logs = dailyLogRepo.findByRange(
            clientId, LocalDate.now().minusDays(7), LocalDate.now());
        int ta = logs.stream().mapToInt(DailyLog::getMealsAssigned).sum();
        int tc = logs.stream().mapToInt(DailyLog::getMealsCompleted).sum();
        if (ta > 0 && (double) tc / ta * 100 < 70) {
            createAlert(p.getUser(), p.getDietitian(), Alert.AlertType.LOW_COMPLIANCE,
                p.getUser().getFullName() + "'s meal compliance dropped below 70% over the past 7 days.");
        }
    }

    private void createAlert(User client, User dietitian, Alert.AlertType type, String msg) {
        Alert a = new Alert();
        a.setClient(client);
        a.setDietitian(dietitian);
        a.setAlertType(type);
        a.setMessage(msg);
        a.setRead(false);
        alertRepo.save(a);
    }

    public List<Alert> getDietitianAlerts(Long dietitianId) {
        return alertRepo.findByDietitianIdOrderByCreatedAtDesc(dietitianId);
    }
    public List<Alert> getUnread(Long dietitianId) {
        return alertRepo.findByDietitianIdAndReadFalseOrderByCreatedAtDesc(dietitianId);
    }
    public long getUnreadCount(Long dietitianId) {
        return alertRepo.countByDietitianIdAndReadFalse(dietitianId);
    }
    public void markRead(Long alertId) {
        alertRepo.findById(alertId).ifPresent(a -> { a.setRead(true); alertRepo.save(a); });
    }
    public void markAllRead(Long dietitianId) {
        alertRepo.findByDietitianIdAndReadFalseOrderByCreatedAtDesc(dietitianId)
            .forEach(a -> { a.setRead(true); alertRepo.save(a); });
    }
}
