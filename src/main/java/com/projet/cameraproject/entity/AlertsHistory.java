package com.projet.cameraproject.entity;

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

    // Constructors
    public AlertsHistory() {}

    public AlertsHistory(int userId, int cameraId, int alertId) {
        this.userId = userId;
        this.cameraId = cameraId;
        this.alertId = alertId;
    }

    // Getters and setters
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
}