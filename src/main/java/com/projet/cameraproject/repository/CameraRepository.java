package com.projet.cameraproject.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.cameraproject.entity.Camera;

public interface CameraRepository extends JpaRepository<Camera, Integer> {
    
    long countByStatusCamera(String statusCamera);

}
