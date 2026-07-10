package com.nutritrack.service;

import com.nutritrack.entity.User;
import com.nutritrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired private UserRepository userRepo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder encoder;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetLink(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepo.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Nutrivya — Reset Your Password");
            helper.setText(buildEmailHtml(user.getFullName(), resetLink), true);
            mailSender.send(msg);
        } catch (Exception e) {
            // NEW
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public void resetPassword(String token, String newPassword) {
        if(newPassword == null || newPassword.length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");
        if(!newPassword.matches(".*[a-zA-Z].*") || !newPassword.matches(".*[0-9].*"))
            throw new RuntimeException("Password must contain at least one letter and one number");
        User user = userRepo.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);
    }

    private String buildEmailHtml(String name, String resetLink) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#F8FAFC;font-family:Inter,Arial,sans-serif'>" +
               "<div style='max-width:520px;margin:40px auto;background:#fff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden'>" +
               "<div style='background:linear-gradient(135deg,#2563EB,#1D4ED8);padding:32px;text-align:center'>" +
               "<h1 style='color:#fff;margin:0;font-size:24px;font-weight:800'>🥗 Nutrivya</h1>" +
               "<p style='color:#BFDBFE;margin:8px 0 0;font-size:14px'>Your nutrition journey companion</p></div>" +
               "<div style='padding:36px 32px'>" +
               "<h2 style='margin:0 0 8px;color:#0F172A;font-size:20px'>Reset your password</h2>" +
               "<p style='color:#475569;font-size:14px;margin:0 0 24px'>Hi " + (name != null ? name : "there") + ", we received a request to reset your Nutrivya password.</p>" +
               "<a href='" + resetLink + "' style='display:inline-block;background:linear-gradient(135deg,#2563EB,#1D4ED8);color:#fff;text-decoration:none;padding:14px 32px;border-radius:10px;font-weight:700;font-size:15px'>Reset Password</a>" +
               "<p style='color:#94A3B8;font-size:12px;margin:24px 0 0'>This link expires in <strong>1 hour</strong>. If you didn't request this, you can safely ignore this email.</p>" +
               "<p style='color:#CBD5E1;font-size:11px;margin:8px 0 0'>Or copy this link: <span style='color:#2563EB'>" + resetLink + "</span></p>" +
               "</div></div></body></html>";
    }
}
