package com.nutritrack.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String username;
    @Column(nullable=false,unique=true) private String email;
    @Column(nullable=false) private String password;
    private String fullName; private String phoneNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
    private LocalDateTime createdAt;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // --- Brute-force login protection ---
    // Consecutive bad-password attempts since the last successful login or lock expiry.
    private int failedLoginAttempts;
    // Set when failedLoginAttempts hits the threshold; null/past = not currently locked.
    private LocalDateTime lockedUntil;
    // How many times in a row this account has been locked out; used to double the wait
    // each time (1 min, 2 min, 4 min...) so repeated attacks get progressively slower.
    private int lockoutStreak;

    @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();}
    public enum Role{DIETITIAN,CLIENT}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getPhoneNumber(){return phoneNumber;} public void setPhoneNumber(String v){phoneNumber=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public String getResetToken(){return resetToken;} public void setResetToken(String v){resetToken=v;}
    public LocalDateTime getResetTokenExpiry(){return resetTokenExpiry;} public void setResetTokenExpiry(LocalDateTime v){resetTokenExpiry=v;}
    public int getFailedLoginAttempts(){return failedLoginAttempts;} public void setFailedLoginAttempts(int v){failedLoginAttempts=v;}
    public LocalDateTime getLockedUntil(){return lockedUntil;} public void setLockedUntil(LocalDateTime v){lockedUntil=v;}
    public int getLockoutStreak(){return lockoutStreak;} public void setLockoutStreak(int v){lockoutStreak=v;}
}

