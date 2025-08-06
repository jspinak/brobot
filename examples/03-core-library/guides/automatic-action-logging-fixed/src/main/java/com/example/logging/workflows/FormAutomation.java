package com.example.logging.workflows;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates form automation with comprehensive logging.
 * Shows best practices for logging multi-step workflows.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FormAutomation {
    
    private final Action action;
    
    // Form elements
    private final StateImage firstNameField = new StateImage.Builder()
        .setName("FirstNameField")
        .addPatterns("forms/first_name_field.png")
        .build();
        
    private final StateImage lastNameField = new StateImage.Builder()
        .setName("LastNameField")
        .addPatterns("forms/last_name_field.png")
        .build();
        
    private final StateImage emailField = new StateImage.Builder()
        .setName("EmailField")
        .addPatterns("forms/email_field.png")
        .build();
        
    private final StateImage submitButton = new StateImage.Builder()
        .setName("SubmitButton")
        .addPatterns("forms/submit_button.png")
        .build();
    
    /**
     * Fills and submits a form with detailed logging
     */
    public void fillAndSubmitForm(Map<String, String> formData) {
        log.info("=== Form Automation Workflow ===");
        MDC.put("workflow", "form-automation");
        MDC.put("form_id", "contact-form");
        
        try {
            // Log form data (be careful with sensitive data in production)
            log.info("Starting form submission with data:");
            log.info("  First Name: {}", formData.get("firstName"));
            log.info("  Last Name: {}", formData.get("lastName"));
            log.info("  Email: {}", formData.get("email"));
            
            // Execute form filling sequence
            MDC.put("step", "execute-form-filling");
            log.info("Executing form fill sequence");
            
            long startTime = System.currentTimeMillis();
            boolean success = executeFormFilling(formData);
            long duration = System.currentTimeMillis() - startTime;
            
            MDC.put("duration_ms", String.valueOf(duration));
            
            if (success) {
                log.info("✓ Form submitted successfully in {}ms", duration);
                MDC.put("result", "success");
                
                // Log form submission details
                log.info("Form submission details:");
                log.info("  - First Name: {}", formData.get("firstName"));
                log.info("  - Last Name: {}", formData.get("lastName"));
                log.info("  - Email: {}", formData.get("email"));
                log.info("  - Total duration: {}ms", duration);
                
            } else {
                log.error("✗ Form submission failed after {}ms", duration);
                MDC.put("result", "failure");
                log.error("Failure details:");
                log.error("  - Form filling sequence failed");
                
                // Attempt recovery
                attemptFormRecovery(formData);
            }
            
        } catch (Exception e) {
            log.error("Form automation failed with exception", e);
            MDC.put("result", "exception");
            MDC.put("exception", e.getClass().getSimpleName());
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Executes the form filling sequence
     */
    private boolean executeFormFilling(Map<String, String> formData) {
        log.debug("Executing form filling sequence");
        
        // Step 1: Find and click first name field
        log.debug("Step 1: First name field");
        ObjectCollection firstNameCollection = new ObjectCollection.Builder()
            .withImages(firstNameField)
            .build();
            
        ActionResult firstNameResult = action.perform(
            new PatternFindOptions.Builder().setSimilarity(0.8).build(),
            firstNameCollection
        );
        
        if (!firstNameResult.isSuccess()) {
            log.error("First name field not found");
            return false;
        }
        
        // Click and type first name
        action.perform(new ClickOptions.Builder().setPauseAfterEnd(0.2).build(), firstNameCollection);
        action.perform(
            new TypeOptions.Builder().setTypeDelay(0.05).build(),
            new ObjectCollection.Builder().withStrings(formData.get("firstName")).build()
        );
        log.debug("First name entered");
        
        // Step 2: Tab to last name field and enter data
        log.debug("Step 2: Last name field");
        action.perform(
            new TypeOptions.Builder().setTypeDelay(0.1).build(),
            new ObjectCollection.Builder().withStrings("{TAB}").build()
        );
        
        action.perform(
            new TypeOptions.Builder().setTypeDelay(0.05).build(),
            new ObjectCollection.Builder().withStrings(formData.get("lastName")).build()
        );
        log.debug("Last name entered");
        
        // Step 3: Tab to email field and enter data
        log.debug("Step 3: Email field");
        action.perform(
            new TypeOptions.Builder().setTypeDelay(0.1).build(),
            new ObjectCollection.Builder().withStrings("{TAB}").build()
        );
        
        action.perform(
            new TypeOptions.Builder().setTypeDelay(0.05).build(),
            new ObjectCollection.Builder().withStrings(formData.get("email")).build()
        );
        log.debug("Email entered");
        
        // Step 4: Find and click submit button
        log.debug("Step 4: Submit form");
        ObjectCollection submitCollection = new ObjectCollection.Builder()
            .withImages(submitButton)
            .build();
            
        ActionResult submitResult = action.perform(
            new PatternFindOptions.Builder().setSimilarity(0.9).build(),
            submitCollection
        );
        
        if (submitResult.isSuccess()) {
            action.perform(new ClickOptions.Builder().setPauseAfterEnd(1.0).build(), submitCollection);
            log.debug("Form submitted");
            return true;
        } else {
            log.error("Submit button not found");
            return false;
        }
    }
    
    /**
     * Attempts to recover from form submission failure
     */
    private void attemptFormRecovery(Map<String, String> formData) {
        log.warn("Attempting form recovery");
        MDC.put("step", "recovery");
        
        // Check if form is still visible
        StateImage formIndicator = new StateImage.Builder()
            .setName("FormTitle")
            .addPatterns("form_title.png")
            .build();
            
        ObjectCollection formCheckObjects = new ObjectCollection.Builder()
            .withImages(formIndicator)
            .build();
            
        ActionResult formCheck = action.perform(
            new PatternFindOptions.Builder()
                .setSimilarity(0.7)
                .build(),
            formCheckObjects
        );
        
        if (formCheck.isSuccess()) {
            log.info("Form still visible, attempting field-by-field submission");
            fillFormFieldByField(formData);
        } else {
            log.error("Form no longer visible, recovery not possible");
        }
    }
    
    /**
     * Fills form field by field (fallback method)
     */
    private void fillFormFieldByField(Map<String, String> formData) {
        log.info("Using field-by-field approach");
        
        // First name
        if (fillField("first_name_field", formData.get("firstName"), "First Name")) {
            log.info("First name filled successfully");
        }
        
        // Last name
        if (fillField("last_name_field", formData.get("lastName"), "Last Name")) {
            log.info("Last name filled successfully");
        }
        
        // Email
        if (fillField("email_field", formData.get("email"), "Email")) {
            log.info("Email filled successfully");
        }
        
        // Submit
        submitForm();
    }
    
    /**
     * Fills a single form field
     */
    private boolean fillField(String fieldPattern, String value, String fieldName) {
        MDC.put("field", fieldName);
        log.debug("Filling field: {}", fieldName);
        
        StateImage field = new StateImage.Builder()
            .setName(fieldName)
            .addPatterns(fieldPattern + ".png")
            .build();
            
        ObjectCollection fieldObjects = new ObjectCollection.Builder()
            .withImages(field)
            .build();
            
        // Find field
        ActionResult findResult = action.perform(
            new PatternFindOptions.Builder()
                .setSimilarity(0.8)
                .build(),
            fieldObjects
        );
        
        if (!findResult.isSuccess()) {
            log.error("Field {} not found", fieldName);
            return false;
        }
        
        // Click field
        action.perform(new ClickOptions.Builder().build(), fieldObjects);
        
        // Clear field (Ctrl+A, Delete)
        action.perform(
            new TypeOptions.Builder().build(),
            new ObjectCollection.Builder()
                .withStrings("^a", "{DELETE}")
                .build()
        );
        
        // Type value
        action.perform(
            new TypeOptions.Builder()
                .setTypeDelay(0.05)
                .build(),
            new ObjectCollection.Builder()
                .withStrings(value)
                .build()
        );
        
        log.debug("Field {} filled with value", fieldName);
        MDC.remove("field");
        
        return true;
    }
    
    /**
     * Submits the form
     */
    private void submitForm() {
        log.info("Submitting form");
        
        StateImage submitButton = new StateImage.Builder()
            .setName("SubmitButton")
            .addPatterns("submit_button.png")
            .build();
            
        ObjectCollection submitObjects = new ObjectCollection.Builder()
            .withImages(submitButton)
            .build();
            
        ActionResult result = action.perform(
            new ClickOptions.Builder()
                .setPauseAfterEnd(2.0)
                .build(),
            submitObjects
        );
        
        if (result.isSuccess()) {
            log.info("Form submitted via recovery method");
        } else {
            log.error("Failed to submit form even with recovery");
        }
    }
    
    /**
     * Demonstrates form automation
     */
    public void demonstrateFormAutomation() {
        log.info("=== Form Automation Demonstration ===");
        
        // Create sample form data
        Map<String, String> formData = new HashMap<>();
        formData.put("firstName", "John");
        formData.put("lastName", "Doe");
        formData.put("email", "john.doe@example.com");
        
        // Execute form automation
        fillAndSubmitForm(formData);
        
        log.info("Form automation demonstration completed");
    }
}