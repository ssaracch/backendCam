package com.projet.cameraproject.service;

import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.entity.AlertsHistoryId;
import com.projet.cameraproject.entity.Camera;
import com.projet.cameraproject.entity.User;
import com.projet.cameraproject.entity.Groupe;
import com.projet.cameraproject.repository.CameraRepository;
import com.projet.cameraproject.repository.AlertsHistoryRepository;
import com.projet.cameraproject.repository.UserRepository;
import com.projet.cameraproject.repository.GroupeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CameraService {

    @Autowired
    private CameraRepository repository;

    @Autowired
    private AlertsHistoryRepository alertsHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupeRepository groupeRepository;

    @Autowired
    private AlertsHistoryService alertsHistoryService;

    public List<Camera> getAll() {
        return repository.findAll();
    }

    public Optional<Camera> getById(int id) {
        return repository.findById(id);
    }

    public Camera save(Camera camera) {
        User user = userRepository.findById(camera.getUser().getId_User())
            .orElseThrow(() -> new RuntimeException("User not found with id " + camera.getUser().getId_User()));

        Groupe groupe = groupeRepository.findById(camera.getGroupe().getId_Groupe())
            .orElseThrow(() -> new RuntimeException("Groupe not found with id " + camera.getGroupe().getId_Groupe()));

        camera.setUser(user);
        camera.setGroupe(groupe);

        return repository.save(camera);
    }

    public Camera update(int id, Camera camera) {
    Optional<Camera> optionalCamera = repository.findById(id);

    if (optionalCamera.isPresent()) {
        Camera existingCamera = optionalCamera.get();
        Camera.Status oldStatus = existingCamera.getStatusCamera();
        Camera.Status newStatus = camera.getStatusCamera();

        existingCamera.setNomCamera(camera.getNomCamera());
        existingCamera.setLocation(camera.getLocation());
        existingCamera.setIpAdress(camera.getIpAdress());
        existingCamera.setMacAdress(camera.getMacAdress());
        existingCamera.setStatusCamera(newStatus);

        User user = userRepository.findById(camera.getUser().getId_User())
            .orElseThrow(() -> new RuntimeException("User not found with id " + camera.getUser().getId_User()));
        Groupe groupe = groupeRepository.findById(camera.getGroupe().getId_Groupe())
            .orElseThrow(() -> new RuntimeException("Groupe not found with id " + camera.getGroupe().getId_Groupe()));

        existingCamera.setUser(user);
        existingCamera.setGroupe(groupe);

        Camera updatedCamera = repository.save(existingCamera);

        // Handle status changes and alert creation/resolution
        if (!oldStatus.equals(newStatus)) {
            if (newStatus == Camera.Status.offline || newStatus == Camera.Status.blurry) {
                // Create new alert
                int alertId = newStatus == Camera.Status.offline ? 1 : 2;

                // First, resolve any existing ongoing alerts for this camera of the same type
                AlertsHistoryId existingId = new AlertsHistoryId(user.getId_User(), updatedCamera.getIdCamera(), alertId);
                Optional<AlertsHistory> existingAlert = alertsHistoryRepository.findById(existingId);
               if (existingAlert.isPresent() && existingAlert.get().getPerformed_at() == null) {
                   // Resolve the existing ongoing alert
                   AlertsHistory existing = existingAlert.get();
                   existing.setPerformed_at(LocalDateTime.now());
                   alertsHistoryRepository.save(existing);
               }

               // Create new alert entry
               AlertsHistory history = new AlertsHistory(user.getId_User(), updatedCamera.getIdCamera(), alertId);
               history.setStart_alert(LocalDateTime.now());
               history.setPerformed_at(null); // Alert is ongoing
               history.setUser(user);
               history.setCamera(updatedCamera);

               alertsHistoryRepository.save(history);
               
           } else if (newStatus == Camera.Status.normal) {
               // Resolve all ongoing alerts for this camera
               List<Integer> alertTypes = Arrays.asList(1, 2);
               for (int alertId : alertTypes) {
                   AlertsHistoryId historyId = new AlertsHistoryId(user.getId_User(), updatedCamera.getIdCamera(), alertId);
                   alertsHistoryRepository.findById(historyId).ifPresent(history -> {
                       if (history.getPerformed_at() == null) {
                           history.setPerformed_at(LocalDateTime.now());
                           alertsHistoryRepository.save(history);
                       }
                   });
               }
           }
       }

       return updatedCamera;
   } else {
       throw new RuntimeException("Camera not found with id " + id);
   }
}

    
    @Transactional
    public void delete(int id) {
        System.out.println("CameraService: Attempting to delete camera with ID: " + id);

        if (!repository.existsById(id)) {
            System.out.println("Camera with ID " + id + " does not exist");
            throw new RuntimeException("Camera not found with id " + id);
        }

        alertsHistoryRepository.deleteByCameraId(id);
        repository.deleteById(id);

        System.out.println("CameraService: Camera with ID " + id + " deleted successfully");
    }

    public Map<String, Long> getStatusCounts() {
        List<Camera> cameras = repository.findAll();
        Map<String, Long> statusCounts = new HashMap<>();

        for (Camera camera : cameras) {
            String status = camera.getStatusCamera().toString();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }

        return statusCounts;
    }

    public List<Camera> findOfflineOrBlurryCameras() {
    return repository.findByStatusCameraIn(Arrays.asList(Camera.Status.offline, Camera.Status.blurry));
}

}