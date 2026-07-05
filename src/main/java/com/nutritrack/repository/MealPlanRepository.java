package com.nutritrack.repository;
import com.nutritrack.entity.MealPlan; import com.nutritrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.List; import java.util.Optional;
public interface MealPlanRepository extends JpaRepository<MealPlan,Long> {
    List<MealPlan> findByClientIdOrderByPlanDateDesc(Long clientId);
    Optional<MealPlan> findByClientAndPlanDate(User client, LocalDate date);
}
