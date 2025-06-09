package com.projet.cameraproject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projet.cameraproject.entity.User;
import com.projet.cameraproject.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService service;

    // Récupérer tous les utilisateurs
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    } 

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
    List<User> users = service.getAll();
    for (User user : users) {
        if (user.getNom_User().equals(loginUser.getNom_User())
            && user.getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.ok(user); // Success
        }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
}

}
