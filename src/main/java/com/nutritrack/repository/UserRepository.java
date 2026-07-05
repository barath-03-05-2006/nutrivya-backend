package com.nutritrack.repository;
import com.nutritrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByRole(User.Role role);
}
