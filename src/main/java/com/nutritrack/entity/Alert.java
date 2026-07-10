package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="alerts")
public class Alert {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="client_id",nullable=false) private User client;
    @ManyToOne @JoinColumn(name="dietitian_id",nullable=false) private User dietitian;
    @Enumerated(EnumType.STRING) private AlertType alertType;
    private String message;
    @Column(name="is_read") private boolean read;
    private Instant createdAt;
    @PrePersist protected void onCreate(){createdAt=Instant.now();read=false;}
    public enum AlertType{MISSED_MEALS,LOW_PROTEIN,LOW_CALORIES,LOW_COMPLIANCE,DIET_DEVIATION}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getClient(){return client;} public void setClient(User v){client=v;}
    public User getDietitian(){return dietitian;} public void setDietitian(User v){dietitian=v;}
    public AlertType getAlertType(){return alertType;} public void setAlertType(AlertType v){alertType=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public boolean isRead(){return read;} public void setRead(boolean v){read=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
}
