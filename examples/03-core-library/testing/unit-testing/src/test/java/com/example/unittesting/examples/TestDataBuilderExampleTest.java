package com.example.unittesting.examples;

import com.example.unittesting.utils.TestDataBuilder;
import com.example.unittesting.utils.TestAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Examples of using TestDataBuilder for cleaner tests.
 */
class TestDataBuilderExampleTest {
    
    @Test
    @DisplayName("Should create test users with builder")
    void testUserBuilder() {
        // Create a custom user
        TestDataBuilder.TestUser customUser = new TestDataBuilder.UserBuilder()
            .withUsername("john.doe")
            .withPassword("securePass123")
            .withEmail("john.doe@example.com")
            .withRole("USER")
            .withRole("PREMIUM")
            .build();
        
        // Use custom assertions
        TestAssertions.UserAssert.assertThat(customUser)
            .hasUsername("john.doe")
            .hasRole("USER")
            .hasRole("PREMIUM")
            .hasRoleCount(2)
            .hasValidEmail();
    }
    
    @Test
    @DisplayName("Should create predefined test data")
    void testPredefinedData() {
        // Use predefined data
        TestDataBuilder.TestUser defaultUser = TestDataBuilder.defaultUser();
        TestDataBuilder.TestUser adminUser = TestDataBuilder.adminUser();
        
        assertThat(defaultUser.username).isEqualTo("testuser");
        assertThat(adminUser.roles).contains("ADMIN");
    }
    
    @Test
    @DisplayName("Should create test states with builder")
    void testStateBuilder() {
        // Create a complex state
        TestDataBuilder.TestState settingsState = new TestDataBuilder.StateBuilder()
            .withName("settings")
            .withStateObject("save_button", "save_btn.png")
            .withStateObject("cancel_button", "cancel_btn.png")
            .withStateObject("theme_dropdown", "theme_select.png")
            .withRegion("settings_panel", "100,100,800,600")
            .isActive(true)
            .build();
        
        // Use custom assertions
        TestAssertions.StateAssert.assertThat(settingsState)
            .hasName("settings")
            .isActive()
            .hasStateObject("save_button")
            .hasStateObject("theme_dropdown")
            .hasStateObjectCount(3)
            .hasRegion("settings_panel");
    }
    
    @Test
    @DisplayName("Should create action results with builder")
    void testActionResultBuilder() {
        // Create successful action
        TestDataBuilder.TestActionResult successResult = 
            TestDataBuilder.successfulClick();
        
        // Create failed action
        TestDataBuilder.TestActionResult failedResult = 
            TestDataBuilder.failedClick("Element not found");
        
        // Create custom action result
        TestDataBuilder.TestActionResult customResult = 
            new TestDataBuilder.ActionResultBuilder()
                .actionType("TYPE")
                .target("search_field")
                .success(true)
                .duration(250)
                .build();
        
        // Verify with custom assertions
        TestAssertions.ActionResultAssert.assertThat(successResult)
            .isSuccessful()
            .hasActionType("CLICK")
            .hasDurationLessThan(100);
        
        TestAssertions.ActionResultAssert.assertThat(failedResult)
            .hasFailed()
            .hasErrorMessage("Element not found");
        
        TestAssertions.ActionResultAssert.assertThat(customResult)
            .isSuccessful()
            .hasActionType("TYPE")
            .hasDurationLessThan(300);
    }
    
    @Test
    @DisplayName("Should use builders for complex test scenarios")
    void testComplexScenario() {
        // Create test data for a login scenario
        TestDataBuilder.TestState loginState = TestDataBuilder.loginState();
        TestDataBuilder.TestUser testUser = TestDataBuilder.defaultUser();
        
        // Simulate login attempt
        TestDataBuilder.TestActionResult clickUsername = 
            new TestDataBuilder.ActionResultBuilder()
                .actionType("CLICK")
                .target(loginState.stateObjects.get("username_field"))
                .success(true)
                .duration(45)
                .build();
        
        TestDataBuilder.TestActionResult typeUsername = 
            new TestDataBuilder.ActionResultBuilder()
                .actionType("TYPE")
                .target(loginState.stateObjects.get("username_field"))
                .success(true)
                .duration(300)
                .build();
        
        // Verify the scenario
        assertThat(loginState.stateObjects).containsKey("login_button");
        assertThat(testUser.username).isNotEmpty();
        assertThat(clickUsername.success).isTrue();
        assertThat(typeUsername.duration).isLessThan(500);
    }
}