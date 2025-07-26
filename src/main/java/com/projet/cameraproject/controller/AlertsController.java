package com.projet.cameraproject.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.projet.cameraproject.entity.Alerts;
import com.projet.cameraproject.service.AlertsService;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin
public class AlertsController {

    @Autowired
    private AlertsService service;

    // Récupérer toutes les alertes
    @GetMapping
    public List<Alerts> getAll() {
        return service.getAll();
    }

    // Créer une nouvelle alerte
    @PostMapping
    public Alerts save(@RequestBody Alerts alert) {
        // Ici tu peux ajouter des validations si besoin
        return service.save(alert);
    }

    // Mettre à jour une alerte existante
    @PutMapping("/{id}")
    public Alerts update(@PathVariable int id, @RequestBody Alerts alert) {
        Optional<Alerts> existingAlert = service.getById(id);
        if (existingAlert.isPresent()) {
            Alerts toUpdate = existingAlert.get();

            // Ne pas changer l'id
            alert.setId_Alert(id);

            // Mettre à jour les champs (type, camera)
            toUpdate.setType(alert.getType());
            toUpdate.setCamera(alert.getCamera());

            return service.save(toUpdate);
        } else {
            // Ici, tu peux gérer le cas d'alerte non trouvée (throw exception par ex)
            return null; // ou lance une exception
        }
    }

    // Supprimer une alerte
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    
}
