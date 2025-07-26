// Updated AlertsHistory entity with new columns for AI integration

package com.projet.cameraproject.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "alerts_history")
@IdClass(AlertsHistoryId.class)
public class AlertsHistory {

    @Id
    @Column(name = "id_User")
    private int userId;

    @Id
    @Column(name = "id_Camera")
    private int cameraId;

    @Id
    @Column(name = "id_Alert")
    private int alertId;

    @ManyToOne
    @JoinColumn(name = "id_User", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_Camera", insertable = false, updatable = false)
    private Camera camera;

    @ManyToOne
    @JoinColumn(name = "id_Alert", insertable = false, updatable = false)
    private Alerts alert;

    @Column(name = "start_alert", nullable = false)
    private LocalDateTime start_alert;
    
    @Column(name = "performed_at", nullable = true)
    private LocalDateTime performed_at;

    // NEW COLUMNS for AI integration
    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "source", length = 50)
    private String source = "MANUAL";
    
    @Column(name = "captured_image", columnDefinition = "LONGTEXT")
    private String capturedImage;

    // Constructors
    public AlertsHistory() {}

    public AlertsHistory(int userId, int cameraId, int alertId) {
        this.userId = userId;
        this.cameraId = cameraId;
        this.alertId = alertId;
    }

    // Getters and setters for existing fields
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCameraId() {
        return cameraId;
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Alerts getAlert() {
        return alert;
    }

    public void setAlert(Alerts alert) {
        this.alert = alert;
    }

    public LocalDateTime getStart_alert() {
        return start_alert;
    }

    public void setStart_alert(LocalDateTime start_alert) {
        this.start_alert = start_alert;
    }

    public LocalDateTime getPerformed_at() {
        return performed_at;
    }

    public void setPerformed_at(LocalDateTime performed_at) {
        this.performed_at = performed_at;
    }

    // Getters and setters for NEW fields
    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCapturedImage() {
        return capturedImage;
    }

    public void setCapturedImage(String capturedImage) {
        this.capturedImage = capturedImage;
    }

    @Override
    public String toString() {
        return "AlertsHistory{" +
                "userId=" + userId +
                ", cameraId=" + cameraId +
                ", alertId=" + alertId +
                ", start_alert=" + start_alert +
                ", performed_at=" + performed_at +
                ", confidence=" + confidence +
                ", source='" + source + '\'' +
                ", capturedImage='" + (capturedImage != null ? "present" : "null") + '\'' +
                '}';
    }
}
