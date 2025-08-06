package com.example.unittesting.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for creating test data.
 */
public class TestDataBuilder {
    
    /**
     * Creates test user data.
     */
    public static class UserBuilder {
        private String username = "testuser";
        private String password = "password123";
        private String email = "test@example.com";
        private List<String> roles = new ArrayList<>();
        
        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }
        
        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public UserBuilder withRole(String role) {
            this.roles.add(role);
            return this;
        }
        
        public TestUser build() {
            return new TestUser(username, password, email, roles);
        }
    }
    
    /**
     * Creates test state data.
     */
    public static class StateBuilder {
        private String name = "testState";
        private Map<String, String> stateObjects = new HashMap<>();
        private Map<String, String> regions = new HashMap<>();
        private boolean isActive = true;
        
        public StateBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public StateBuilder withStateObject(String name, String imagePath) {
            this.stateObjects.put(name, imagePath);
            return this;
        }
        
        public StateBuilder withRegion(String name, String coordinates) {
            this.regions.put(name, coordinates);
            return this;
        }
        
        public StateBuilder isActive(boolean active) {
            this.isActive = active;
            return this;
        }
        
        public TestState build() {
            return new TestState(name, stateObjects, regions, isActive);
        }
    }
    
    /**
     * Creates test action results.
     */
    public static class ActionResultBuilder {
        private boolean success = true;
        private String actionType = "CLICK";
        private String target = "button";
        private long duration = 100;
        private String errorMessage = null;
        
        public ActionResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public ActionResultBuilder actionType(String actionType) {
            this.actionType = actionType;
            return this;
        }
        
        public ActionResultBuilder target(String target) {
            this.target = target;
            return this;
        }
        
        public ActionResultBuilder duration(long duration) {
            this.duration = duration;
            return this;
        }
        
        public ActionResultBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public TestActionResult build() {
            return new TestActionResult(success, actionType, target, duration, errorMessage);
        }
    }
    
    // Helper methods for common test data
    public static TestUser defaultUser() {
        return new UserBuilder().build();
    }
    
    public static TestUser adminUser() {
        return new UserBuilder()
            .withUsername("admin")
            .withPassword("adminpass")
            .withRole("ADMIN")
            .withRole("USER")
            .build();
    }
    
    public static TestState loginState() {
        return new StateBuilder()
            .withName("login")
            .withStateObject("username_field", "username_input.png")
            .withStateObject("password_field", "password_input.png")
            .withStateObject("login_button", "login_btn.png")
            .build();
    }
    
    public static TestActionResult successfulClick() {
        return new ActionResultBuilder()
            .success(true)
            .actionType("CLICK")
            .duration(50)
            .build();
    }
    
    public static TestActionResult failedClick(String reason) {
        return new ActionResultBuilder()
            .success(false)
            .actionType("CLICK")
            .duration(2000)
            .errorMessage(reason)
            .build();
    }
    
    // Test data classes
    public static class TestUser {
        public final String username;
        public final String password;
        public final String email;
        public final List<String> roles;
        
        TestUser(String username, String password, String email, List<String> roles) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.roles = new ArrayList<>(roles);
        }
    }
    
    public static class TestState {
        public final String name;
        public final Map<String, String> stateObjects;
        public final Map<String, String> regions;
        public final boolean isActive;
        
        TestState(String name, Map<String, String> stateObjects, Map<String, String> regions, boolean isActive) {
            this.name = name;
            this.stateObjects = new HashMap<>(stateObjects);
            this.regions = new HashMap<>(regions);
            this.isActive = isActive;
        }
    }
    
    public static class TestActionResult {
        public final boolean success;
        public final String actionType;
        public final String target;
        public final long duration;
        public final String errorMessage;
        
        TestActionResult(boolean success, String actionType, String target, long duration, String errorMessage) {
            this.success = success;
            this.actionType = actionType;
            this.target = target;
            this.duration = duration;
            this.errorMessage = errorMessage;
        }
    }
}