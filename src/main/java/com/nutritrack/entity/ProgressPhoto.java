package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="progress_photos")
public class ProgressPhoto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="client_id", nullable=false)
    private User client;

    @Lob @Column(columnDefinition="BYTEA") private byte[] imageData;
    private String imageType; // e.g. "image/jpeg"

    private String label; // "Before", "After", or a custom note
    private LocalDate photoDate;
    private LocalDateTime uploadedAt;

    @PrePersist protected void onCreate() { uploadedAt = LocalDateTime.now(); }

    public Long getId() { return id; }               public void setId(Long v) { id = v; }
    public User getClient() { return client; }       public void setClient(User v) { client = v; }
    public byte[] getImageData() { return imageData; } public void setImageData(byte[] v) { imageData = v; }
    public String getImageType() { return imageType; } public void setImageType(String v) { imageType = v; }
    public String getLabel() { return label; }       public void setLabel(String v) { label = v; }
    public LocalDate getPhotoDate() { return photoDate; } public void setPhotoDate(LocalDate v) { photoDate = v; }
    public LocalDateTime getUploadedAt() { return uploadedAt; } public void setUploadedAt(LocalDateTime v) { uploadedAt = v; }
}
