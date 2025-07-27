package com.projet.cameraproject.service;

import com.projet.cameraproject.entity.Camera;
import com.projet.cameraproject.entity.Alerts;
import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.repository.CameraRepository;
import com.projet.cameraproject.repository.AlertsRepository;
import com.projet.cameraproject.repository.AlertsHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
@Transactional
public class AICameraService {

    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
    private AlertsRepository alertsRepository;

    @Autowired
    private AlertsHistoryRepository alertsHistoryRepository;

    private final double CONFIDENCE_THRESHOLD = 75.0;

    /**
     * Handle AI prediction from Angular interface
     */
    public AIPredictionResponse handleAIPrediction(AIPredictionRequest request) {
        try {
            // Validate input
            if (request.getCameraId() == null || request.getPrediction() == null || 
                request.getProbability() == null || request.getTimestamp() == null) {
                throw new IllegalArgumentException("Missing required fields");
            }

            // Convert prediction to status
            String newStatus = request.getPrediction() == 1 ? "normal" : "blurry";
            double confidence = request.getProbability()[request.getPrediction()] * 100;

            // Only process if confidence is above threshold
            if (confidence < CONFIDENCE_THRESHOLD) {
                return new AIPredictionResponse(
                    true, 
                    "Prediction received but confidence too low to act",
                    null, null, newStatus, confidence, 
                    request.getPrediction() == 1 ? "Clear" : "Not Clear"
                );
            }

            // Get current camera data - FIXED: Use int instead of Long
            Optional<Camera> cameraOpt = cameraRepository.findById(request.getCameraId().intValue());
            if (!cameraOpt.isPresent()) {
                throw new RuntimeException("Camera with ID " + request.getCameraId() + " not found");
            }

            Camera camera = cameraOpt.get();
            String currentStatus = camera.getStatusCamera().toString(); // FIXED: Convert enum to string

            // Process status change
            StatusChangeResult result = processStatusChange(
                camera, 
                currentStatus, 
                newStatus, 
                request.getTimestamp(), 
                confidence,
                request.getImage()
            );

            return new AIPredictionResponse(
                true,
                result.getMessage(),
                result.getAlertId(),
                currentStatus,
                newStatus,
                confidence,
                request.getPrediction() == 1 ? "Clear" : "Not Clear"
            );

        } catch (Exception e) {
            throw new RuntimeException("AI Prediction Error: " + e.getMessage());
        }
    }

    /**
     * Handle direct status updates (for manual camera start/stop)
     */
    public StatusUpdateResponse handleStatusUpdate(StatusUpdateRequest request) {
        try {
            // Validate input
            if (request.getCameraId() == null || request.getNewStatus() == null || 
                request.getTimestamp() == null) {
                throw new IllegalArgumentException("Missing required fields");
            }

            // Get current camera data - FIXED: Use int instead of Long
            Optional<Camera> cameraOpt = cameraRepository.findById(request.getCameraId().intValue());
            if (!cameraOpt.isPresent()) {
                throw new RuntimeException("Camera with ID " + request.getCameraId() + " not found");
            }

            Camera camera = cameraOpt.get();
            String currentStatus = camera.getStatusCamera().toString(); // FIXED: Convert enum to string

            // Process status change
            StatusChangeResult result = processStatusChange(
                camera,
                currentStatus,
                request.getNewStatus(),
                request.getTimestamp(),
                request.getConfidence() != null ? request.getConfidence() : 100.0,
                null,
                request.getSource() != null ? request.getSource() : "MANUAL"
            );

            return new StatusUpdateResponse(
                true,
                result.getMessage(),
                result.getAlertId(),
                currentStatus,
                request.getNewStatus()
            );

        } catch (Exception e) {
            throw new RuntimeException("Status Update Error: " + e.getMessage());
        }
    }

    /**
     * Process the status change logic
     */
    private StatusChangeResult processStatusChange(Camera camera, String currentStatus, 
            String newStatus, String timestamp, double confidence, String image) {
        return processStatusChange(camera, currentStatus, newStatus, timestamp, confidence, image, "AI_DETECTION");
    }

    private StatusChangeResult processStatusChange(Camera camera, String currentStatus, 
            String newStatus, String timestamp, double confidence, String image, String source) {
        
        // No change - ignore
        if (currentStatus.equals(newStatus)) {
            return new StatusChangeResult("No status change detected", null);
        }

        // Update camera status first
        updateCameraStatus(camera.getIdCamera(), newStatus);

        // Handle different status transitions
        if ("normal".equals(newStatus)) {
            // Camera became normal - resolve existing alerts
            return resolveExistingAlerts(camera, timestamp);
        } else {
            // Camera became offline or blurry - create new alert
            return createNewAlert(camera, newStatus, timestamp, confidence, image, source);
        }
    }

    /**
     * Create a new alert for offline/blurry status
     */
    private StatusChangeResult createNewAlert(Camera camera, String alertType, 
            String timestamp, double confidence, String image, String source) {
        try {
            // Create alert record
            Alerts alert = new Alerts();
            // FIXED: Convert string to enum and set camera relationship
            Alerts.AlertType enumType = Alerts.AlertType.valueOf(alertType);
            alert.setType(enumType);
            alert.setCamera(camera); // Set the camera relationship, not just ID
            Alerts savedAlert = alertsRepository.save(alert);

            // Parse timestamp
            LocalDateTime startAlert = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);

            // Create history entry with performed_at = null (ongoing)
            AlertsHistory history = new AlertsHistory();
            history.setUserId(camera.getUser().getId_User());
            history.setCameraId(camera.getIdCamera());
            history.setAlertId(savedAlert.getId_Alert()); // FIXED: Use correct getter name
            history.setStart_alert(startAlert);
            history.setPerformed_at(null); // Ongoing alert
            history.setConfidence(BigDecimal.valueOf(confidence));
            history.setSource(source);
            history.setCapturedImage(image);

            alertsHistoryRepository.save(history);

            System.out.println("Alert created: Camera " + camera.getNomCamera() + 
                " is " + alertType + " (Alert ID: " + savedAlert.getId_Alert() + ")");

            return new StatusChangeResult(
                "Alert created for " + alertType + " status", 
                savedAlert.getId_Alert()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create alert: " + e.getMessage());
        }
    }

    /**
     * Resolve existing alerts when camera becomes normal
     */
    private StatusChangeResult resolveExistingAlerts(Camera camera, String timestamp) {
        try {
            // Find ongoing alerts for this camera (performed_at is null)
            List<AlertsHistory> ongoingAlerts = alertsHistoryRepository
                .findByCameraIdAndPerformedAtIsNull(camera.getIdCamera()); // FIXED: Use correct method name

            if (ongoingAlerts.isEmpty()) {
                return new StatusChangeResult("No ongoing alerts to resolve", null);
            }

            // Parse timestamp
            LocalDateTime performedAt = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);

            // Update all ongoing alerts with performed_at timestamp
            for (AlertsHistory alert : ongoingAlerts) {
                alert.setPerformed_at(performedAt);
                alertsHistoryRepository.save(alert);
            }

            System.out.println("AI Resolution: " + ongoingAlerts.size() + 
                " alerts resolved for camera " + camera.getNomCamera());

            return new StatusChangeResult(
                ongoingAlerts.size() + " alerts resolved",
                ongoingAlerts.get(0).getAlertId()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve alerts: " + e.getMessage());
        }
    }

    /**
     * Update camera status in database
     */
    private void updateCameraStatus(int cameraId, String newStatus) { // FIXED: Use int instead of Long
        Optional<Camera> cameraOpt = cameraRepository.findById(cameraId);
        if (cameraOpt.isPresent()) {
            Camera camera = cameraOpt.get();
            // FIXED: Convert string to enum
            Camera.Status status = Camera.Status.valueOf(newStatus);
            camera.setStatusCamera(status);
            cameraRepository.save(camera);
        }
    }

    /**
     * Get ongoing alerts for a camera
     */
    public List<AlertsHistory> getOngoingAlerts(Long cameraId) {
        return alertsHistoryRepository.findByCameraIdAndPerformedAtIsNull(cameraId.intValue()); // FIXED: Convert to int
    }

    /**
     * Get all alerts history for a camera (for camera history display)
     */
    public List<AlertsHistory> getCameraHistory(Long cameraId) {
        return alertsHistoryRepository.findByCameraIdOrderByStartAlertDesc(cameraId.intValue()); // FIXED: Convert to int
    }

    /**
     * Get all cameras with their current status (for camera list display)
     */
    public List<Camera> getAllCamerasWithStatus() {
        return cameraRepository.findAll();
    }

    /**
     * Get camera status by ID
     */
    public Optional<Camera> getCameraById(Long cameraId) {
        return cameraRepository.findById(cameraId.intValue()); // FIXED: Convert to int
    }

    // Inner classes for request/response objects (unchanged)
    public static class AIPredictionRequest {
        private Long cameraId;
        private Integer prediction;
        private double[] probability;
        private String image;
        private String timestamp;

        // Getters and setters
        public Long getCameraId() { return cameraId; }
        public void setCameraId(Long cameraId) { this.cameraId = cameraId; }
        public Integer getPrediction() { return prediction; }
        public void setPrediction(Integer prediction) { this.prediction = prediction; }
        public double[] getProbability() { return probability; }
        public void setProbability(double[] probability) { this.probability = probability; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public static class StatusUpdateRequest {
        private Long cameraId;
        private String newStatus;
        private String timestamp;
        private Double confidence;
        private String source;

        // Getters and setters
        public Long getCameraId() { return cameraId; }
        public void setCameraId(Long cameraId) { this.cameraId = cameraId; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    public static class AIPredictionResponse {
        private boolean success;
        private String message;
        private Integer alertId; // FIXED: Use Integer instead of Long
        private String previousStatus;
        private String newStatus;
        private double confidence;
        private String label;

        public AIPredictionResponse(boolean success, String message, Integer alertId, 
                String previousStatus, String newStatus, double confidence, String label) {
            this.success = success;
            this.message = message;
            this.alertId = alertId;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.confidence = confidence;
            this.label = label;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getAlertId() { return alertId; }
        public String getPreviousStatus() { return previousStatus; }
        public String getNewStatus() { return newStatus; }
        public double getConfidence() { return confidence; }
        public String getLabel() { return label; }
    }

    public static class StatusUpdateResponse {
        private boolean success;
        private String message;
        private Integer alertId; // FIXED: Use Integer instead of Long
        private String previousStatus;
        private String newStatus;

        public StatusUpdateResponse(boolean success, String message, Integer alertId, 
                String previousStatus, String newStatus) {
            this.success = success;
            this.message = message;
            this.alertId = alertId;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getAlertId() { return alertId; }
        public String getPreviousStatus() { return previousStatus; }
        public String getNewStatus() { return newStatus; }
    }

    private static class StatusChangeResult {
        private String message;
        private Integer alertId; // FIXED: Use Integer instead of Long

        public StatusChangeResult(String message, Integer alertId) {
            this.message = message;
            this.alertId = alertId;
        }

        public String getMessage() { return message; }
        public Integer getAlertId() { return alertId; }
    }
}