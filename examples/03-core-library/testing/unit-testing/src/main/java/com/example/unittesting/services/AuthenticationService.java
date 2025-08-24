package com.example.unittesting.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for authentication logic - used in testing examples.
 */
@Service
@Slf4j
public class AuthenticationService {
    
    private final Map<String, String> validUsers = new HashMap<>();
    private String currentUser = null;
    
    public AuthenticationService() {
        // Initialize with test users
        validUsers.put("testuser", "password123");
        validUsers.put("admin", "adminpass");
        validUsers.put("demo", "demopass");
    }
    
    public boolean authenticate(String username, String password) {
        log.info("Attempting to authenticate user: {}", username);
        
        if (username == null || password == null) {
            log.warn("Username or password is null");
            return false;
        }
        
        String storedPassword = validUsers.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            currentUser = username;
            log.info("Authentication successful for user: {}", username);
            return true;
        }
        
        log.warn("Authentication failed for user: {}", username);
        return false;
    }
    
    public void logout() {
        log.info("Logging out user: {}", currentUser);
        currentUser = null;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public void addUser(String username, String password) {
        validUsers.put(username, password);
        log.info("Added new user: {}", username);
    }
    
    public boolean userExists(String username) {
        return validUsers.containsKey(username);
    }
}