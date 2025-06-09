package com.projet.cameraproject.service;

import com.projet.cameraproject.entity.Camera;
import com.projet.cameraproject.repository.CameraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CameraService {

    @Autowired
    private CameraRepository repository;

    public List<Camera> getAll() {
        return repository.findAll();
    }

    public Optional<Camera> getById(int id) {
        return repository.findById(id);
    }

    public Camera save(Camera camera) {
        return repository.save(camera);
    }

    public Camera update(int id, Camera camera) {
        Optional<Camera> optionalCamera = repository.findById(id);

        if (optionalCamera.isPresent()) {
            Camera existingCamera = optionalCamera.get();
            existingCamera.setNomCamera(camera.getNomCamera());
            existingCamera.setLocation(camera.getLocation());
            existingCamera.setIpAdress(camera.getIpAdress());
            existingCamera.setMacAdress(camera.getMacAdress());
            existingCamera.setStatusCamera(camera.getStatusCamera());
            existingCamera.setUser(camera.getUser());
            existingCamera.setGroupe(camera.getGroupe());

            return repository.save(existingCamera);
        } else {
            throw new RuntimeException("Camera not found with id " + id);
        }
    }

    public void delete(int id) {
        repository.deleteById(id);
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

}
