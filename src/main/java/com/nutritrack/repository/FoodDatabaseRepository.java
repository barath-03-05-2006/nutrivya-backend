package com.nutritrack.repository;
import com.nutritrack.entity.FoodDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FoodDatabaseRepository extends JpaRepository<FoodDatabase,Long> {
    List<FoodDatabase> findByFoodNameContainingIgnoreCase(String name);
    boolean existsByFoodNameIgnoreCase(String name);
}
