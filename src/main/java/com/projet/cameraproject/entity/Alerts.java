package com.projet.cameraproject.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Alerts {

    public enum AlertType {
        offline,
        blurry
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_Alert;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    @ManyToOne
    @JoinColumn(name = "id_Camera", nullable = false)
    private Camera camera;

    // === Getters & Setters ===

    public int getId_Alert() {
        return id_Alert;
    }

    public void setId_Alert(int id_Alert) {
        this.id_Alert = id_Alert;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }
}
