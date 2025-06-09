package com.projet.cameraproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user") // Important car "User" est un mot réservé dans certains cas
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_User;

    @Column(nullable = false)
    private String Nom_User;

    @Column(nullable = false)
    private String password;

    // === Getters & Setters ===

    public int getId_User() {
        return id_User;
    }

    public void setId_User(int id_User) {
        this.id_User = id_User;
    }

    public String getNom_User() {
        return Nom_User;
    }

    public void setNom_User(String nom_User) {
        this.Nom_User = nom_User;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
