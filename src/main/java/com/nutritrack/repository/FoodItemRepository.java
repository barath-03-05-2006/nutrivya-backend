package com.nutritrack.repository;
import com.nutritrack.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FoodItemRepository extends JpaRepository<FoodItem,Long> {}