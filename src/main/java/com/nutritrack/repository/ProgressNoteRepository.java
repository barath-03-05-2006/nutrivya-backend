package com.nutritrack.repository;
import com.nutritrack.entity.ProgressNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProgressNoteRepository extends JpaRepository<ProgressNote,Long> {
    List<ProgressNote> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<ProgressNote> findTop5ByClientIdOrderByCreatedAtDesc(Long clientId);

    // NEW: batched version for the client-overview list (avoids N+1 loop)
    List<ProgressNote> findByClientIdInOrderByCreatedAtDesc(List<Long> clientIds);
}
