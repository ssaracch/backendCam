package com.projet.cameraproject.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.projet.cameraproject.entity.Camera;
import com.projet.cameraproject.service.CameraService;

@RestController
@RequestMapping("/api/cameras")
@CrossOrigin
public class CameraController {

    @Autowired
    private CameraService cameraService;

    @GetMapping
    public List<Camera> getAll() {
        return cameraService.getAll();
    }

    @GetMapping("/{id}")
    public Camera getById(@PathVariable int id) {
        return cameraService.getById(id).orElseThrow(() -> new RuntimeException("Camera not found with id " + id));
    }

    @PostMapping
    public Camera save(@RequestBody Camera camera) {
        return cameraService.save(camera);
    }

    @PutMapping("/{id}")
    public Camera update(@PathVariable int id, @RequestBody Camera camera) {
        return cameraService.update(id, camera);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            System.out.println("Attempting to delete camera with ID: " + id);

            if (!cameraService.getById(id).isPresent()) {
                System.out.println("Camera with ID " + id + " not found");
                return ResponseEntity.notFound().build();
            }

            cameraService.delete(id);
            System.out.println("Camera with ID " + id + " deleted successfully");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error deleting camera with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting camera: " + e.getMessage());
        }
    }

    @GetMapping("/status-counts")
    public Map<String, Long> getStatusCounts() {
        return cameraService.getStatusCounts();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getCameraStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) cameraService.getAll().size());
        stats.put("online", cameraService.getStatusCounts().getOrDefault("online", 0L));
        stats.put("offline", cameraService.getStatusCounts().getOrDefault("offline", 0L));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/status/offline-or-blurry")
    public List<Camera> getOfflineOrBlurryCameras() {
    return cameraService.findOfflineOrBlurryCameras();
}

}