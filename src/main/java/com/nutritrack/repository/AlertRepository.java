package com.nutritrack.repository;
import com.nutritrack.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDietitianIdOrderByCreatedAtDesc(Long dietitianId);
    List<Alert> findByDietitianIdAndReadFalseOrderByCreatedAtDesc(Long dietitianId);
    long countByDietitianIdAndReadFalse(Long dietitianId);

    // Old dedup (still needed for other queries)
    boolean existsByClientIdAndAlertTypeAndReadFalse(Long clientId, Alert.AlertType alertType);

    // NEW: date-window dedup — don't recreate same alert for same client within a time window
    @Query("SELECT COUNT(a) > 0 FROM Alert a WHERE a.client.id = :clientId AND a.alertType = :alertType AND a.createdAt >= :since")
    boolean existsByClientIdAndAlertTypeAndCreatedAtAfter(
        @Param("clientId") Long clientId,
        @Param("alertType") Alert.AlertType alertType,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT a FROM Alert a WHERE a.createdAt < :cutoff")
    List<Alert> findOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
