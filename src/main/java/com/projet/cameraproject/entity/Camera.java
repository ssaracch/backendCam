package com.projet.cameraproject.entity;

import jakarta.persistence.*;

@Entity
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idCamera;

    private String nomCamera;

    private String location;

    private String ipAdress;

    private String macAdress;

    @Enumerated(EnumType.STRING)
    private Status statusCamera;

    @ManyToOne
    @JoinColumn(name = "id_User", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_Groupe", nullable = false)
    private Groupe groupe;

    public enum Status {
        normal, offline, blurry
    }

    // === Getters & Setters ===

    public int getIdCamera() {
        return idCamera;
    }

    public void setIdCamera(int idCamera) {
        this.idCamera = idCamera;
    }

    public String getNomCamera() {
        return nomCamera;
    }

    public void setNomCamera(String nomCamera) {
        this.nomCamera = nomCamera;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public String getMacAdress() {
        return macAdress;
    }

    public void setMacAdress(String macAdress) {
        this.macAdress = macAdress;
    }

    public Status getStatusCamera() {
        return statusCamera;
    }

    public void setStatusCamera(Status statusCamera) {
        this.statusCamera = statusCamera;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }
}
