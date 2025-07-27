package com.projet.cameraproject.controller;

import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@CrossOrigin(origins = "*") // Allow all origins for now
public class UserController {

    @Autowired
    private UserService service;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get all users
    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    } 

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        try {
            System.out.println("Login attempt for user: " + loginUser.getNom_User());
            
            List<User> users = service.getAll();
            Optional<User> foundUser = users.stream()
                .filter(user -> user.getNom_User().equals(loginUser.getNom_User()))
                .findFirst();
            
            if (foundUser.isPresent()) {
                User user = foundUser.get();
                System.out.println("User found, checking password...");
                System.out.println("Stored password: " + user.getPassword());
                System.out.println("Login password: " + loginUser.getPassword());
                
                // Since your passwords are stored as plain text, use simple comparison
                // TODO: Later you should encode existing passwords and use passwordEncoder.matches()
                if (user.getPassword().equals(loginUser.getPassword())) {
                    System.out.println("Password matches, login successful");
                    // Remove password from response for security
                    User responseUser = new User();
                    responseUser.setId_User(user.getId_User());
                    responseUser.setNom_User(user.getNom_User());
                    // Don't include password in response
                    
                    return ResponseEntity.ok(responseUser);
                } else {
                    System.out.println("Password does not match");
                }
            } else {
                System.out.println("User not found");
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed");
        }
    }

    // TODO: Use this later to encode existing plain text passwords
    @PostMapping("/encode-existing-passwords")
    public ResponseEntity<?> encodeExistingPasswords() {
        try {
            List<User> users = service.getAll();
            for (User user : users) {
                // Only encode if not already encoded (BCrypt hashes start with $2a$)
                if (!user.getPassword().startsWith("$2a$")) {
                    String plainPassword = user.getPassword();
                    user.setPassword(passwordEncoder.encode(plainPassword));
                    // Save without double encoding by calling repository directly
                    // You might need to modify this based on your repository setup
                }
            }
            return ResponseEntity.ok("Passwords encoded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to encode passwords");
        }
    }
}