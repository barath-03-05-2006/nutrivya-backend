package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDate; import java.time.LocalDateTime; import java.util.List;
@Entity @Table(name="meal_plans")
public class MealPlan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="client_id",nullable=false) private User client;
    @ManyToOne @JoinColumn(name="dietitian_id",nullable=false) private User dietitian;
    private LocalDate planDate; private String planName;
    @OneToMany(mappedBy="mealPlan",cascade=CascadeType.ALL,orphanRemoval=true) private List<Meal> meals;
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getClient(){return client;} public void setClient(User v){client=v;}
    public User getDietitian(){return dietitian;} public void setDietitian(User v){dietitian=v;}
    public LocalDate getPlanDate(){return planDate;} public void setPlanDate(LocalDate v){planDate=v;}
    public String getPlanName(){return planName;} public void setPlanName(String v){planName=v;}
    public List<Meal> getMeals(){return meals;} public void setMeals(List<Meal> v){meals=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
