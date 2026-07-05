package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="progress_notes")
public class ProgressNote {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="client_id",nullable=false) private User client;
    @ManyToOne @JoinColumn(name="dietitian_id",nullable=false) private User dietitian;
    @Column(columnDefinition="TEXT") private String note;
    private LocalDateTime createdAt;
    @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public User getClient(){return client;} public void setClient(User v){client=v;}
    public User getDietitian(){return dietitian;} public void setDietitian(User v){dietitian=v;}
    public String getNote(){return note;} public void setNote(String v){note=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
