package com.projet.cameraproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projet.cameraproject.entity.User;
import com.projet.cameraproject.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public List<User> getAll() {
        return repository.findAll();
    }

    public User save(User user) {
        return repository.save(user);
    }

    public User update(int id, User u) {
        Optional<User> existingUser = repository.findById(id);
        if (existingUser.isPresent()) {
            User userToUpdate = existingUser.get();
            // Mettre à jour les champs nécessaires
            userToUpdate.setNom_User(u.getNom_User());
            userToUpdate.setPassword(u.getPassword());
            return repository.save(userToUpdate);
        } else {
            // Optionnel : si l'utilisateur n'existe pas, on crée un nouveau avec cet id
            u.setId_User(id);
            return repository.save(u);
        }
    }

    public void delete(int id) {
        repository.deleteById(id);
    }
}
