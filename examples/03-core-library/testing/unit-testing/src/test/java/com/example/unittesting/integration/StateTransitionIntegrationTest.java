package com.example.unittesting.integration;

import com.example.unittesting.services.AuthenticationService;
import com.example.unittesting.states.LoginState;
import com.example.unittesting.states.DashboardState;
import com.example.unittesting.transitions.LoginToDashboardTransition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for state transitions.
 */
@SpringBootTest
@ActiveProfiles("test")
class StateTransitionIntegrationTest {
    
    @Autowired
    private LoginState loginState;
    
    @Autowired
    private DashboardState dashboardState;
    
    @Autowired
    private LoginToDashboardTransition transition;
    
    @Autowired
    private AuthenticationService authService;
    
    @BeforeEach
    void setUp() {
        authService.logout(); // Ensure clean state
    }
    
    @Test
    @DisplayName("Should wire all components correctly")
    void testComponentWiring() {
        assertThat(loginState).isNotNull();
        assertThat(dashboardState).isNotNull();
        assertThat(transition).isNotNull();
        assertThat(authService).isNotNull();
    }
    
    @Test
    @DisplayName("Should transition from login to dashboard with valid credentials")
    void testSuccessfulTransition() {
        // Verify initial state
        assertThat(authService.isAuthenticated()).isFalse();
        assertThat(loginState.hasRequiredElements()).isTrue();
        
        // Simulate transition with valid credentials
        boolean credentialsValid = transition.validateCredentials("testuser", "password123");
        assertThat(credentialsValid).isTrue();
        
        // Authenticate
        boolean authenticated = authService.authenticate("testuser", "password123");
        assertThat(authenticated).isTrue();
        
        // Verify end state
        assertThat(authService.isAuthenticated()).isTrue();
        assertThat(dashboardState.isUserLoggedIn()).isTrue();
    }
    
    @Test
    @DisplayName("Should not transition with invalid credentials")
    void testFailedTransition() {
        // Attempt transition with invalid credentials
        boolean credentialsValid = transition.validateCredentials("testuser", "wrong");
        assertThat(credentialsValid).isFalse();
        
        // Attempt authentication
        boolean authenticated = authService.authenticate("testuser", "wrong");
        assertThat(authenticated).isFalse();
        
        // Verify still in login state
        assertThat(authService.isAuthenticated()).isFalse();
    }
    
    @Test
    @DisplayName("Should maintain state consistency")
    void testStateConsistency() {
        // Login state should always have required elements
        assertThat(loginState.hasRequiredElements()).isTrue();
        
        // Dashboard state should have navigation elements
        String[] navElements = dashboardState.getNavigationElements();
        assertThat(navElements).hasSize(4);
        
        // States should have proper descriptions
        assertThat(loginState.getStateDescription()).contains("Login");
        assertThat(transition.getTransitionDescription()).contains("Login to Dashboard");
    }
}