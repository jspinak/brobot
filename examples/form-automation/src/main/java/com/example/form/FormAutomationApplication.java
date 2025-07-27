package com.example.form;

import com.example.form.automation.FormFiller;
import com.example.form.automation.FormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Form Automation Example Application
 * 
 * Demonstrates advanced form filling techniques with automatic logging,
 * error handling, and validation.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.form",
    "io.github.jspinak.brobot"
})
@RequiredArgsConstructor
@Slf4j
public class FormAutomationApplication implements CommandLineRunner {
    
    private final FormFiller formFiller;
    private final FormValidator formValidator;
    
    public static void main(String[] args) {
        SpringApplication.run(FormAutomationApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Form Automation Example ===");
        log.info("This example demonstrates automatic logging for form interactions");
        
        // Example 1: Simple form with text fields
        demonstrateSimpleForm();
        
        // Example 2: Complex form with various input types
        demonstrateComplexForm();
        
        // Example 3: Form with validation and retry
        demonstrateFormWithValidation();
        
        log.info("Form automation examples completed!");
    }
    
    private void demonstrateSimpleForm() {
        log.info("\n--- Example 1: Simple Contact Form ---");
        
        Map<String, String> contactData = new LinkedHashMap<>();
        contactData.put("firstName", "John");
        contactData.put("lastName", "Doe");
        contactData.put("email", "john.doe@example.com");
        contactData.put("phone", "555-1234");
        
        boolean success = formFiller.fillForm(contactData);
        
        if (success) {
            log.info("✓ Simple form filled successfully");
        } else {
            log.error("✗ Failed to fill simple form");
        }
    }
    
    private void demonstrateComplexForm() {
        log.info("\n--- Example 2: Complex Registration Form ---");
        
        Map<String, String> registrationData = new LinkedHashMap<>();
        registrationData.put("username", "johndoe2024");
        registrationData.put("password", "SecurePass123!");
        registrationData.put("confirmPassword", "SecurePass123!");
        registrationData.put("firstName", "John");
        registrationData.put("lastName", "Doe");
        registrationData.put("email", "john.doe@example.com");
        registrationData.put("country", "United States");  // Dropdown
        registrationData.put("agreeTerms", "true");       // Checkbox
        
        boolean success = formFiller.fillComplexForm(registrationData);
        
        if (success) {
            log.info("✓ Complex form filled successfully");
        } else {
            log.error("✗ Failed to fill complex form");
        }
    }
    
    private void demonstrateFormWithValidation() {
        log.info("\n--- Example 3: Form with Validation ---");
        
        Map<String, String> userData = new LinkedHashMap<>();
        userData.put("email", "invalid-email");  // Will trigger validation error
        userData.put("age", "150");              // Out of range
        userData.put("zipCode", "ABCDE");        // Invalid format
        
        // First attempt will fail
        boolean success = formFiller.fillFormWithValidation(userData);
        
        if (!success) {
            log.info("First attempt failed as expected due to validation");
            
            // Fix the data
            userData.put("email", "valid@example.com");
            userData.put("age", "25");
            userData.put("zipCode", "12345");
            
            // Retry with valid data
            success = formFiller.fillFormWithValidation(userData);
            
            if (success) {
                log.info("✓ Form filled successfully after fixing validation errors");
            }
        }
    }
}