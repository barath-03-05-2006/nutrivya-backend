package com.nutritrack.service;
import com.nutritrack.entity.*; import com.nutritrack.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate; import java.util.*; import java.util.stream.Collectors;
@Service
public class AnalyticsService {
    @Autowired private DailyLogRepository dailyLogRepo;
    @Autowired private ClientProfileRepository profileRepo;
    @Autowired private WeightLogRepository weightLogRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AlertRepository alertRepo;
    @Autowired private ProgressNoteRepository noteRepo;

    public Map<String,Object> getDailySummary(Long clientId, LocalDate date){
        DailyLog log=dailyLogRepo.findByClientIdAndLogDate(clientId,date).orElse(null);
        ClientProfile profile=profileRepo.findByUserId(clientId).orElse(null);
        Map<String,Object> r=new HashMap<>();
        r.put("date",date.toString());
        r.put("caloriesConsumed",log!=null?log.getCaloriesConsumed():0);
        r.put("proteinConsumed",log!=null?log.getProteinConsumed():0.0);
        r.put("carbsConsumed",log!=null?log.getCarbsConsumed():0.0);
        r.put("fatConsumed",log!=null?log.getFatConsumed():0.0);
        r.put("fiberConsumed",log!=null?log.getFiberConsumed():0.0);
        r.put("waterIntake",log!=null?log.getWaterIntake():0.0);
        r.put("mealsAssigned",log!=null?log.getMealsAssigned():0);
        r.put("mealsCompleted",log!=null?log.getMealsCompleted():0);
        double comp=log!=null&&log.getMealsAssigned()>0?(double)log.getMealsCompleted()/log.getMealsAssigned()*100:0;
        r.put("complianceRate",comp);
        r.put("targetCalories",profile!=null&&profile.getTargetCalories()!=null?profile.getTargetCalories():2000);
        r.put("targetProtein",profile!=null&&profile.getTargetProtein()!=null?profile.getTargetProtein():150);
        r.put("targetCarbs",profile!=null&&profile.getTargetCarbs()!=null?profile.getTargetCarbs():200);
        r.put("targetFat",profile!=null&&profile.getTargetFat()!=null?profile.getTargetFat():65);
        r.put("targetFiber",profile!=null&&profile.getTargetFiber()!=null?profile.getTargetFiber():30);
        r.put("targetWater",profile!=null&&profile.getTargetWater()!=null?profile.getTargetWater():3000);
        return r;
    }

    public Map<String,Object> getWeeklySummary(Long clientId, LocalDate weekStart){
        List<Map<String,Object>> days=new ArrayList<>();
        for(int i=0;i<7;i++) days.add(getDailySummary(clientId,weekStart.plusDays(i)));
        double avgCal=days.stream().mapToDouble(d->((Number)d.get("caloriesConsumed")).doubleValue()).average().orElse(0);
        double avgPro=days.stream().mapToDouble(d->((Number)d.get("proteinConsumed")).doubleValue()).average().orElse(0);
        double avgCarb=days.stream().mapToDouble(d->((Number)d.get("carbsConsumed")).doubleValue()).average().orElse(0);
        double avgFat=days.stream().mapToDouble(d->((Number)d.get("fatConsumed")).doubleValue()).average().orElse(0);
        double avgComp=days.stream().mapToDouble(d->((Number)d.get("complianceRate")).doubleValue()).average().orElse(0);
        return Map.of("avgCalories",avgCal,"avgProtein",avgPro,"avgCarbs",avgCarb,
            "avgFat",avgFat,"weeklyCompliance",avgComp,"dailyBreakdown",days);
    }

    public Map<String,Object> getClientOverview(Long clientId){
        User client=userRepo.findById(clientId).orElseThrow();
        ClientProfile profile=profileRepo.findByUserId(clientId).orElse(null);
        LocalDate from=LocalDate.now().minusDays(30);
        List<DailyLog> logs=dailyLogRepo.findRecent(clientId,from);
        double avgCal=logs.stream().mapToInt(DailyLog::getCaloriesConsumed).average().orElse(0);
        double avgPro=logs.stream().mapToDouble(DailyLog::getProteinConsumed).average().orElse(0);
        double avgCarb=logs.stream().mapToDouble(DailyLog::getCarbsConsumed).average().orElse(0);
        double avgFat=logs.stream().mapToDouble(DailyLog::getFatConsumed).average().orElse(0);
        double avgWater=logs.stream().mapToDouble(DailyLog::getWaterIntake).average().orElse(0);
        int ta=logs.stream().mapToInt(DailyLog::getMealsAssigned).sum();
        int tc=logs.stream().mapToInt(DailyLog::getMealsCompleted).sum();
        double comp=ta>0?(double)tc/ta*100:0;
        double cw=profile!=null&&profile.getCurrentWeight()!=null?profile.getCurrentWeight():0;
        double sw=profile!=null&&profile.getStartingWeight()!=null?profile.getStartingWeight():cw;
        List<String> notes=noteRepo.findTop5ByClientIdOrderByCreatedAtDesc(clientId)
            .stream().map(ProgressNote::getNote).collect(Collectors.toList());
        long alerts=profile!=null&&profile.getDietitian()!=null?alertRepo.countByDietitianIdAndReadFalse(profile.getDietitian().getId()):0;
        Map<String,Object> r=new HashMap<>();
        r.put("clientId",clientId); r.put("clientName",client.getFullName());
        r.put("currentWeight",cw); r.put("weightChange",cw-sw);
        r.put("complianceRate",comp); r.put("avgDailyCalories",avgCal);
        r.put("avgDailyProtein",avgPro); r.put("avgDailyCarbs",avgCarb);
        r.put("avgDailyFat",avgFat); r.put("waterIntakeAvg",avgWater);
        r.put("recentProgressNotes",notes); r.put("unreadAlerts",(int)alerts);
        if(profile!=null) r.put("goalWeight",profile.getGoalWeight());
        return r;
    }

    public void updateWater(Long clientId, double amount, LocalDate date){
        User client=userRepo.findById(clientId).orElseThrow();
        DailyLog log=dailyLogRepo.findByClientAndLogDate(client,date)
            .orElseGet(()->{DailyLog dl=new DailyLog();dl.setClient(client);dl.setLogDate(date);return dl;});
        log.setWaterIntake(log.getWaterIntake() + amount);
        dailyLogRepo.save(log);
    }

    public WeightLog logWeight(Long clientId, double weight, LocalDate date, String notes){
        User client=userRepo.findById(clientId).orElseThrow();
        WeightLog wl=new WeightLog(); wl.setClient(client); wl.setWeight(weight);
        wl.setLogDate(date); wl.setNotes(notes);
        WeightLog saved=weightLogRepo.save(wl);
        profileRepo.findByUserId(clientId).ifPresent(p->{p.setCurrentWeight(weight);profileRepo.save(p);});
        return saved;
    }
}
