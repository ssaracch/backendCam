package com.projet.cameraproject.repository;

import com.projet.cameraproject.entity.Alerts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface AlertsRepository extends JpaRepository<Alerts, Integer> {
    
    // Find alert by type and camera
    @Query("SELECT a FROM Alerts a WHERE a.type = :type AND a.camera.idCamera = :cameraId")
    Optional<Alerts> findByTypeAndCameraId(@Param("type") Alerts.AlertType type, @Param("cameraId") int cameraId);
    
    // Find alerts by type (returns list)
    List<Alerts> findByType(Alerts.AlertType type);
    
    // Find alerts by camera ID
    List<Alerts> findByCamera_IdCamera(int cameraId);
}