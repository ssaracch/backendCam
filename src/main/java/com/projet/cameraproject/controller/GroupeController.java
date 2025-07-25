package com.projet.cameraproject.controller;

import com.projet.cameraproject.entity.Groupe;
import com.projet.cameraproject.service.GroupeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groupes")
@CrossOrigin(origins = "http://localhost:4200") // autorise Angular
public class GroupeController {

    @Autowired
    private GroupeService groupeService;

    @GetMapping
    public List<Groupe> getAllGroupes() {
        return groupeService.getAllGroupes();
    }
}
