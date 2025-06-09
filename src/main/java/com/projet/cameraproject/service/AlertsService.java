package com.projet.cameraproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projet.cameraproject.entity.Alerts;
import com.projet.cameraproject.repository.AlertsRepository;

@Service
public class AlertsService {

    @Autowired
    private AlertsRepository repository;

    // Récupère toutes les alertes
    public List<Alerts> getAll() {
        return repository.findAll();
    }

    // Sauvegarde une nouvelle alerte
    public Alerts save(Alerts alert) {
        return repository.save(alert);
    }

    // Met à jour une alerte existante selon son id
    public Alerts update(int id, Alerts a) {
        Optional<Alerts> existingAlert = repository.findById(id);
        if (existingAlert.isPresent()) {
            Alerts alertToUpdate = existingAlert.get();
            // Mets à jour les champs nécessaires (exemple ici)
            alertToUpdate.setType(a.getType());
            alertToUpdate.setCamera(a.getCamera());
            return repository.save(alertToUpdate);
        } else {
            // Si l'alerte n'existe pas, tu peux choisir de créer une nouvelle ou renvoyer null / exception
            a.setId_Alert(id);
            return repository.save(a);
        }
    }

    // Supprime une alerte par son id
    public void delete(int id) {
        repository.deleteById(id);
    }

    public Optional<Alerts> getById(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
