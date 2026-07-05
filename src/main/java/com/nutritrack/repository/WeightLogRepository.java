package com.nutritrack.repository;
import com.nutritrack.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WeightLogRepository extends JpaRepository<WeightLog,Long> {
    List<WeightLog> findByClientIdOrderByLogDateAsc(Long clientId);
}
