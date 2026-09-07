package com.nutritrack.repository;
import com.nutritrack.entity.ClientProfile; import com.nutritrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List; import java.util.Optional;
public interface ClientProfileRepository extends JpaRepository<ClientProfile,Long> {
    Optional<ClientProfile> findByUser(User user);
    Optional<ClientProfile> findByUserId(Long userId);
    List<ClientProfile> findByDietitian(User dietitian);
    List<ClientProfile> findByDietitianId(Long dietitianId);

    // NEW: loads profiles + their user + their dietitian in ONE query (avoids EAGER-triggered N+1)
    @Query("SELECT p FROM ClientProfile p JOIN FETCH p.user JOIN FETCH p.dietitian WHERE p.dietitian = :dietitian")
    List<ClientProfile> findByDietitianFetched(@Param("dietitian") User dietitian);
}
