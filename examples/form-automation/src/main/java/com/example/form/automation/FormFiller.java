package com.example.form.automation;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.basic.match.Matches;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Form filling automation with comprehensive logging.
 * 
 * Demonstrates how automatic logging provides visibility into each
 * step of the form filling process without cluttering the code.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FormFiller {
    
    private final Action action;
    
    /**
     * Fills a simple form with text fields.
     * Each field interaction is automatically logged.
     */
    public boolean fillForm(Map<String, String> formData) {
        for (Map.Entry<String, String> field : formData.entrySet()) {
            if (!fillField(field.getKey(), field.getValue())) {
                return false;
            }
        }
        return submitForm();
    }
    
    /**
     * Fills a complex form with various input types.
     * Shows different logging patterns for different field types.
     */
    public boolean fillComplexForm(Map<String, String> formData) {
        for (Map.Entry<String, String> field : formData.entrySet()) {
            String fieldName = field.getKey();
            String value = field.getValue();
            
            // Handle different field types
            if (fieldName.equals("agreeTerms") && value.equals("true")) {
                if (!clickCheckbox(fieldName)) return false;
            } else if (fieldName.equals("country")) {
                if (!selectDropdown(fieldName, value)) return false;
            } else if (fieldName.contains("password")) {
                if (!fillPasswordField(fieldName, value)) return false;
            } else {
                if (!fillField(fieldName, value)) return false;
            }
        }
        return submitForm();
    }
    
    /**
     * Fills a form and handles validation errors.
     * Demonstrates retry logic with automatic logging.
     */
    public boolean fillFormWithValidation(Map<String, String> formData) {
        // Fill the form
        for (Map.Entry<String, String> field : formData.entrySet()) {
            if (!fillField(field.getKey(), field.getValue())) {
                return false;
            }
        }
        
        // Submit and check for validation errors
        if (!submitForm()) {
            return false;
        }
        
        // Check for validation error messages
        return !hasValidationErrors();
    }
    
    /**
     * Fills a single text field with automatic logging.
     */
    private boolean fillField(String fieldName, String value) {
        // Find the field with comprehensive logging
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for " + fieldName + " field...")
            .withSuccessLog("Found " + fieldName + " field")
            .withFailureLog("Failed to find " + fieldName + " field - check if form is loaded")
            .build();
            
        StateImage fieldImage = new StateImage.Builder()
            .setName(fieldName)
            .addPattern("images/" + fieldName + ".png")
            .build();
            
        ActionResult findResult = action.perform(findOptions, fieldImage);
        if (!findResult.isSuccess()) {
            return false;
        }
        
        // Click to focus the field
        ClickOptions clickOptions = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking " + fieldName + " field...")
            .withSuccessLog("Successfully clicked " + fieldName + " field")
            .withFailureLog("Failed to click " + fieldName + " field")
            .build();
            
        ActionResult clickResult = action.perform(clickOptions, 
            new ObjectCollection.Builder().withMatches(findResult.getMatchList()).build());
        if (!clickResult.isSuccess()) {
            return false;
        }
        
        // Clear the field (Ctrl+A, Delete)
        if (!clearField()) {
            return false;
        }
        
        // Type the value
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .withBeforeActionLog("Typing " + value + "...")
            .withSuccessLog("Successfully typed " + value)
            .withFailureLog("Failed to type " + value)
            .withAfterActionLog("Form field completed in {duration}ms")
            .build();
            
        return action.perform(typeOptions, 
            new ObjectCollection.Builder().withStrings(value).build()).isSuccess();
    }
    
    /**
     * Fills a password field with masked logging for security.
     */
    private boolean fillPasswordField(String fieldName, String value) {
        // Find and click the password field
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for " + fieldName + " field...")
            .withSuccessLog("Found " + fieldName + " field")
            .withFailureLog(fieldName + " field not found")
            .build();
            
        StateImage fieldImage = new StateImage.Builder()
            .setName(fieldName)
            .addPattern("images/" + fieldName + ".png")
            .build();
            
        ActionResult findResult = action.perform(findOptions, fieldImage);
        if (!findResult.isSuccess()) {
            return false;
        }
        
        // Click to focus
        ClickOptions clickOptions = new ClickOptions.Builder()
            .withBeforeActionLog("Focusing " + fieldName + " field...")
            .withSuccessLog(fieldName + " field focused")
            .build();
            
        action.perform(clickOptions, 
            new ObjectCollection.Builder().withMatches(findResult.getMatchList()).build());
        
        // Type password (logging masks the actual value)
        TypeOptions typeOptions = new TypeOptions.Builder()
            .setTypeDelay(0.05)
            .withBeforeActionLog("Typing password...")
            .withSuccessLog("Password entered successfully")
            .withFailureLog("Failed to enter password")
            .build();
            
        return action.perform(typeOptions, 
            new ObjectCollection.Builder().withStrings(value).build()).isSuccess();
    }
    
    /**
     * Clicks a checkbox with appropriate logging.
     */
    private boolean clickCheckbox(String checkboxName) {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for " + checkboxName + " checkbox...")
            .withSuccessLog("Found " + checkboxName + " checkbox")
            .withFailureLog(checkboxName + " checkbox not found")
            .build();
            
        StateImage checkbox = new StateImage.Builder()
            .setName(checkboxName)
            .addPattern("images/" + checkboxName + ".png")
            .build();
            
        ActionResult findResult = action.perform(findOptions, checkbox);
        if (!findResult.isSuccess()) {
            return false;
        }
        
        ClickOptions clickOptions = new ClickOptions.Builder()
            .withBeforeActionLog("Clicking " + checkboxName + " checkbox...")
            .withSuccessLog(checkboxName + " checkbox selected")
            .withFailureLog("Failed to click " + checkboxName + " checkbox")
            .build();
            
        return action.perform(clickOptions, 
            new ObjectCollection.Builder().withMatches(findResult.getMatchList()).build()).isSuccess();
    }
    
    /**
     * Selects a dropdown value with detailed logging.
     */
    private boolean selectDropdown(String dropdownName, String value) {
        // First click the dropdown to open it
        PatternFindOptions findDropdown = new PatternFindOptions.Builder()
            .withBeforeActionLog("Opening " + dropdownName + " dropdown...")
            .withSuccessLog(dropdownName + " dropdown found")
            .withFailureLog(dropdownName + " dropdown not found")
            .build();
            
        StateImage dropdown = new StateImage.Builder()
            .setName(dropdownName)
            .addPattern("images/" + dropdownName + ".png")
            .build();
            
        ActionResult dropdownResult = action.perform(findDropdown, dropdown);
        if (!dropdownResult.isSuccess()) {
            return false;
        }
        
        // Click to open dropdown
        ClickOptions openDropdown = new ClickOptions.Builder()
            .withBeforeActionLog("Opening dropdown menu...")
            .withSuccessLog("Dropdown menu opened")
            .setPauseAfterEnd(0.5) // Wait for dropdown animation
            .build();
            
        action.perform(openDropdown, 
            new ObjectCollection.Builder().withMatches(dropdownResult.getMatchList()).build());
        
        // Find and click the value
        PatternFindOptions findValue = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for option: " + value + "...")
            .withSuccessLog("Found option: " + value)
            .withFailureLog("Option not found: " + value)
            .build();
            
        StateImage valueOption = new StateImage.Builder()
            .setName(value)
            .addPattern("images/dropdown-" + value + ".png")
            .build();
            
        ActionResult valueResult = action.perform(findValue, valueOption);
        if (!valueResult.isSuccess()) {
            return false;
        }
        
        ClickOptions selectValue = new ClickOptions.Builder()
            .withBeforeActionLog("Selecting " + value + "...")
            .withSuccessLog(value + " selected from dropdown")
            .build();
            
        return action.perform(selectValue, 
            new ObjectCollection.Builder().withMatches(valueResult.getMatchList()).build()).isSuccess();
    }
    
    /**
     * Clears a field using keyboard shortcuts.
     */
    private boolean clearField() {
        // Select all (Ctrl+A)
        TypeOptions selectAll = new TypeOptions.Builder()
            .setModifiers("ctrl")
            .withBeforeActionLog("Clearing field contents...")
            .withSuccessLog("Field cleared")
            .build();
            
        action.perform(selectAll, 
            new ObjectCollection.Builder().withStrings("a").build());
        
        // Delete
        TypeOptions delete = new TypeOptions.Builder()
            .build();
            
        return action.perform(delete, 
            new ObjectCollection.Builder().withStrings("\b").build()).isSuccess();
    }
    
    /**
     * Submits the form with comprehensive logging.
     */
    private boolean submitForm() {
        PatternFindOptions findSubmit = new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for submit button...")
            .withSuccessLog("Submit button found")
            .withFailureLog("Submit button not found - form may not be complete")
            .build();
            
        StateImage submitButton = new StateImage.Builder()
            .setName("submitButton")
            .addPattern("images/submit-button.png")
            .build();
            
        ActionResult findResult = action.perform(findSubmit, submitButton);
        if (!findResult.isSuccess()) {
            return false;
        }
        
        ClickOptions clickSubmit = new ClickOptions.Builder()
            .withBeforeActionLog("Submitting form...")
            .withSuccessLog("Form submitted successfully")
            .withFailureLog("Failed to submit form")
            .withAfterActionLog("Form submission completed in {duration}ms")
            .setPauseAfterEnd(2.0) // Wait for form processing
            .build();
            
        return action.perform(clickSubmit, 
            new ObjectCollection.Builder().withMatches(findResult.getMatchList()).build()).isSuccess();
    }
    
    /**
     * Checks for validation error messages.
     */
    private boolean hasValidationErrors() {
        PatternFindOptions findErrors = new PatternFindOptions.Builder()
            .withBeforeActionLog("Checking for validation errors...")
            .withSuccessLog("Validation errors found - form needs correction")
            .withFailureLog("No validation errors detected")
            .build();
            
        StateImage errorMessage = new StateImage.Builder()
            .setName("validationError")
            .addPattern("images/validation-error.png")
            .build();
            
        // In this case, finding the error means validation failed
        return action.perform(findErrors, errorMessage).isSuccess();
    }
}