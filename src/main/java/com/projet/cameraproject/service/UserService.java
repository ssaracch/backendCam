package com.projet.cameraproject.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projet.cameraproject.entity.User;
import com.projet.cameraproject.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<User> getAll() {
        return repository.findAll();
    }

    public User save(User user) {
    // Encoder le mot de passe avant d'enregistrer
    String encodedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encodedPassword);
    return repository.save(user);
}


    public User update(int id, User u) {
    Optional<User> existingUser = repository.findById(id);
    if (existingUser.isPresent()) {
        User userToUpdate = existingUser.get();
        userToUpdate.setNom_User(u.getNom_User());

        // Encoder le nouveau mot de passe
        String encodedPassword = passwordEncoder.encode(u.getPassword());
        userToUpdate.setPassword(encodedPassword);

        return repository.save(userToUpdate);
    } else {
        u.setId_User(id);
        // Encoder le mot de passe même dans le cas d'une création
        String encodedPassword = passwordEncoder.encode(u.getPassword());
        u.setPassword(encodedPassword);
        return repository.save(u);
    }
}


    public void delete(int id) {
        repository.deleteById(id);
    }
}
