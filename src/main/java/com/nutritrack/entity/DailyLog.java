package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="daily_logs")
public class DailyLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="client_id",nullable=false) private User client;
    private LocalDate logDate;
    private Integer caloriesConsumed=0; private Double proteinConsumed=0.0;
    private Double carbsConsumed=0.0; private Double fatConsumed=0.0;
    private Double fiberConsumed=0.0; private Double waterIntake=0.0;
    private Integer mealsAssigned=0; private Integer mealsCompleted=0;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getClient(){return client;} public void setClient(User v){client=v;}
    public LocalDate getLogDate(){return logDate;} public void setLogDate(LocalDate v){logDate=v;}
    public Integer getCaloriesConsumed(){return caloriesConsumed!=null?caloriesConsumed:0;} public void setCaloriesConsumed(Integer v){caloriesConsumed=v;}
    public Double getProteinConsumed(){return proteinConsumed!=null?proteinConsumed:0.0;} public void setProteinConsumed(Double v){proteinConsumed=v;}
    public Double getCarbsConsumed(){return carbsConsumed!=null?carbsConsumed:0.0;} public void setCarbsConsumed(Double v){carbsConsumed=v;}
    public Double getFatConsumed(){return fatConsumed!=null?fatConsumed:0.0;} public void setFatConsumed(Double v){fatConsumed=v;}
    public Double getFiberConsumed(){return fiberConsumed!=null?fiberConsumed:0.0;} public void setFiberConsumed(Double v){fiberConsumed=v;}
    public Double getWaterIntake(){return waterIntake!=null?waterIntake:0.0;} public void setWaterIntake(Double v){waterIntake=v;}
    public Integer getMealsAssigned(){return mealsAssigned!=null?mealsAssigned:0;} public void setMealsAssigned(Integer v){mealsAssigned=v;}
    public Integer getMealsCompleted(){return mealsCompleted!=null?mealsCompleted:0;} public void setMealsCompleted(Integer v){mealsCompleted=v;}
}
