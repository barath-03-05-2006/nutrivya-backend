package com.nutritrack.entity;
import jakarta.persistence.*;
@Entity @Table(name="food_items")
public class FoodItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="meal_id",nullable=false) private Meal meal;
    private String foodName; private String quantity; private String quantityUnit;
    private Integer calories; private Double protein; private Double carbohydrates;
    private Double fat; private Double fiber;
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
}
