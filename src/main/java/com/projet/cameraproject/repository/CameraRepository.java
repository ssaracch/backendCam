package com.projet.cameraproject.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.cameraproject.entity.Camera;
import java.util.List;

public interface CameraRepository extends JpaRepository<Camera, Integer> {
    
    long countByStatusCamera(Camera.Status statusCamera);
    List<Camera> findByStatusCameraIn(List<Camera.Status> statuses);

}
