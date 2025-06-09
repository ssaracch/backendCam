package com.projet.cameraproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projet.cameraproject.entity.AlertsHistory;
import com.projet.cameraproject.entity.AlertsHistoryId;
import com.projet.cameraproject.service.AlertsHistoryService;

@RestController
@RequestMapping ("/api/alerts-history")
@CrossOrigin
public class AlertsHistoryController {

    @Autowired
    private AlertsHistoryService service;

    // Récupérer tous les historiques d'alertes
    @GetMapping
    public List<AlertsHistory> getAll() {
        return service.getAll();
    }

    // Ajouter un nouvel historique d'alerte
    @PostMapping
    public AlertsHistory save(@RequestBody AlertsHistory alertsHistory) {
        return service.save(alertsHistory);
    }

    // Supprimer un historique d'alerte via la clé composite (envoyée dans le corps de la requête)
    @DeleteMapping
    public void delete(@RequestBody AlertsHistoryId id) {
        service.delete(id);
    }
}
