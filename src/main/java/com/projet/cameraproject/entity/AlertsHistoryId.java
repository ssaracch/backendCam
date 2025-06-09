package com.projet.cameraproject.entity;

import java.io.Serializable;
import java.util.Objects;

public class AlertsHistoryId implements Serializable {

    private int userId;    // the name must be exactly "user" as the field in AlertsHistory is "User user"
    private int cameraId;  // same for camera
    private int alertId;   // same for alert

    public AlertsHistoryId() {}

    public AlertsHistoryId(int userId, int cameraId, int alertId) {
        this.userId = userId;
        this.cameraId = cameraId;
        this.alertId = alertId;
    }

    public int getUser() {
        return userId;
    }

    public void setUser(int userId) {
        this.userId = userId;
    }

    public int getCamera() {
        return cameraId;
    }

    public void setCamera(int cameraId) {
        this.cameraId = cameraId;
    }

    public int getAlert() {
        return alertId;
    }

    public void setAlert(int alertId) {
        this.alertId = alertId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertsHistoryId)) return false;
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
