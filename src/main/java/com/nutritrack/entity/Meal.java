package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity @Table(name="meals")
public class Meal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="meal_plan_id",nullable=false) private MealPlan mealPlan;
    @Enumerated(EnumType.STRING) private MealType mealType;
    private String mealName; private boolean completed;
    @OneToMany(mappedBy="meal",cascade=CascadeType.ALL,orphanRemoval=true) private List<FoodItem> foodItems;
    private Integer totalCalories; private Double totalProtein; private Double totalCarbs;
    private Double totalFat; private Double totalFiber;

    // --- Client-reported deviation from the planned meal ---
    // Free-text note the client writes when they ate something different from what was planned.
    @Column(length = 1000) private String clientNote;
    private boolean hasDeviation;
    // True once the dietitian has recalculated nutrition for the deviation and it's reflected in logs.
    private boolean deviationReviewed;
    private LocalDateTime deviationLoggedAt;
    private LocalDateTime deviationReviewedAt;

    // Dietitian-recalculated actual nutrition for what the client really ate (overrides the
    // planned totals above when present). Null until the dietitian reviews a reported deviation.
    private Integer actualCalories; private Double actualProtein; private Double actualCarbs;
    private Double actualFat; private Double actualFiber;

    public enum MealType{EARLY_MORNING,BREAKFAST,MID_MORNING,LUNCH,EVENING_SNACK,DINNER,BEDTIME}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public MealPlan getMealPlan(){return mealPlan;} public void setMealPlan(MealPlan v){mealPlan=v;}
    public MealType getMealType(){return mealType;} public void setMealType(MealType v){mealType=v;}
    public String getMealName(){return mealName;} public void setMealName(String v){mealName=v;}
    public boolean isCompleted(){return completed;} public void setCompleted(boolean v){completed=v;}
    public List<FoodItem> getFoodItems(){return foodItems;} public void setFoodItems(List<FoodItem> v){foodItems=v;}
    public Integer getTotalCalories(){return totalCalories;} public void setTotalCalories(Integer v){totalCalories=v;}
    public Double getTotalProtein(){return totalProtein;} public void setTotalProtein(Double v){totalProtein=v;}
    public Double getTotalCarbs(){return totalCarbs;} public void setTotalCarbs(Double v){totalCarbs=v;}
    public Double getTotalFat(){return totalFat;} public void setTotalFat(Double v){totalFat=v;}
    public Double getTotalFiber(){return totalFiber;} public void setTotalFiber(Double v){totalFiber=v;}

    public String getClientNote(){return clientNote;} public void setClientNote(String v){clientNote=v;}
    public boolean isHasDeviation(){return hasDeviation;} public void setHasDeviation(boolean v){hasDeviation=v;}
    public boolean isDeviationReviewed(){return deviationReviewed;} public void setDeviationReviewed(boolean v){deviationReviewed=v;}
    public LocalDateTime getDeviationLoggedAt(){return deviationLoggedAt;} public void setDeviationLoggedAt(LocalDateTime v){deviationLoggedAt=v;}
    public LocalDateTime getDeviationReviewedAt(){return deviationReviewedAt;} public void setDeviationReviewedAt(LocalDateTime v){deviationReviewedAt=v;}
    public Integer getActualCalories(){return actualCalories;} public void setActualCalories(Integer v){actualCalories=v;}
    public Double getActualProtein(){return actualProtein;} public void setActualProtein(Double v){actualProtein=v;}
    public Double getActualCarbs(){return actualCarbs;} public void setActualCarbs(Double v){actualCarbs=v;}
    public Double getActualFat(){return actualFat;} public void setActualFat(Double v){actualFat=v;}
    public Double getActualFiber(){return actualFiber;} public void setActualFiber(Double v){actualFiber=v;}
}
