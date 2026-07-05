package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name="weight_logs")
public class WeightLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="client_id",nullable=false) private User client;
    private Double weight; private LocalDate logDate; private String notes;
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getClient(){return client;} public void setClient(User v){client=v;}
    public Double getWeight(){return weight;} public void setWeight(Double v){weight=v;}
    public LocalDate getLogDate(){return logDate;} public void setLogDate(LocalDate v){logDate=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
