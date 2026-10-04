package com.nutritrack.service;

import com.nutritrack.entity.MealPlan;
import com.nutritrack.entity.User;
import com.nutritrack.repository.MealPlanRepository;
import com.nutritrack.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Permanently deletes a client account and every row that belongs to it.
 * Runs in ONE transaction: if any step fails, nothing is deleted.
 */
@Service
public class ClientDeletionService {

    @Autowired private UserRepository userRepo;
    @Autowired private MealPlanRepository mealPlanRepo;
    @PersistenceContext private EntityManager em;

    @Transactional
    public void deleteClientAccount(Long clientId) {
        User client = userRepo.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getRole() != User.Role.CLIENT) {
            throw new RuntimeException("Only client accounts can be deleted");
        }

        // 1. Simple child tables (bulk deletes, so photo blobs are never loaded into memory)
        bulkDelete("Alert", "client", clientId);
        bulkDelete("ProgressPhoto", "client", clientId);
        bulkDelete("ProgressNote", "client", clientId);
        bulkDelete("WeightLog", "client", clientId);
        bulkDelete("DailyLog", "client", clientId);
        bulkDelete("RefreshToken", "user", clientId);

        // 2. Meal plans: delete via entities so cascade removes meals -> food items
        List<MealPlan> plans = mealPlanRepo.findByClientIdOrderByPlanDateDesc(clientId);
        mealPlanRepo.deleteAll(plans);
        mealPlanRepo.flush();

        // 3. Client profile
        bulkDelete("ClientProfile", "user", clientId);

        // 4. Finally the user itself (clear stale entities first, then delete)
        em.flush();
        em.clear();
        userRepo.deleteById(clientId);
    }

    private void bulkDelete(String entity, String ownerField, Long id) {
        em.createQuery("delete from " + entity + " e where e." + ownerField + ".id = :id")
          .setParameter("id", id)
          .executeUpdate();
    }
}
