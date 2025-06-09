package com.projet.cameraproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_Groupe;

    @Column(nullable = false)
    private String nom_Groupe;

    @ManyToOne
    @JoinColumn(name = "id_User", nullable = false)
    private User user;

    // === Getters & Setters ===

    public int getId_Groupe() {
        return id_Groupe;
    }

    public void setId_Groupe(int id_Groupe) {
        this.id_Groupe = id_Groupe;
    }

    public String getNom_Groupe() {
        return nom_Groupe;
    }

    public void setNom_Groupe(String nom_Groupe) {
        this.nom_Groupe = nom_Groupe;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
