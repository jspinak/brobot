package com.example.unittesting.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AuthenticationService.
 */
class AuthenticationServiceTest {
    
    private AuthenticationService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthenticationService();
    }
    
    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        
        @Test
        @DisplayName("Should authenticate valid user")
        void testValidAuthentication() {
            boolean result = authService.authenticate("testuser", "password123");
            
            assertThat(result).isTrue();
            assertThat(authService.isAuthenticated()).isTrue();
            assertThat(authService.getCurrentUser()).isEqualTo("testuser");
        }
        
        @ParameterizedTest
        @DisplayName("Should authenticate all default users")
        @CsvSource({
            "testuser, password123",
            "admin, adminpass",
            "demo, demopass"
        })
        void testDefaultUsers(String username, String password) {
            boolean result = authService.authenticate(username, password);
            
            assertThat(result).isTrue();
            assertThat(authService.getCurrentUser()).isEqualTo(username);
        }
        
        @Test
        @DisplayName("Should reject invalid credentials")
        void testInvalidAuthentication() {
            boolean result = authService.authenticate("testuser", "wrongpassword");
            
            assertThat(result).isFalse();
            assertThat(authService.isAuthenticated()).isFalse();
            assertThat(authService.getCurrentUser()).isNull();
        }
        
        @Test
        @DisplayName("Should handle null username")
        void testNullUsername() {
            boolean result = authService.authenticate(null, "password");
            
            assertThat(result).isFalse();
            assertThat(authService.isAuthenticated()).isFalse();
        }
        
        @Test
        @DisplayName("Should handle null password")
        void testNullPassword() {
            boolean result = authService.authenticate("testuser", null);
            
            assertThat(result).isFalse();
            assertThat(authService.isAuthenticated()).isFalse();
        }
    }
    
    @Nested
    @DisplayName("User Management Tests")
    class UserManagementTests {
        
        @Test
        @DisplayName("Should add new user")
        void testAddUser() {
            authService.addUser("newuser", "newpassword");
            
            assertThat(authService.userExists("newuser")).isTrue();
            assertThat(authService.authenticate("newuser", "newpassword")).isTrue();
        }
        
        @Test
        @DisplayName("Should check if user exists")
        void testUserExists() {
            assertThat(authService.userExists("testuser")).isTrue();
            assertThat(authService.userExists("nonexistent")).isFalse();
        }
        
        @Test
        @DisplayName("Should override existing user")
        void testOverrideUser() {
            authService.addUser("testuser", "newpassword");
            
            assertThat(authService.authenticate("testuser", "password123")).isFalse();
            assertThat(authService.authenticate("testuser", "newpassword")).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {
        
        @Test
        @DisplayName("Should logout authenticated user")
        void testLogout() {
            authService.authenticate("testuser", "password123");
            assertThat(authService.isAuthenticated()).isTrue();
            
            authService.logout();
            
            assertThat(authService.isAuthenticated()).isFalse();
            assertThat(authService.getCurrentUser()).isNull();
        }
        
        @Test
        @DisplayName("Should handle logout when not authenticated")
        void testLogoutWhenNotAuthenticated() {
            assertThat(authService.isAuthenticated()).isFalse();
            
            assertThatCode(() -> authService.logout())
                .doesNotThrowAnyException();
            
            assertThat(authService.isAuthenticated()).isFalse();
        }
    }
    
    @Test
    @DisplayName("Should maintain authentication state across operations")
    void testAuthenticationState() {
        // Initially not authenticated
        assertThat(authService.isAuthenticated()).isFalse();
        
        // Authenticate
        authService.authenticate("admin", "adminpass");
        assertThat(authService.isAuthenticated()).isTrue();
        assertThat(authService.getCurrentUser()).isEqualTo("admin");
        
        // Failed authentication should not change current user
        authService.authenticate("testuser", "wrongpass");
        assertThat(authService.isAuthenticated()).isTrue();
        assertThat(authService.getCurrentUser()).isEqualTo("admin");
        
        // Successful authentication should change user
        authService.authenticate("testuser", "password123");
        assertThat(authService.getCurrentUser()).isEqualTo("testuser");
        
        // Logout
        authService.logout();
        assertThat(authService.isAuthenticated()).isFalse();
    }
}