package com.nutritrack.controller;
import com.nutritrack.entity.FoodDatabase;
import com.nutritrack.repository.FoodDatabaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/foods") public class FoodController {
    @Autowired private FoodDatabaseRepository foodRepo;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q){
        if(q==null||q.trim().length()<2) return ResponseEntity.ok(java.util.List.of());
        return ResponseEntity.ok(foodRepo.findByFoodNameContainingIgnoreCase(q.trim()));
    }

    @GetMapping
    public ResponseEntity<?> getAll(){ return ResponseEntity.ok(foodRepo.findAll()); }

    /**
     * Lets a dietitian add a brand-new food to the database with full nutrition info.
     * Once saved, it's permanently searchable for every future meal plan.
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body){
        try {
            String name = body.get("foodName") != null ? body.get("foodName").toString().trim() : "";
            if (name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Food name is required"));
            }
            if (name.length() > 100) {
                return ResponseEntity.badRequest().body(Map.of("error", "Food name is too long"));
            }
            if (foodRepo.existsByFoodNameIgnoreCase(name)) {
                return ResponseEntity.badRequest().body(Map.of("error", "A food with this name already exists. Try searching for it instead."));
            }

            FoodDatabase fd = new FoodDatabase();
            fd.setFoodName(name);
            fd.setCategory(strOrDefault(body.get("category"), "Custom"));
            fd.setServingSize(strOrDefault(body.get("servingSize"), "100"));
            fd.setServingUnit(strOrDefault(body.get("servingUnit"), "g"));
            fd.setCaloriesPer100g(numOrZero(body.get("caloriesPer100g")));
            fd.setProteinPer100g(numOrZero(body.get("proteinPer100g")));
            fd.setCarbsPer100g(numOrZero(body.get("carbsPer100g")));
            fd.setFatPer100g(numOrZero(body.get("fatPer100g")));
            fd.setFiberPer100g(numOrZero(body.get("fiberPer100g")));

            // Optional: unit conversion overrides like "piece:50,tbsp:15"
            Object override = body.get("unitGramsOverride");
            if (override != null && !override.toString().trim().isEmpty()) {
                fd.setUnitGramsOverride(override.toString().trim());
            }

            FoodDatabase saved = foodRepo.save(fd);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to add food: " + e.getMessage()));
        }
    }

    private String strOrDefault(Object val, String def) {
        if (val == null || val.toString().trim().isEmpty()) return def;
        return val.toString().trim();
    }

    private Double numOrZero(Object val) {
        if (val == null || val.toString().trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(val.toString().trim()); }
        catch (Exception e) { return 0.0; }
    }
}
