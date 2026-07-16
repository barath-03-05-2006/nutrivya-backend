package com.nutritrack.controller;

import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import com.nutritrack.security.AccessGuard;
import com.nutritrack.service.MealPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    @Autowired private MealPlanService mealPlanService;
    @Autowired private UserRepository userRepo;
    @Autowired private MealPlanRepository mealPlanRepo;
    @Autowired private MealRepository mealRepo;
    @Autowired private AccessGuard guard;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            User d = userRepo.findByEmail(auth.getName()).orElseThrow();
            // Frontend sends startDate as planDate key
            if (body.get("planDate") == null && body.get("startDate") != null) {
                body.put("planDate", body.get("startDate"));
            }
            MealPlan saved = mealPlanService.createMealPlan(body, d.getId());
            return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                e.getMessage() != null ? e.getMessage() : "Failed to create meal plan"));
        }
    }

    /** Update plan name only (inline rename from ClientDetail) */
    @PutMapping("/{planId}")
    public ResponseEntity<?> update(@PathVariable Long planId,
                                    @RequestBody Map<String, Object> body,
                                    Authentication auth) {
        try {
            User requester = userRepo.findByEmail(auth.getName()).orElseThrow();
            MealPlan plan = mealPlanRepo.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Meal plan not found"));

            // Only the dietitian who created it can rename
            if (plan.getDietitian() == null || !plan.getDietitian().getId().equals(requester.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            if (body.containsKey("planName") && body.get("planName") != null) {
                plan.setPlanName(body.get("planName").toString().trim());
            }
            mealPlanRepo.save(plan);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                e.getMessage() != null ? e.getMessage() : "Failed to update meal plan"));
        }
    }

    /** Delete a meal plan and all its meals */
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> delete(@PathVariable Long planId, Authentication auth) {
        try {
            User requester = userRepo.findByEmail(auth.getName()).orElseThrow();
            MealPlan plan = mealPlanRepo.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Meal plan not found"));

            if (plan.getDietitian() == null || !plan.getDietitian().getId().equals(requester.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
            }

            mealPlanRepo.delete(plan);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                e.getMessage() != null ? e.getMessage() : "Failed to delete meal plan"));
        }
    }

    @GetMapping("/my-plans/today")
    public ResponseEntity<?> getToday(Authentication auth) {
        try {
            User u = userRepo.findByEmail(auth.getName()).orElseThrow();
            LocalDate today = LocalDate.now();
            Optional<MealPlan> plan = mealPlanService.getMealPlanByDate(u.getId(), today);
            if (plan.isEmpty()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(toDto(plan.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-plans")
    public ResponseEntity<?> getMyPlans(Authentication auth) {
        try {
            User u = userRepo.findByEmail(auth.getName()).orElseThrow();
            List<Map<String, Object>> result = mealPlanService.getClientMealPlans(u.getId())
                    .stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientPlans(@PathVariable Long clientId, Authentication auth) {
        try {
            guard.requireClientAccess(auth, clientId);
            List<Map<String, Object>> result = mealPlanService.getClientMealPlans(clientId)
                    .stream().map(this::toDto).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (AccessGuard.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/client/{clientId}/date/{date}")
    public ResponseEntity<?> getByDate(@PathVariable Long clientId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication auth) {
        try {
            guard.requireClientAccess(auth, clientId);
            Optional<MealPlan> plan = mealPlanService.getMealPlanByDate(clientId, date);
            if (plan.isEmpty()) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(toDto(plan.get()));
        } catch (AccessGuard.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/meal/{mealId}/complete")
    public ResponseEntity<?> completeMeal(@PathVariable Long mealId, Authentication auth) {
        try {
            User u = userRepo.findByEmail(auth.getName()).orElseThrow();
            mealPlanService.completeMeal(mealId, u.getId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Client: "I actually ate something different" note for ONE food item in a meal. */
    @PostMapping("/food-item/{foodItemId}/deviation")
    public ResponseEntity<?> reportFoodItemDeviation(@PathVariable Long foodItemId,
                                              @RequestBody Map<String, Object> body,
                                              Authentication auth) {
        try {
            User client = userRepo.findByEmail(auth.getName()).orElseThrow();
            String note = body.get("note") != null ? body.get("note").toString() : null;
            mealPlanService.reportFoodItemDeviation(foodItemId, client.getId(), note);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                e.getMessage() != null ? e.getMessage() : "Failed to log what you ate"));
        }
    }

    /** Dietitian: recalculate and save the actual nutrition for ONE food item's reported deviation. */
    @PutMapping("/food-item/{foodItemId}/actual-nutrition")
    public ResponseEntity<?> updateFoodItemActualNutrition(@PathVariable Long foodItemId,
                                                    @RequestBody Map<String, Object> body,
                                                    Authentication auth) {
        try {
            User dietitian = userRepo.findByEmail(auth.getName()).orElseThrow();
            Integer calories = body.get("calories") != null ? (int) Double.parseDouble(body.get("calories").toString()) : 0;
            Double protein = body.get("protein") != null ? Double.parseDouble(body.get("protein").toString()) : 0.0;
            Double carbs = body.get("carbs") != null ? Double.parseDouble(body.get("carbs").toString()) : 0.0;
            Double fat = body.get("fat") != null ? Double.parseDouble(body.get("fat").toString()) : 0.0;
            Double fiber = body.get("fiber") != null ? Double.parseDouble(body.get("fiber").toString()) : 0.0;
            mealPlanService.updateFoodItemActualNutrition(foodItemId, dietitian.getId(), calories, protein, carbs, fat, fiber);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                e.getMessage() != null ? e.getMessage() : "Failed to update nutrition"));
        }
    }

    private Map<String, Object> toDto(MealPlan plan) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", plan.getId());
        dto.put("planDate", plan.getPlanDate() != null ? plan.getPlanDate().toString() : null);
        dto.put("planName", plan.getPlanName());
        dto.put("clientId", plan.getClient() != null ? plan.getClient().getId() : null);
        dto.put("clientName", plan.getClient() != null ? plan.getClient().getFullName() : null);

        int totalCal = 0; double totalPro = 0, totalCarb = 0, totalFat = 0, totalFib = 0;
        List<Map<String, Object>> mealDtos = new ArrayList<>();
        if (plan.getMeals() != null) {
            for (Meal m : plan.getMeals()) {
                Map<String, Object> mDto = new LinkedHashMap<>();
                mDto.put("id", m.getId());
                mDto.put("mealType", m.getMealType() != null ? m.getMealType().name() : null);
                mDto.put("mealName", m.getMealName());
                mDto.put("completed", m.isCompleted());
                mDto.put("totalCalories", m.getTotalCalories() != null ? m.getTotalCalories() : 0);
                mDto.put("totalProtein", m.getTotalProtein() != null ? m.getTotalProtein() : 0.0);
                mDto.put("totalCarbs", m.getTotalCarbs() != null ? m.getTotalCarbs() : 0.0);
                mDto.put("totalFat", m.getTotalFat() != null ? m.getTotalFat() : 0.0);
                mDto.put("totalFiber", m.getTotalFiber() != null ? m.getTotalFiber() : 0.0);

                List<Map<String, Object>> foodDtos = new ArrayList<>();
                // Rolled up from food-item level: true if ANY item in the meal has a
                // client-reported deviation, and the meal's "current" nutrition (actual where a
                // dietitian has reviewed an item's deviation, planned otherwise) summed per item.
                boolean mealHasDeviation = false, mealDeviationReviewed = true;
                boolean anyReviewed = false;
                int curCal = 0; double curPro = 0, curCarb = 0, curFat = 0, curFib = 0;
                if (m.getFoodItems() != null) {
                    for (FoodItem fi : m.getFoodItems()) {
                        Map<String, Object> fDto = new LinkedHashMap<>();
                        fDto.put("id", fi.getId());
                        fDto.put("foodName", fi.getFoodName());
                        fDto.put("quantity", fi.getQuantity());
                        fDto.put("quantityUnit", fi.getQuantityUnit());
                        fDto.put("calories", fi.getCalories() != null ? fi.getCalories() : 0);
                        fDto.put("protein", fi.getProtein() != null ? fi.getProtein() : 0.0);
                        fDto.put("carbohydrates", fi.getCarbohydrates() != null ? fi.getCarbohydrates() : 0.0);
                        fDto.put("fat", fi.getFat() != null ? fi.getFat() : 0.0);
                        fDto.put("fiber", fi.getFiber() != null ? fi.getFiber() : 0.0);
                        fDto.put("clientNote", fi.getClientNote());
                        fDto.put("hasDeviation", fi.isHasDeviation());
                        fDto.put("deviationReviewed", fi.isDeviationReviewed());
                        fDto.put("actualCalories", fi.getActualCalories());
                        fDto.put("actualProtein", fi.getActualProtein());
                        fDto.put("actualCarbs", fi.getActualCarbs());
                        fDto.put("actualFat", fi.getActualFat());
                        fDto.put("actualFiber", fi.getActualFiber());
                        foodDtos.add(fDto);

                        if (fi.isHasDeviation()) {
                            mealHasDeviation = true;
                            if (!fi.isDeviationReviewed()) mealDeviationReviewed = false;
                        }
                        if (fi.getActualCalories() != null) anyReviewed = true;
                        curCal += (fi.getActualCalories() != null ? fi.getActualCalories() : (fi.getCalories() != null ? fi.getCalories() : 0));
                        curPro += (fi.getActualProtein() != null ? fi.getActualProtein() : (fi.getProtein() != null ? fi.getProtein() : 0.0));
                        curCarb += (fi.getActualCarbs() != null ? fi.getActualCarbs() : (fi.getCarbohydrates() != null ? fi.getCarbohydrates() : 0.0));
                        curFat += (fi.getActualFat() != null ? fi.getActualFat() : (fi.getFat() != null ? fi.getFat() : 0.0));
                        curFib += (fi.getActualFiber() != null ? fi.getActualFiber() : (fi.getFiber() != null ? fi.getFiber() : 0.0));
                    }
                }
                mDto.put("hasDeviation", mealHasDeviation);
                mDto.put("deviationReviewed", mealHasDeviation && mealDeviationReviewed);
                // Only surface a meal-level "actual" figure once at least one item has been
                // reviewed — otherwise the frontend's `actualX ?? totalX` fallback shows planned.
                mDto.put("actualCalories", anyReviewed ? curCal : null);
                mDto.put("actualProtein", anyReviewed ? curPro : null);
                mDto.put("actualCarbs", anyReviewed ? curCarb : null);
                mDto.put("actualFat", anyReviewed ? curFat : null);
                mDto.put("actualFiber", anyReviewed ? curFib : null);
                mDto.put("foodItems", foodDtos);
                mealDtos.add(mDto);
                totalCal += (anyReviewed ? curCal : (m.getTotalCalories() != null ? m.getTotalCalories() : 0));
                totalPro += (anyReviewed ? curPro : (m.getTotalProtein() != null ? m.getTotalProtein() : 0.0));
                totalCarb += (anyReviewed ? curCarb : (m.getTotalCarbs() != null ? m.getTotalCarbs() : 0.0));
                totalFat += (anyReviewed ? curFat : (m.getTotalFat() != null ? m.getTotalFat() : 0.0));
                totalFib += (anyReviewed ? curFib : (m.getTotalFiber() != null ? m.getTotalFiber() : 0.0));
            }
        }
        dto.put("meals", mealDtos);
        dto.put("totalCalories", totalCal);
        dto.put("totalProtein", totalPro);
        dto.put("totalCarbs", totalCarb);
        dto.put("totalFat", totalFat);
        dto.put("totalFiber", totalFib);
        return dto;
    }
}