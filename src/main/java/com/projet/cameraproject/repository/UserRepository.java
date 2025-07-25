package com.projet.cameraproject.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.projet.cameraproject.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    
}
