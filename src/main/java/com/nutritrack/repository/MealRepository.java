package com.nutritrack.repository;
import com.nutritrack.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MealRepository extends JpaRepository<Meal,Long> {}
