package com.projet.cameraproject.entity;

import java.io.Serializable;
import java.util.Objects;

public class AlertsHistoryId implements Serializable {
    private int userId;
    private int cameraId;
    private int alertId;

    // Default constructor
    public AlertsHistoryId() {}

    // Parameterized constructor
    public AlertsHistoryId(int userId, int cameraId, int alertId) {
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

    // equals and hashCode methods (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertsHistoryId that = (AlertsHistoryId) o;
        return userId == that.userId &&
               cameraId == that.cameraId &&
               alertId == that.alertId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, cameraId, alertId);
    }
}