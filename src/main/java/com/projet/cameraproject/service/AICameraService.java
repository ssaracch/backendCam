// FIXED AICameraService.java - Fix status change logic and confidence handling
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
import java.math.RoundingMode;

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

            // FIXED: Ensure confidence is within valid range and properly formatted
            confidence = Math.min(confidence, 100.0); // Cap at 100%
            confidence = Math.max(confidence, 0.0);   // Floor at 0%
            
            System.out.println("Processing AI prediction - Status: " + newStatus + ", Confidence: " + confidence + "%");

            // Get current camera data
            Optional<Camera> cameraOpt = cameraRepository.findById(request.getCameraId().intValue());
            if (!cameraOpt.isPresent()) {
                throw new RuntimeException("Camera with ID " + request.getCameraId() + " not found");
            }

            Camera camera = cameraOpt.get();
            String currentStatus = camera.getStatusCamera().toString();

            System.out.println("Current camera status: " + currentStatus + " -> New status: " + newStatus);

            // FIXED: Always process predictions, regardless of confidence for status updates
            // Only use confidence threshold for alert creation
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
            System.err.println("AI Prediction Error: " + e.getMessage());
            e.printStackTrace();
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

            // Get current camera data
            Optional<Camera> cameraOpt = cameraRepository.findById(request.getCameraId().intValue());
            if (!cameraOpt.isPresent()) {
                throw new RuntimeException("Camera with ID " + request.getCameraId() + " not found");
            }

            Camera camera = cameraOpt.get();
            String currentStatus = camera.getStatusCamera().toString();

            // FIXED: Handle confidence value properly
            double confidence = request.getConfidence() != null ? request.getConfidence() : 100.0;
            confidence = Math.min(confidence, 100.0); // Cap at 100%
            confidence = Math.max(confidence, 0.0);   // Floor at 0%

            System.out.println("Status update request: " + currentStatus + " -> " + request.getNewStatus());

            // Process status change
            StatusChangeResult result = processStatusChange(
                camera,
                currentStatus,
                request.getNewStatus(),
                request.getTimestamp(),
                confidence,
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
            System.err.println("Status Update Error: " + e.getMessage());
            e.printStackTrace();
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
        
        System.out.println("Processing status change: " + currentStatus + " -> " + newStatus + 
                          " (Confidence: " + confidence + "%)");

        // FIXED: Always update camera status first, even for no-change scenarios
        updateCameraStatus(camera.getIdCamera(), newStatus);

        // FIXED: Don't skip processing for same status - we still want to log the detection
        if (currentStatus.equals(newStatus)) {
            System.out.println("Status unchanged but still processing detection");
            
            // If it's a blurry->blurry or offline->offline detection with high confidence,
            // we might want to update the existing alert timestamp or create a new entry
            if (("blurry".equals(newStatus) || "offline".equals(newStatus)) && confidence >= CONFIDENCE_THRESHOLD) {
                // Check if there's an ongoing alert to update
                List<AlertsHistory> ongoingAlerts = alertsHistoryRepository
                    .findByCameraIdAndPerformedAtIsNull(camera.getIdCamera());
                
                if (!ongoingAlerts.isEmpty()) {
                    // Update the confidence of the most recent alert if this one is higher
                    AlertsHistory latestAlert = ongoingAlerts.get(0);
                    BigDecimal newConfidenceBD = BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
                    
                    if (latestAlert.getConfidence() == null || 
                        latestAlert.getConfidence().compareTo(newConfidenceBD) < 0) {
                        latestAlert.setConfidence(newConfidenceBD);
                        alertsHistoryRepository.save(latestAlert);
                        System.out.println("Updated existing alert confidence to: " + confidence + "%");
                    }
                    
                    return new StatusChangeResult("Detection logged, confidence updated", latestAlert.getAlertId());
                }
            }
            
            return new StatusChangeResult("Status unchanged, detection logged", null);
        }

        // Handle different status transitions
        if ("normal".equals(newStatus)) {
            // Camera became normal - resolve existing alerts
            return resolveExistingAlerts(camera, timestamp);
        } else {
            // Camera became offline or blurry - create new alert only if confidence is high enough
            if (confidence >= CONFIDENCE_THRESHOLD) {
                return createNewAlert(camera, newStatus, timestamp, confidence, image, source);
            } else {
                System.out.println("Status changed but confidence too low for alert creation: " + confidence + "%");
                return new StatusChangeResult("Status updated but confidence too low for alert (" + confidence + "%)", null);
            }
        }
    }

    /**
     * Create a new alert for offline/blurry status
     */
    private StatusChangeResult createNewAlert(Camera camera, String alertType, 
            String timestamp, double confidence, String image, String source) {
        try {
            System.out.println("Creating new alert: " + alertType + " for camera " + camera.getNomCamera());

            // FIXED: First resolve any existing alerts before creating new one
            resolveExistingAlerts(camera, timestamp);

            // Create alert record
            Alerts alert = new Alerts();
            Alerts.AlertType enumType = Alerts.AlertType.valueOf(alertType.toUpperCase());
            alert.setType(enumType);
            alert.setCamera(camera);
            Alerts savedAlert = alertsRepository.save(alert);

            // Parse timestamp
            LocalDateTime startAlert = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);

            // FIXED: Create BigDecimal with proper scale and rounding
            BigDecimal confidenceBD = BigDecimal.valueOf(confidence)
                .setScale(2, RoundingMode.HALF_UP);

            System.out.println("Confidence BigDecimal: " + confidenceBD);

            // Create history entry with performed_at = null (ongoing)
            AlertsHistory history = new AlertsHistory();
            history.setUserId(camera.getUser().getId_User());
            history.setCameraId(camera.getIdCamera());
            history.setAlertId(savedAlert.getId_Alert());
            history.setStart_alert(startAlert);
            history.setPerformed_at(null); // Ongoing alert
            history.setConfidence(confidenceBD);
            history.setSource(source);
            history.setCapturedImage(image);

            alertsHistoryRepository.save(history);

            System.out.println("Alert created successfully: Camera " + camera.getNomCamera() + 
                " is " + alertType + " (Alert ID: " + savedAlert.getId_Alert() + 
                ", Confidence: " + confidenceBD + "%)");

            return new StatusChangeResult(
                "Alert created for " + alertType + " status", 
                savedAlert.getId_Alert()
            );

        } catch (Exception e) {
            System.err.println("Failed to create alert: " + e.getMessage());
            e.printStackTrace();
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
                .findByCameraIdAndPerformedAtIsNull(camera.getIdCamera());

            if (ongoingAlerts.isEmpty()) {
                System.out.println("No ongoing alerts to resolve for camera " + camera.getNomCamera());
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
            System.err.println("Failed to resolve alerts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to resolve alerts: " + e.getMessage());
        }
    }

    /**
     * Update camera status in database
     */
    private void updateCameraStatus(int cameraId, String newStatus) {
        try {
            Optional<Camera> cameraOpt = cameraRepository.findById(cameraId);
            if (cameraOpt.isPresent()) {
                Camera camera = cameraOpt.get();
                
                // FIXED: Handle case sensitivity and validation
                String normalizedStatus = newStatus.toLowerCase();
                Camera.Status status;
                
                switch (normalizedStatus) {
                    case "normal":
                        status = Camera.Status.normal;
                        break;
                    case "blurry":
                        status = Camera.Status.blurry;
                        break;
                    case "offline":
                        status = Camera.Status.offline;
                        break;
                    default:
                        System.err.println("Invalid status: " + newStatus + ", defaulting to offline");
                        status = Camera.Status.offline;
                }
                
                camera.setStatusCamera(status);
                cameraRepository.save(camera);
                System.out.println("Camera " + cameraId + " status updated to: " + status);
            } else {
                System.err.println("Camera " + cameraId + " not found for status update");
            }
        } catch (Exception e) {
            System.err.println("Error updating camera status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get ongoing alerts for a camera
     */
    public List<AlertsHistory> getOngoingAlerts(Long cameraId) {
        return alertsHistoryRepository.findByCameraIdAndPerformedAtIsNull(cameraId.intValue());
    }

    /**
     * Get all alerts history for a camera (for camera history display)
     */
    public List<AlertsHistory> getCameraHistory(Long cameraId) {
        return alertsHistoryRepository.findByCameraIdOrderByStartAlertDesc(cameraId.intValue());
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
        return cameraRepository.findById(cameraId.intValue());
    }

    // Inner classes for request/response objects
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
        private Integer alertId;
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
        private Integer alertId;
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
        private Integer alertId;

        public StatusChangeResult(String message, Integer alertId) {
            this.message = message;
            this.alertId = alertId;
        }

        public String getMessage() { return message; }
        public Integer getAlertId() { return alertId; }
    }
}