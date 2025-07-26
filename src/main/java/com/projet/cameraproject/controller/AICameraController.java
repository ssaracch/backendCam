package com.projet.cameraproject.controller;

import com.projet.cameraproject.service.AICameraService;
import com.projet.cameraproject.service.AICameraService.*;
import com.projet.cameraproject.entity.Camera;
import com.projet.cameraproject.entity.AlertsHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-camera")
@CrossOrigin(origins = "http://localhost:4200")
public class AICameraController {

    @Autowired
    private AICameraService aiCameraService;

    /**
     * Endpoint for AI predictions from Angular interface
     * POST /api/ai-camera/prediction
     */
    @PostMapping("/prediction")
    public ResponseEntity<?> handleAIPrediction(@RequestBody AIPredictionRequest request) {
        try {
            AIPredictionResponse response = aiCameraService.handleAIPrediction(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Missing required fields: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Endpoint for direct status updates (manual camera start/stop)
     * POST /api/ai-camera/status-update
     */
    @PostMapping("/status-update")
    public ResponseEntity<?> handleStatusUpdate(@RequestBody StatusUpdateRequest request) {
        try {
            StatusUpdateResponse response = aiCameraService.handleStatusUpdate(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Missing required fields: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Get ongoing alerts for a specific camera
     * GET /api/ai-camera/ongoing-alerts/{cameraId}
     */
    @GetMapping("/ongoing-alerts/{cameraId}")
    public ResponseEntity<?> getOngoingAlerts(@PathVariable Long cameraId) { // FIXED: integer -> Long
        try {
            List<AlertsHistory> ongoingAlerts = aiCameraService.getOngoingAlerts(cameraId);
            return ResponseEntity.ok(ongoingAlerts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching ongoing alerts: " + e.getMessage()));
        }
    }

    /**
     * Get camera history for display in camera history view
     * GET /api/ai-camera/history/{cameraId}
     */
    @GetMapping("/history/{cameraId}")
    public ResponseEntity<?> getCameraHistory(@PathVariable Long cameraId) { // FIXED: integer -> Long
        try {
            List<AlertsHistory> cameraHistory = aiCameraService.getCameraHistory(cameraId);
            return ResponseEntity.ok(cameraHistory);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching camera history: " + e.getMessage()));
        }
    }

    /**
     * Get all cameras with their current status for camera list display
     * GET /api/ai-camera/cameras
     */
    @GetMapping("/cameras")
    public ResponseEntity<?> getAllCameras() {
        try {
            List<Camera> cameras = aiCameraService.getAllCamerasWithStatus();
            return ResponseEntity.ok(cameras);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching camera list: " + e.getMessage()));
        }
    }

    /**
     * Get specific camera details by ID
     * GET /api/ai-camera/cameras/{cameraId}
     */
    @GetMapping("/cameras/{cameraId}")
    public ResponseEntity<?> getCameraById(@PathVariable Long cameraId) { // FIXED: integer -> Long
        try {
            Optional<Camera> camera = aiCameraService.getCameraById(cameraId);
            if (camera.isPresent()) {
                return ResponseEntity.ok(camera.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error fetching camera details: " + e.getMessage()));
        }
    }

    // Error response class
    public static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}