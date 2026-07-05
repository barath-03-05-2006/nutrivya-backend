package com.nutritrack.repository;
import com.nutritrack.entity.ProgressPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, Long> {
    @Query("SELECT p FROM ProgressPhoto p WHERE p.client.id = :clientId ORDER BY p.photoDate DESC, p.uploadedAt DESC")
    List<ProgressPhoto> findByClientIdOrderByDateDesc(@Param("clientId") Long clientId);

    // Metadata only (no image blob) for listing
    @Query("SELECT new map(p.id as id, p.label as label, p.photoDate as photoDate, p.uploadedAt as uploadedAt, p.imageType as imageType) " +
           "FROM ProgressPhoto p WHERE p.client.id = :clientId ORDER BY p.photoDate DESC, p.uploadedAt DESC")
    List<java.util.Map<String, Object>> findMetaByClientId(@Param("clientId") Long clientId);
}
