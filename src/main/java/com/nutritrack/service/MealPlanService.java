package com.nutritrack.service;

import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MealPlanService {

    @Autowired private MealPlanRepository mealPlanRepo;
    @Autowired private MealRepository mealRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private DailyLogRepository dailyLogRepo;
    @Autowired private AlertRepository alertRepo;
    @Autowired private EmailService emailService;
    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public MealPlan createMealPlan(Map<String, Object> req, Long dietitianId) {
        Object clientIdObj = req.get("clientId");
        if (clientIdObj == null) throw new RuntimeException("clientId is required");
        Long clientId = Long.valueOf(clientIdObj.toString());

        User client = userRepo.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        User dietitian = userRepo.findById(dietitianId)
                .orElseThrow(() -> new RuntimeException("Dietitian not found"));

        Object planDateObj = req.get("planDate");
        if (planDateObj == null || planDateObj.toString().trim().isEmpty())
            throw new RuntimeException("planDate is required");
        LocalDate planDate = LocalDate.parse(planDateObj.toString().trim());

        Object planNameObj = req.get("planName");
        String planName = (planNameObj != null && !planNameObj.toString().trim().isEmpty())
                ? planNameObj.toString().trim() : "Meal Plan";

        MealPlan plan = new MealPlan();
        plan.setClient(client);
        plan.setDietitian(dietitian);
        plan.setPlanDate(planDate);
        plan.setPlanName(planName);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mealsData = (List<Map<String, Object>>) req.get("meals");
        if (mealsData == null || mealsData.isEmpty())
            throw new RuntimeException("At least one meal is required");

        List<Meal> meals = new ArrayList<>();
        for (Map<String, Object> md : mealsData) {
            Object mtObj = md.get("mealType");
            if (mtObj == null) throw new RuntimeException("mealType is required");

            Meal meal = new Meal();
            meal.setMealPlan(plan);
            meal.setMealType(Meal.MealType.valueOf(mtObj.toString()));

            Object mnObj = md.get("mealName");
            String mealName = (mnObj != null && !mnObj.toString().trim().isEmpty())
                    ? mnObj.toString().trim()
                    : mtObj.toString().replace("_", " ");
            meal.setMealName(mealName);
            meal.setCompleted(false);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fiData = (List<Map<String, Object>>) md.get("foodItems");
            List<FoodItem> items = new ArrayList<>();
            int tc = 0; double tp = 0, tca = 0, tf = 0, tfi = 0;

            if (fiData != null) {
                for (Map<String, Object> fi : fiData) {
                    Object fnObj = fi.get("foodName");
                    if (fnObj == null || fnObj.toString().trim().isEmpty()) continue;

                    FoodItem item = new FoodItem();
                    item.setMeal(meal);
                    item.setFoodName(fnObj.toString().trim());
                    item.setQuantity(fi.getOrDefault("quantity", "").toString());
                    item.setQuantityUnit(fi.getOrDefault("quantityUnit", "g").toString());
                    int cal = parseIntSafe(fi.get("calories"));
                    double pro = parseDoubleSafe(fi.get("protein"));
                    double carb = parseDoubleSafe(fi.get("carbohydrates"));
                    double fat = parseDoubleSafe(fi.get("fat"));
                    double fib = parseDoubleSafe(fi.get("fiber"));
                    item.setCalories(cal); item.setProtein(pro);
                    item.setCarbohydrates(carb); item.setFat(fat); item.setFiber(fib);
                    items.add(item);
                    tc += cal; tp += pro; tca += carb; tf += fat; tfi += fib;
                }
            }

            meal.setFoodItems(items);
            meal.setTotalCalories(tc); meal.setTotalProtein(tp);
            meal.setTotalCarbs(tca); meal.setTotalFat(tf); meal.setTotalFiber(tfi);
            meals.add(meal);
        }

        plan.setMeals(meals);
        MealPlan saved = mealPlanRepo.save(plan);

        // Update daily log
        DailyLog log = dailyLogRepo.findByClientAndLogDate(client, planDate)
                .orElseGet(() -> { DailyLog dl = new DailyLog(); dl.setClient(client); dl.setLogDate(planDate); return dl; });
        log.setMealsAssigned(meals.size());
        dailyLogRepo.save(log);

        // Notify the client by email that a new plan was assigned
        try { sendPlanAssignedEmail(client, dietitian, saved); }
        catch (Exception e) { System.err.println("[MealPlan] Email failed: " + e.getMessage()); }

        return saved;
    }

    @Transactional(readOnly = true)
    public List<MealPlan> getClientMealPlans(Long clientId) {
        List<MealPlan> plans = mealPlanRepo.findByClientIdOrderByPlanDateDesc(clientId);
        // Force load lazy collections
        plans.forEach(p -> {
            if (p.getMeals() != null) {
                p.getMeals().forEach(m -> {
                    if (m.getFoodItems() != null) m.getFoodItems().size();
                });
            }
        });
        return plans;
    }

    @Transactional(readOnly = true)
    public Optional<MealPlan> getMealPlanByDate(Long clientId, LocalDate date) {
        User client = userRepo.findById(clientId).orElseThrow();
        Optional<MealPlan> planOpt = mealPlanRepo.findByClientAndPlanDate(client, date);
        // Force load lazy collections
        planOpt.ifPresent(p -> {
            if (p.getMeals() != null) {
                p.getMeals().forEach(m -> {
                    if (m.getFoodItems() != null) m.getFoodItems().size();
                });
            }
        });
        return planOpt;
    }

    @Transactional
    public void completeMeal(Long mealId, Long clientId) {
        Meal meal = mealRepo.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        if (!meal.isCompleted()) {
            meal.setCompleted(true);
            mealRepo.save(meal);
            User client = userRepo.findById(clientId).orElseThrow();
            LocalDate date = meal.getMealPlan().getPlanDate();
            DailyLog log = dailyLogRepo.findByClientAndLogDate(client, date)
                    .orElseGet(() -> { DailyLog dl = new DailyLog(); dl.setClient(client); dl.setLogDate(date); return dl; });
            // Prefer dietitian-reviewed actual nutrition (if a deviation was already recalculated
            // before the meal was ticked complete) over the originally planned totals.
            log.setCaloriesConsumed(log.getCaloriesConsumed() + nz(meal.getActualCalories() != null ? meal.getActualCalories() : meal.getTotalCalories()));
            log.setProteinConsumed(log.getProteinConsumed() + nz(meal.getActualProtein() != null ? meal.getActualProtein() : meal.getTotalProtein()));
            log.setCarbsConsumed(log.getCarbsConsumed() + nz(meal.getActualCarbs() != null ? meal.getActualCarbs() : meal.getTotalCarbs()));
            log.setFatConsumed(log.getFatConsumed() + nz(meal.getActualFat() != null ? meal.getActualFat() : meal.getTotalFat()));
            log.setFiberConsumed(log.getFiberConsumed() + nz(meal.getActualFiber() != null ? meal.getActualFiber() : meal.getTotalFiber()));
            log.setMealsCompleted(log.getMealsCompleted() + 1);
            dailyLogRepo.save(log);
        }
    }

    /**
     * Client reports that they ate something different from what the dietitian planned for
     * this meal. Just records the note — nutrition stays as originally planned/logged until
     * the dietitian reviews it and recalculates the actual values below.
     */
    @Transactional
    public void reportDeviation(Long mealId, Long clientId, String note) {
        Meal meal = mealRepo.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        User client = meal.getMealPlan().getClient();
        if (client == null || !client.getId().equals(clientId)) {
            throw new RuntimeException("Not authorized to update this meal");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new RuntimeException("Please describe what you actually ate");
        }
        meal.setClientNote(note.trim());
        meal.setHasDeviation(true);
        meal.setDeviationReviewed(false);
        meal.setDeviationLoggedAt(LocalDateTime.now());
        mealRepo.save(meal);

        // Let the dietitian know there's something to review.
        User dietitian = meal.getMealPlan().getDietitian();
        if (dietitian != null) {
            Alert a = new Alert();
            a.setClient(client);
            a.setDietitian(dietitian);
            a.setAlertType(Alert.AlertType.DIET_DEVIATION);
            a.setMessage((client.getFullName() != null ? client.getFullName() : "A client")
                    + " logged a different meal than planned ("
                    + (meal.getMealName() != null ? meal.getMealName() : meal.getMealType())
                    + ") — recalculate nutrition to keep their log accurate.");
            a.setRead(false);
            alertRepo.save(a);
        }
    }

    /**
     * Dietitian reviews a client's reported deviation and enters the recalculated nutrition
     * for what was actually eaten. This replaces whatever was previously counted for this meal
     * in the client's daily log (planned totals, or a prior actual figure) with the new values,
     * and marks the meal completed since the client has now logged what they ate.
     */
    @Transactional
    public void updateActualNutrition(Long mealId, Long dietitianId, Integer calories,
            Double protein, Double carbs, Double fat, Double fiber) {
        Meal meal = mealRepo.findById(mealId)
                .orElseThrow(() -> new RuntimeException("Meal not found"));
        MealPlan plan = meal.getMealPlan();
        if (plan.getDietitian() == null || !plan.getDietitian().getId().equals(dietitianId)) {
            throw new RuntimeException("Not authorized to update this meal");
        }

        User client = plan.getClient();
        LocalDate date = plan.getPlanDate();
        DailyLog log = dailyLogRepo.findByClientAndLogDate(client, date)
                .orElseGet(() -> { DailyLog dl = new DailyLog(); dl.setClient(client); dl.setLogDate(date); return dl; });

        // Whatever is currently sitting in the daily log for this meal (0 if it was never
        // marked complete yet).
        boolean wasCompleted = meal.isCompleted();
        int prevCal = wasCompleted ? nz(meal.getActualCalories() != null ? meal.getActualCalories() : meal.getTotalCalories()) : 0;
        double prevPro = wasCompleted ? nz(meal.getActualProtein() != null ? meal.getActualProtein() : meal.getTotalProtein()) : 0;
        double prevCarb = wasCompleted ? nz(meal.getActualCarbs() != null ? meal.getActualCarbs() : meal.getTotalCarbs()) : 0;
        double prevFat = wasCompleted ? nz(meal.getActualFat() != null ? meal.getActualFat() : meal.getTotalFat()) : 0;
        double prevFib = wasCompleted ? nz(meal.getActualFiber() != null ? meal.getActualFiber() : meal.getTotalFiber()) : 0;

        meal.setActualCalories(calories);
        meal.setActualProtein(protein);
        meal.setActualCarbs(carbs);
        meal.setActualFat(fat);
        meal.setActualFiber(fiber);
        meal.setDeviationReviewed(true);
        meal.setDeviationReviewedAt(LocalDateTime.now());

        boolean justCompleted = !meal.isCompleted();
        meal.setCompleted(true);
        mealRepo.save(meal);

        log.setCaloriesConsumed(log.getCaloriesConsumed() - prevCal + nz(calories));
        log.setProteinConsumed(log.getProteinConsumed() - prevPro + nz(protein));
        log.setCarbsConsumed(log.getCarbsConsumed() - prevCarb + nz(carbs));
        log.setFatConsumed(log.getFatConsumed() - prevFat + nz(fat));
        log.setFiberConsumed(log.getFiberConsumed() - prevFib + nz(fiber));
        if (justCompleted) log.setMealsCompleted(log.getMealsCompleted() + 1);
        dailyLogRepo.save(log);
    }

    private int nz(Integer v) { return v != null ? v : 0; }
    private double nz(Double v) { return v != null ? v : 0.0; }

    private void sendPlanAssignedEmail(User client, User dietitian, MealPlan plan) {
        String dietName = dietitian.getFullName() != null ? dietitian.getFullName() : "Your dietitian";
        String clientName = client.getFullName() != null ? client.getFullName().split(" ")[0] : "there";
        String planName = plan.getPlanName() != null ? plan.getPlanName() : "Meal Plan";
        String planDate = plan.getPlanDate() != null ? plan.getPlanDate().toString() : "";

        String body =
            "<h2 style='margin:0 0 8px;color:#0F172A;font-size:20px'>New Meal Plan Assigned! 🎉</h2>" +
            "<p style='color:#475569;font-size:14px;margin:0 0 20px'>Hi " + client.getFullName() + ", " +
            dietName + " has created a new meal plan for you.</p>" +
            "<div style='background:#F0F6FF;border:1px solid #BFDBFE;border-radius:10px;padding:16px;margin-bottom:20px'>" +
            "<div style='font-size:13px;color:#94A3B8;margin-bottom:4px'>PLAN NAME</div>" +
            "<div style='font-size:18px;font-weight:700;color:#2563EB'>" + planName + "</div>" +
            (planDate.isEmpty() ? "" : "<div style='font-size:13px;color:#475569;margin-top:4px'>Starting " + planDate + "</div>") +
            "</div>" +
            "<p style='color:#475569;font-size:14px;margin:0 0 20px'>Log in to view your meals, track your nutrition, and mark each meal as complete throughout the day.</p>" +
            com.nutritrack.service.EmailService.btn(frontendUrl + "/client/meals", "View My Meal Plan") +
            "<p style='color:#94A3B8;font-size:12px;margin:20px 0 0'>Prepared by " + dietName + " · Nutrivya</p>";

        emailService.send(client.getEmail(), "New Meal Plan: " + planName + " — Nutrivya", body);
    }

    private int parseIntSafe(Object val) {
        if (val == null || val.toString().trim().isEmpty()) return 0;
        try { return (int) Double.parseDouble(val.toString().trim()); }
        catch (Exception e) { return 0; }
    }

    private double parseDoubleSafe(Object val) {
        if (val == null || val.toString().trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(val.toString().trim()); }
        catch (Exception e) { return 0.0; }
    }
}
