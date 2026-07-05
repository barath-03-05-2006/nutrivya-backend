package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="client_profiles")
public class ClientProfile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne @JoinColumn(name="user_id",nullable=false) private User user;
    @ManyToOne @JoinColumn(name="dietitian_id") private User dietitian;
    private Double currentWeight; private Double startingWeight; private Double goalWeight;
    private Double height; private Integer age; private String gender;
    private Integer targetCalories; private Integer targetProtein; private Integer targetCarbs;
    private Integer targetFat; private Integer targetFiber; private Integer targetWater;
    private LocalDate startDate;
    @Column(columnDefinition="TEXT") private String notes;
    // Questionnaire fields
    private Boolean medicalDiagnosis; private Boolean regularMedicine;
    private Boolean pastMedicalHistory; private Boolean medicalSurgery;
    private String workLifestyle; private String socialHabits;
    private String physicalActivityLevel; private Boolean supplements;
    private String dietType; private Boolean foodAllergy;
    @Column(columnDefinition="TEXT") private String foodAllergyDetails;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getUser(){return user;} public void setUser(User v){user=v;}
    public User getDietitian(){return dietitian;} public void setDietitian(User v){dietitian=v;}
    public Double getCurrentWeight(){return currentWeight;} public void setCurrentWeight(Double v){currentWeight=v;}
    public Double getStartingWeight(){return startingWeight;} public void setStartingWeight(Double v){startingWeight=v;}
    public Double getGoalWeight(){return goalWeight;} public void setGoalWeight(Double v){goalWeight=v;}
    public Double getHeight(){return height;} public void setHeight(Double v){height=v;}
    public Integer getAge(){return age;} public void setAge(Integer v){age=v;}
    public String getGender(){return gender;} public void setGender(String v){gender=v;}
    public Integer getTargetCalories(){return targetCalories;} public void setTargetCalories(Integer v){targetCalories=v;}
    public Integer getTargetProtein(){return targetProtein;} public void setTargetProtein(Integer v){targetProtein=v;}
    public Integer getTargetCarbs(){return targetCarbs;} public void setTargetCarbs(Integer v){targetCarbs=v;}
    public Integer getTargetFat(){return targetFat;} public void setTargetFat(Integer v){targetFat=v;}
    public Integer getTargetFiber(){return targetFiber;} public void setTargetFiber(Integer v){targetFiber=v;}
    public Integer getTargetWater(){return targetWater;} public void setTargetWater(Integer v){targetWater=v;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public Boolean getMedicalDiagnosis(){return medicalDiagnosis;} public void setMedicalDiagnosis(Boolean v){medicalDiagnosis=v;}
    public Boolean getRegularMedicine(){return regularMedicine;} public void setRegularMedicine(Boolean v){regularMedicine=v;}
    public Boolean getPastMedicalHistory(){return pastMedicalHistory;} public void setPastMedicalHistory(Boolean v){pastMedicalHistory=v;}
    public Boolean getMedicalSurgery(){return medicalSurgery;} public void setMedicalSurgery(Boolean v){medicalSurgery=v;}
    public String getWorkLifestyle(){return workLifestyle;} public void setWorkLifestyle(String v){workLifestyle=v;}
    public String getSocialHabits(){return socialHabits;} public void setSocialHabits(String v){socialHabits=v;}
    public String getPhysicalActivityLevel(){return physicalActivityLevel;} public void setPhysicalActivityLevel(String v){physicalActivityLevel=v;}
    public Boolean getSupplements(){return supplements;} public void setSupplements(Boolean v){supplements=v;}
    public String getDietType(){return dietType;} public void setDietType(String v){dietType=v;}
    public Boolean getFoodAllergy(){return foodAllergy;} public void setFoodAllergy(Boolean v){foodAllergy=v;}
    public String getFoodAllergyDetails(){return foodAllergyDetails;} public void setFoodAllergyDetails(String v){foodAllergyDetails=v;}
}
