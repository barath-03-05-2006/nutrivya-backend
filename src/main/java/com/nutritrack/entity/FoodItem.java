package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="food_items")
public class FoodItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="meal_id",nullable=false) private Meal meal;
    private String foodName; private String quantity; private String quantityUnit;
    private Integer calories; private Double protein; private Double carbohydrates;
    private Double fat; private Double fiber;

    // --- Client-reported deviation for this specific food item ---
    @Column(length = 1000) private String clientNote;
    private boolean hasDeviation;
    // True once the dietitian has recalculated nutrition for the deviation and it's reflected in logs.
    private boolean deviationReviewed;
    private LocalDateTime deviationLoggedAt;
    private LocalDateTime deviationReviewedAt;

    // Dietitian-recalculated actual nutrition for what the client really ate (overrides the
    // planned values above when present). Null until the dietitian reviews a reported deviation.
    private Integer actualCalories; private Double actualProtein; private Double actualCarbs;
    private Double actualFat; private Double actualFiber;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Meal getMeal(){return meal;} public void setMeal(Meal v){meal=v;}
    public String getFoodName(){return foodName;} public void setFoodName(String v){foodName=v;}
    public String getQuantity(){return quantity;} public void setQuantity(String v){quantity=v;}
    public String getQuantityUnit(){return quantityUnit;} public void setQuantityUnit(String v){quantityUnit=v;}
    public Integer getCalories(){return calories;} public void setCalories(Integer v){calories=v;}
    public Double getProtein(){return protein;} public void setProtein(Double v){protein=v;}
    public Double getCarbohydrates(){return carbohydrates;} public void setCarbohydrates(Double v){carbohydrates=v;}
    public Double getFat(){return fat;} public void setFat(Double v){fat=v;}
    public Double getFiber(){return fiber;} public void setFiber(Double v){fiber=v;}

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