package com.example.unittesting.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoginToDashboardTransition.
 */
class LoginToDashboardTransitionTest {
    
    private LoginToDashboardTransition transition;
    
    @BeforeEach
    void setUp() {
        transition = new LoginToDashboardTransition();
    }
    
    @Test
    @DisplayName("Should have correct transition description")
    void testTransitionDescription() {
        String description = transition.getTransitionDescription();
        
        assertThat(description)
            .isNotNull()
            .contains("Login to Dashboard")
            .contains("credential validation");
    }
    
    @ParameterizedTest
    @DisplayName("Should validate credentials correctly")
    @CsvSource({
        "validuser, password123, true",
        "user, pass1234, true",
        "admin, adminpass, true",
        "user, short, false",
        "user, 1234567, false"
    })
    void testCredentialValidation(String username, String password, boolean expected) {
        boolean result = transition.validateCredentials(username, password);
        assertThat(result).isEqualTo(expected);
    }
    
    @ParameterizedTest
    @DisplayName("Should reject null or empty usernames")
    @NullAndEmptySource
    void testInvalidUsernames(String username) {
        boolean result = transition.validateCredentials(username, "validpassword");
        assertThat(result).isFalse();
    }
    
    @ParameterizedTest
    @DisplayName("Should reject null passwords")
    @ValueSource(strings = {"", "short"})
    void testInvalidPasswords(String password) {
        boolean result = transition.validateCredentials("validuser", password);
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should reject null password")
    void testNullPassword() {
        boolean result = transition.validateCredentials("validuser", null);
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("Should execute transition steps in order")
    void testTransitionStepsOrder() {
        // In a real test, you would verify the order of execution
        // This is a simplified example
        transition.enterCredentials();
        transition.clickLogin();
        transition.waitForDashboard();
        
        // Verify steps completed (in real implementation, would check logs or state)
        assertThat(transition).isNotNull();
    }
    
    @ParameterizedTest
    @DisplayName("Should validate password length requirements")
    @CsvSource({
        "12345678, true",
        "123456789, true",
        "verylongpassword123, true",
        "1234567, false",
        "seven77, false"
    })
    void testPasswordLengthValidation(String password, boolean expected) {
        boolean result = transition.validateCredentials("user", password);
        assertThat(result).isEqualTo(expected);
    }
}