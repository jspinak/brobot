package com.example.form.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Form validation helper with automatic logging.
 * 
 * Verifies form state and provides detailed feedback about
 * validation issues through automatic logging.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FormValidator {
    
    private final Action action;
    
    /**
     * Validates that all required fields are filled.
     * Each validation check is automatically logged.
     */
    public boolean validateRequiredFields(List<String> requiredFields) {
        List<String> missingFields = new ArrayList<>();
        
        for (String fieldName : requiredFields) {
            PatternFindOptions checkField = new PatternFindOptions.Builder()
                .withBeforeActionLog("Validating " + fieldName + " field...")
                .withSuccessLog(fieldName + " field is filled")
                .withFailureLog(fieldName + " field is empty or missing")
                .build();
                
            StateImage filledField = new StateImage.Builder()
                .setName(fieldName + "-filled")
                .addPattern("images/" + fieldName + "-filled.png")
                .build();
                
            ActionResult result = action.perform(checkField, filledField);
            if (!result.isSuccess()) {
                missingFields.add(fieldName);
            }
        }
        
        if (!missingFields.isEmpty()) {
            log.warn("Missing required fields: {}", String.join(", ", missingFields));
            return false;
        }
        
        log.info("All required fields are filled");
        return true;
    }
    
    /**
     * Checks for specific validation error messages.
     * Useful for understanding what went wrong.
     */
    public List<String> getValidationErrors() {
        List<String> errors = new ArrayList<>();
        
        // Check for email validation error
        if (checkForError("email-error", "Invalid email format")) {
            errors.add("Email format is invalid");
        }
        
        // Check for password validation error
        if (checkForError("password-error", "Password too weak")) {
            errors.add("Password does not meet requirements");
        }
        
        // Check for age validation error
        if (checkForError("age-error", "Age out of range")) {
            errors.add("Age must be between 18 and 120");
        }
        
        // Check for required field errors
        if (checkForError("required-error", "This field is required")) {
            errors.add("One or more required fields are empty");
        }
        
        return errors;
    }
    
    /**
     * Checks for a specific error type with automatic logging.
     */
    private boolean checkForError(String errorType, String errorDescription) {
        PatternFindOptions findError = new PatternFindOptions.Builder()
            .withLogging(logging -> logging
                .beforeActionMessage("Checking for " + errorDescription + "...")
                .successMessage("Found error: " + errorDescription)
                .failureMessage("No " + errorDescription + " detected")
                .logBeforeAction(false) // Don't log the check itself
                .logOnSuccess(true)     // Only log if error is found
                .logOnFailure(false))   // Don't log if no error
            .build();
            
        StateImage errorImage = new StateImage.Builder()
            .setName(errorType)
            .addPattern("images/errors/" + errorType + ".png")
            .build();
            
        return action.perform(findError, errorImage).isSuccess();
    }
    
    /**
     * Waits for form to be ready with progress logging.
     */
    public boolean waitForFormReady(int maxWaitSeconds) {
        PatternFindOptions checkReady = new PatternFindOptions.Builder()
            .withBeforeActionLog("Waiting for form to load...")
            .withSuccessLog("Form is ready")
            .withFailureLog("Form did not load within " + maxWaitSeconds + " seconds")
            .setPauseBeforeBegin(1.0) // Initial wait
            .build();
            
        StateImage formReady = new StateImage.Builder()
            .setName("form-ready")
            .addPattern("images/form-ready-indicator.png")
            .build();
        
        // Try multiple times with logging
        for (int i = 0; i < maxWaitSeconds; i++) {
            if (i > 0) {
                // Update the find options for subsequent attempts
                checkReady = new PatternFindOptions.Builder()
                    .withBeforeActionLog("Still waiting... (" + (i+1) + "/" + maxWaitSeconds + ")")
                    .withSuccessLog("Form is now ready")
                    .withFailureLog("Form not ready yet")
                    .setPauseBeforeBegin(1.0)
                    .build();
            }
            
            ActionResult result = action.perform(checkReady, formReady);
            if (result.isSuccess()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validates form submission was successful.
     */
    public boolean validateSubmissionSuccess() {
        // Check for success message
        PatternFindOptions findSuccess = new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking for submission confirmation...")
            .withSuccessLog("Form submitted successfully - confirmation received")
            .withFailureLog("No confirmation found - submission may have failed")
            .setPauseBeforeBegin(1.0) // Wait for server response
            .build();
            
        StateImage successMessage = new StateImage.Builder()
            .setName("success-message")
            .addPattern("images/success-message.png")
            .build();
            
        ActionResult successResult = action.perform(findSuccess, successMessage);
        
        if (!successResult.isSuccess()) {
            // Check for error message instead
            PatternFindOptions findError = new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for submission errors...")
                .withSuccessLog("Submission error detected")
                .withFailureLog("No specific error message found")
                .build();
                
            StateImage errorMessage = new StateImage.Builder()
                .setName("submission-error")
                .addPattern("images/submission-error.png")
                .build();
                
            ActionResult errorResult = action.perform(findError, errorMessage);
            
            if (errorResult.isSuccess()) {
                log.error("Form submission failed with an error");
                return false;
            }
            
            log.warn("Form submission status unclear - no confirmation or error found");
            return false;
        }
        
        return true;
    }
}