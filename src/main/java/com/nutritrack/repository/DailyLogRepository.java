package com.nutritrack.repository;
import com.nutritrack.entity.DailyLog; import com.nutritrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate; import java.util.List; import java.util.Optional;
public interface DailyLogRepository extends JpaRepository<DailyLog,Long> {
    Optional<DailyLog> findByClientAndLogDate(User client, LocalDate date);
    Optional<DailyLog> findByClientIdAndLogDate(Long clientId, LocalDate date);
    @Query("SELECT d FROM DailyLog d WHERE d.client.id=:cid AND d.logDate BETWEEN :s AND :e ORDER BY d.logDate")
    List<DailyLog> findByRange(@Param("cid") Long cid, @Param("s") LocalDate s, @Param("e") LocalDate e);
    @Query("SELECT d FROM DailyLog d WHERE d.client.id=:cid AND d.logDate>=:from ORDER BY d.logDate DESC")
    List<DailyLog> findRecent(@Param("cid") Long cid, @Param("from") LocalDate from);
}
