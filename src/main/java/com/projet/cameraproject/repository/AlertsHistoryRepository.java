package com.projet.cameraproject.repository;

import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.entity.AlertsHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertsHistoryRepository extends JpaRepository<AlertsHistory, AlertsHistoryId> {
    
    // Delete all alerts for a camera (for camera deletion)
    @Modifying
    @Query("DELETE FROM AlertsHistory ah WHERE ah.cameraId = :cameraId")
    void deleteByCameraId(@Param("cameraId") Integer cameraId);
    
    // Get all alerts ordered by start date (newest first)
    @Query("SELECT ah FROM AlertsHistory ah ORDER BY ah.start_alert DESC")
    List<AlertsHistory> findAllOrderByStartAlertDesc();
    
    // Find ongoing alerts (not resolved yet) - FIXED: Use correct field name
    @Query("SELECT ah FROM AlertsHistory ah WHERE ah.performed_at IS NULL")
    List<AlertsHistory> findByPerformed_atIsNull();
    
    // Find resolved alerts only - FIXED: Use correct field name
    @Query("SELECT ah FROM AlertsHistory ah WHERE ah.performed_at IS NOT NULL")
    List<AlertsHistory> findByPerformed_atIsNotNull();

    // Find ongoing alerts for a specific camera
    @Query("SELECT ah FROM AlertsHistory ah WHERE ah.cameraId = :cameraId AND ah.performed_at IS NULL")
    List<AlertsHistory> findByCameraIdAndPerformedAtIsNull(@Param("cameraId") int cameraId);

    // Find all alerts for a camera ordered by start date
    @Query("SELECT ah FROM AlertsHistory ah WHERE ah.cameraId = :cameraId ORDER BY ah.start_alert DESC")
    List<AlertsHistory> findByCameraIdOrderByStartAlertDesc(@Param("cameraId") int cameraId);

}