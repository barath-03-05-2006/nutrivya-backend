package com.nutritrack.entity;
import jakarta.persistence.*;

@Entity @Table(name="food_database")
public class FoodDatabase {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String foodName; private String category;
    private String servingSize; private String servingUnit;
    private Double caloriesPer100g; private Double proteinPer100g;
    private Double carbsPer100g; private Double fatPer100g; private Double fiberPer100g;

    /**
     * Gram-equivalent for each supported unit, stored as JSON-like delimited string:
     * "g:1,ml:1,piece:50,cup:240,bowl:150,katori:150,tbsp:15,tsp:5"
     * Default fallback (if a food has no override) is handled in code via DEFAULT_UNIT_GRAMS.
     */
    private String unitGramsOverride;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getFoodName(){return foodName;} public void setFoodName(String v){foodName=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getServingSize(){return servingSize;} public void setServingSize(String v){servingSize=v;}
    public String getServingUnit(){return servingUnit;} public void setServingUnit(String v){servingUnit=v;}
    public Double getCaloriesPer100g(){return caloriesPer100g;} public void setCaloriesPer100g(Double v){caloriesPer100g=v;}
    public Double getProteinPer100g(){return proteinPer100g;} public void setProteinPer100g(Double v){proteinPer100g=v;}
    public Double getCarbsPer100g(){return carbsPer100g;} public void setCarbsPer100g(Double v){carbsPer100g=v;}
    public Double getFatPer100g(){return fatPer100g;} public void setFatPer100g(Double v){fatPer100g=v;}
    public Double getFiberPer100g(){return fiberPer100g;} public void setFiberPer100g(Double v){fiberPer100g=v;}
    public String getUnitGramsOverride(){return unitGramsOverride;} public void setUnitGramsOverride(String v){unitGramsOverride=v;}
}
