package com.projet.cameraproject.service;

import com.projet.cameraproject.entity.Groupe;
import com.projet.cameraproject.repository.GroupeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupeService {

    @Autowired
    private GroupeRepository groupeRepository;

    public List<Groupe> getAllGroupes() {
        return groupeRepository.findAll();
    }
}
