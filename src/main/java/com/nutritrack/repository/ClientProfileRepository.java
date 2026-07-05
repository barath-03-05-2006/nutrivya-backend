package com.nutritrack.repository;
import com.nutritrack.entity.ClientProfile; import com.nutritrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface ClientProfileRepository extends JpaRepository<ClientProfile,Long> {
    Optional<ClientProfile> findByUser(User user);
    Optional<ClientProfile> findByUserId(Long userId);
    List<ClientProfile> findByDietitian(User dietitian);
    List<ClientProfile> findByDietitianId(Long dietitianId);
}
