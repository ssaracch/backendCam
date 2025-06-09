package com.projet.cameraproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.entity.AlertsHistoryId;
import com.projet.cameraproject.repository.AlertsHistoryRepository;

@Service
public class AlertsHistoryService {

    @Autowired
    private AlertsHistoryRepository repository;

    // Récupère toutes les entrées d'historique d'alertes
    public List<AlertsHistory> getAll() {
        return repository.findAll();
    }

    // Enregistre une nouvelle entrée dans l'historique des alertes
    public AlertsHistory save(AlertsHistory ah) {
        return repository.save(ah);
    }

    // Supprime une entrée d'historique selon sa clé composite
    public void delete(AlertsHistoryId id) {
        repository.deleteById(id);
    }
}
