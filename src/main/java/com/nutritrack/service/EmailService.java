package com.nutritrack.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Shared email utility used by all services that send emails.
 * Provides a consistent Nutrivya-branded HTML wrapper.
 */
@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;
    @Value("${spring.mail.username}") private String fromEmail;

    public void send(String to, String subject, String bodyHtml) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, "Nutrivya");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(wrap(bodyHtml), true);
            mailSender.send(msg);
        } catch (Exception e) {
            // Log but don't crash — email failure should never break the main flow
            System.err.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    /** Wraps any body HTML in the standard Nutrivya branded shell. */
    public static String wrap(String body) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
            "<body style='margin:0;padding:0;background:#F8FAFC;font-family:Inter,Arial,sans-serif'>" +
            "<div style='max-width:600px;margin:40px auto;background:#fff;border-radius:16px;" +
            "box-shadow:0 4px 24px rgba(0,0,0,0.08);overflow:hidden'>" +
            // Header
            "<div style='background:linear-gradient(135deg,#2563EB,#1D4ED8);padding:28px 32px;text-align:center'>" +
            "<div style='display:inline-flex;align-items:center;gap:10px'>" +
            "<div style='width:38px;height:38px;background:rgba(255,255,255,0.2);border-radius:10px;" +
            "display:inline-flex;align-items:center;justify-content:center;font-size:20px'>🥗</div>" +
            "<span style='color:#fff;font-size:22px;font-weight:800;vertical-align:middle'>Nutrivya</span></div>" +
            "<p style='color:#BFDBFE;margin:6px 0 0;font-size:13px'>Personalised Nutrition Planning</p></div>" +
            // Body
            "<div style='padding:32px'>" + body + "</div>" +
            // Footer
            "<div style='background:#F8FAFC;border-top:1px solid #E2E8F0;padding:18px 32px;text-align:center'>" +
            "<p style='margin:0;color:#94A3B8;font-size:12px'>© 2025 Nutrivya · You are receiving this because you have an account on Nutrivya.</p>" +
            "</div></div></body></html>";
    }

    /** Reusable styled button */
    public static String btn(String url, String label) {
        return "<a href='" + url + "' style='display:inline-block;background:linear-gradient(135deg,#2563EB,#1D4ED8);" +
            "color:#fff;text-decoration:none;padding:13px 28px;border-radius:10px;font-weight:700;" +
            "font-size:14px;margin:16px 0'>" + label + "</a>";
    }

    /** Reusable stat box — color is a hex like #2563EB */
    public static String statBox(String label, String value, String color) {
        return "<div style='flex:1;min-width:100px;background:#F8FAFC;border:1px solid #E2E8F0;" +
            "border-radius:10px;padding:14px;text-align:center;border-top:3px solid " + color + "'>" +
            "<div style='font-size:11px;color:#94A3B8;text-transform:uppercase;letter-spacing:.06em;margin-bottom:4px'>" +
            label + "</div>" +
            "<div style='font-size:20px;font-weight:800;color:" + color + "'>" + value + "</div></div>";
    }
}
