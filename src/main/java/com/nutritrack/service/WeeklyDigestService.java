package com.nutritrack.service;

import com.nutritrack.entity.*;
import com.nutritrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sends a detailed weekly compliance digest to each dietitian every Monday morning.
 * Covers every client assigned to that dietitian — full 7-day breakdown.
 */
@Service
public class WeeklyDigestService {

    @Autowired private EmailService emailService;
    @Autowired private UserRepository userRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private DailyLogRepository dailyLogRepo;
    @Autowired private WeightLogRepository weightLogRepo;
    @Autowired private ProgressNoteRepository noteRepo;
    @Autowired private MealPlanRepository mealPlanRepo;

    @Value("${app.frontend.url:http://localhost:3000}") private String frontendUrl;

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("EEE, MMM d");
    private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("MMM d");

    /** Fires every Monday at 7:00 AM */
    @Scheduled(cron = "0 0 7 * * MON")
    public void sendWeeklyDigests() {
        List<User> dietitians = userRepo.findByRole(User.Role.DIETITIAN);
        dietitians.forEach(d -> {
            try { sendDigestForDietitian(d); }
            catch (Exception e) { System.err.println("[WeeklyDigest] Failed for " + d.getEmail() + ": " + e.getMessage()); }
        });
    }

    /** Public so it can be manually triggered via API for testing */
    public void sendDigestForDietitian(User dietitian) {
        List<ClientProfile> clients = profileRepo.findByDietitian(dietitian);
        if (clients.isEmpty()) return;

        LocalDate weekEnd   = LocalDate.now().minusDays(1); // yesterday
        LocalDate weekStart = weekEnd.minusDays(6);         // 7 days back

        String weekLabel = weekStart.format(WEEK_FMT) + " – " + weekEnd.format(WEEK_FMT);

        StringBuilder body = new StringBuilder();
        body.append("<h2 style='margin:0 0 6px;color:#0F172A;font-size:20px'>Weekly Client Report 📊</h2>")
            .append("<p style='color:#475569;font-size:14px;margin:0 0 4px'>Hi ").append(dietitian.getFullName()).append(",</p>")
            .append("<p style='color:#475569;font-size:14px;margin:0 0 24px'>Here's your full weekly overview for <strong>").append(weekLabel).append("</strong>. ")
            .append("This covers all ").append(clients.size()).append(" client").append(clients.size() > 1 ? "s" : "").append(" assigned to you.</p>");

        // Quick summary stats row
        long highCompliance  = clients.stream().filter(c -> weekCompliance(c.getUser().getId(), weekStart, weekEnd) >= 70).count();
        long lowCompliance   = clients.size() - highCompliance;
        body.append("<div style='display:flex;gap:10px;margin-bottom:24px;flex-wrap:wrap'>")
            .append(EmailService.statBox("Total Clients", String.valueOf(clients.size()), "#2563EB"))
            .append(EmailService.statBox("On Track (≥70%)", String.valueOf(highCompliance), "#22C55E"))
            .append(EmailService.statBox("Needs Attention", String.valueOf(lowCompliance), "#EF4444"))
            .append("</div>");

        // Per-client detailed section
        for (ClientProfile cp : clients) {
            body.append(buildClientSection(cp, weekStart, weekEnd));
        }

        body.append(EmailService.btn(frontendUrl + "/dietitian", "Open Nutrivya Dashboard"))
            .append("<p style='color:#94A3B8;font-size:12px;margin:20px 0 0'>This report is automatically generated every Monday. Use it to prepare for client check-ins.</p>");

        emailService.send(
            dietitian.getEmail(),
            "Weekly Client Report — " + weekLabel + " | Nutrivya",
            body.toString()
        );
    }

    private String buildClientSection(ClientProfile cp, LocalDate weekStart, LocalDate weekEnd) {
        Long clientId = cp.getUser().getId();
        String name   = cp.getUser().getFullName();

        List<DailyLog> logs = dailyLogRepo.findByRange(clientId, weekStart, weekEnd);
        double compliance   = weekCompliance(clientId, weekStart, weekEnd);
        boolean onTrack     = compliance >= 70;

        // Aggregate stats
        double avgCal    = logs.stream().mapToDouble(l -> l.getCaloriesConsumed() != null ? l.getCaloriesConsumed() : 0).average().orElse(0);
        double avgPro    = logs.stream().mapToDouble(l -> l.getProteinConsumed() != null ? l.getProteinConsumed() : 0).average().orElse(0);
        double avgCarb   = logs.stream().mapToDouble(l -> l.getCarbsConsumed() != null ? l.getCarbsConsumed() : 0).average().orElse(0);
        double avgFat    = logs.stream().mapToDouble(l -> l.getFatConsumed() != null ? l.getFatConsumed() : 0).average().orElse(0);
        double avgWater  = logs.stream().mapToDouble(l -> l.getWaterIntake() != null ? l.getWaterIntake() : 0).average().orElse(0);
        int totalMealsA  = logs.stream().mapToInt(l -> l.getMealsAssigned() != null ? l.getMealsAssigned() : 0).sum();
        int totalMealsC  = logs.stream().mapToInt(l -> l.getMealsCompleted() != null ? l.getMealsCompleted() : 0).sum();

        // Weight change
        List<WeightLog> weights = weightLogRepo.findByClientIdOrderByLogDateAsc(clientId);
        String weightChange = "—";
        if (weights.size() >= 2) {
            double wStart = weights.get(0).getWeight();
            double wEnd   = weights.get(weights.size() - 1).getWeight();
            double change = wEnd - wStart;
            weightChange  = (change >= 0 ? "+" : "") + String.format("%.1f", change) + " kg";
        }
        String latestWeight = weights.isEmpty() ? "—" : String.format("%.1f", weights.get(weights.size()-1).getWeight()) + " kg";

        // Recent notes
        List<String> notes = noteRepo.findTop5ByClientIdOrderByCreatedAtDesc(clientId)
            .stream().map(ProgressNote::getNote).collect(Collectors.toList());

        String borderColor  = onTrack ? "#22C55E" : "#EF4444";
        String statusBadge  = onTrack
            ? "<span style='background:#DCFCE7;color:#16A34A;padding:2px 10px;border-radius:20px;font-size:12px;font-weight:700'>On Track ✓</span>"
            : "<span style='background:#FEE2E2;color:#DC2626;padding:2px 10px;border-radius:20px;font-size:12px;font-weight:700'>Needs Attention ⚠</span>";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='border:1px solid #E2E8F0;border-left:4px solid ").append(borderColor)
          .append(";border-radius:12px;padding:20px;margin-bottom:20px;background:#fff'>")

          // Client header
          .append("<div style='display:flex;align-items:center;gap:12px;margin-bottom:16px'>")
          .append("<div style='width:44px;height:44px;border-radius:50%;background:#2563EB;display:flex;align-items:center;justify-content:center;")
          .append("color:#fff;font-weight:800;font-size:18px;flex-shrink:0'>").append(name.charAt(0)).append("</div>")
          .append("<div><div style='font-size:16px;font-weight:700;color:#0F172A'>").append(name).append("</div>")
          .append("<div style='margin-top:4px'>").append(statusBadge).append("</div></div>")
          .append("<div style='margin-left:auto;text-align:right'>")
          .append("<div style='font-size:24px;font-weight:800;color:").append(borderColor).append("'>").append(String.format("%.0f", compliance)).append("%</div>")
          .append("<div style='font-size:11px;color:#94A3B8'>compliance</div>")
          .append("</div></div>")

          // Quick stat boxes
          .append("<div style='display:flex;gap:8px;margin-bottom:16px;flex-wrap:wrap'>")
          .append(EmailService.statBox("Avg Calories", String.format("%.0f", avgCal) + " kcal", "#2563EB"))
          .append(EmailService.statBox("Avg Protein", String.format("%.0f", avgPro) + "g", "#22C55E"))
          .append(EmailService.statBox("Avg Carbs", String.format("%.0f", avgCarb) + "g", "#F59E0B"))
          .append(EmailService.statBox("Avg Fat", String.format("%.0f", avgFat) + "g", "#EF4444"))
          .append(EmailService.statBox("Avg Water", String.format("%.0f", avgWater) + "ml", "#0EA5E9"))
          .append(EmailService.statBox("Weight", latestWeight + " (" + weightChange + ")", "#8B5CF6"))
          .append("</div>")

          // Meals summary
          .append("<div style='background:#F8FAFC;border-radius:8px;padding:12px;margin-bottom:14px;font-size:13px;color:#475569'>")
          .append("<strong>Meals:</strong> ").append(totalMealsC).append(" completed out of ").append(totalMealsA).append(" assigned this week")
          .append("</div>");

        // Day-by-day breakdown table
        sb.append("<div style='margin-bottom:14px'>")
          .append("<div style='font-size:12px;font-weight:700;color:#94A3B8;text-transform:uppercase;letter-spacing:.06em;margin-bottom:8px'>Day-by-Day Breakdown</div>")
          .append("<table style='width:100%;border-collapse:collapse;font-size:12px'>")
          .append("<tr style='background:#F8FAFC'>")
          .append("<th style='text-align:left;padding:6px 8px;color:#94A3B8;font-weight:600'>Day</th>")
          .append("<th style='text-align:right;padding:6px 8px;color:#94A3B8;font-weight:600'>Calories</th>")
          .append("<th style='text-align:right;padding:6px 8px;color:#94A3B8;font-weight:600'>Protein</th>")
          .append("<th style='text-align:right;padding:6px 8px;color:#94A3B8;font-weight:600'>Water</th>")
          .append("<th style='text-align:right;padding:6px 8px;color:#94A3B8;font-weight:600'>Meals</th>")
          .append("<th style='text-align:right;padding:6px 8px;color:#94A3B8;font-weight:600'>Compliance</th>")
          .append("</tr>");

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            final LocalDate fDay = day;
            Optional<DailyLog> log = logs.stream().filter(l -> fDay.equals(l.getLogDate())).findFirst();
            boolean hasData = log.isPresent() && (log.get().getMealsAssigned() != null && log.get().getMealsAssigned() > 0);
            String rowBg    = i % 2 == 0 ? "#fff" : "#F8FAFC";
            double dayComp  = hasData ? (double) log.get().getMealsCompleted() / log.get().getMealsAssigned() * 100 : 0;
            String compColor = dayComp >= 70 ? "#22C55E" : hasData ? "#EF4444" : "#94A3B8";

            sb.append("<tr style='background:").append(rowBg).append("'>")
              .append("<td style='padding:6px 8px;color:#0F172A;font-weight:500'>").append(day.format(DAY_FMT)).append("</td>")
              .append("<td style='text-align:right;padding:6px 8px;color:#2563EB;font-family:monospace'>")
              .append(hasData ? log.get().getCaloriesConsumed() + " kcal" : "—").append("</td>")
              .append("<td style='text-align:right;padding:6px 8px;color:#22C55E;font-family:monospace'>")
              .append(hasData ? String.format("%.1f", log.get().getProteinConsumed()) + "g" : "—").append("</td>")
              .append("<td style='text-align:right;padding:6px 8px;color:#0EA5E9;font-family:monospace'>")
              .append(hasData ? String.format("%.0f", log.get().getWaterIntake()) + "ml" : "—").append("</td>")
              .append("<td style='text-align:right;padding:6px 8px;color:#475569'>")
              .append(hasData ? log.get().getMealsCompleted() + "/" + log.get().getMealsAssigned() : "—").append("</td>")
              .append("<td style='text-align:right;padding:6px 8px;font-weight:700;color:").append(compColor).append("'>")
              .append(hasData ? String.format("%.0f", dayComp) + "%" : "—").append("</td>")
              .append("</tr>");
        }
        sb.append("</table></div>");

        // Recent progress notes
        if (!notes.isEmpty()) {
            sb.append("<div style='background:#F0F6FF;border:1px solid #BFDBFE;border-radius:8px;padding:12px;margin-bottom:8px'>")
              .append("<div style='font-size:12px;font-weight:700;color:#2563EB;margin-bottom:8px;text-transform:uppercase;letter-spacing:.06em'>Your Recent Notes</div>");
            notes.forEach(n -> sb.append("<div style='font-size:13px;color:#475569;padding:4px 0;border-bottom:1px solid #DBEAFE'>• ").append(n).append("</div>"));
            sb.append("</div>");
        }

        sb.append("<a href='").append(frontendUrl).append("/dietitian/clients/").append(clientId)
          .append("' style='display:inline-block;background:#F0F6FF;color:#2563EB;text-decoration:none;")
          .append("padding:8px 16px;border-radius:8px;font-weight:600;font-size:13px;border:1px solid #BFDBFE'>")
          .append("View ").append(name).append("'s Full Profile →</a>")
          .append("</div>");

        return sb.toString();
    }

    private double weekCompliance(Long clientId, LocalDate weekStart, LocalDate weekEnd) {
        List<DailyLog> logs = dailyLogRepo.findByRange(clientId, weekStart, weekEnd);
        int ta = logs.stream().mapToInt(l -> l.getMealsAssigned() != null ? l.getMealsAssigned() : 0).sum();
        int tc = logs.stream().mapToInt(l -> l.getMealsCompleted() != null ? l.getMealsCompleted() : 0).sum();
        return ta > 0 ? (double) tc / ta * 100 : 0;
    }
}
