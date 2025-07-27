package com.example.chaining.builders;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.wait.WaitOptions;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building common action chain patterns.
 * Provides reusable chain templates for common automation scenarios.
 */
@Component
@Slf4j
public class ChainBuilder {
    
    /**
     * Creates a login chain with customizable parameters
     */
    public ActionConfig buildLoginChain(String usernameFieldName, 
                                      String passwordFieldName,
                                      String loginButtonName,
                                      boolean checkRememberMe) {
        
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting login process...")
            .withSuccessLog("Login form found");
            
        // Add remember me check if requested
        if (checkRememberMe) {
            chainBuilder = chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking for 'Remember Me' option...")
                    .withSuccessLog("'Remember Me' option found and selected")
                    .withFailureLog("No 'Remember Me' option available")
                    .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Selecting 'Remember Me'...")
                        .build())
                    .build());
        }
        
        // Continue with username/password
        return chainBuilder
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + usernameFieldName + " field...")
                .withSuccessLog("Username field found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing username field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering username...")
                .withSuccessLog("Username entered")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + passwordFieldName + " field...")
                .withSuccessLog("Password field found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing password field...")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering password...")
                .withSuccessLog("Password entered")
                // Note: Password masking should be handled at the logging level
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + loginButtonName + "...")
                .withSuccessLog("Login button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Submitting login...")
                .withSuccessLog("Login submitted successfully")
                .withAfterActionLog("Login process completed in {duration}ms")
                .setPauseAfterEnd(2.0) // Wait for login processing
                .build())
            .build();
    }
    
    /**
     * Creates a form filling chain for multiple fields
     */
    public ActionConfig buildFormChain(List<FormField> fields, String submitButtonName) {
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting form fill process...")
            .withSuccessLog("Form ready for input");
            
        // Add each field to the chain
        for (FormField field : fields) {
            chainBuilder = chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for " + field.getName() + " field...")
                    .withSuccessLog(field.getName() + " field found")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Focusing " + field.getName() + "...")
                    .build());
                    
            // Clear field if requested
            if (field.shouldClear()) {
                chainBuilder = chainBuilder
                    .then(new TypeOptions.Builder()
                        .withBeforeActionLog("Clearing " + field.getName() + "...")
                        .setModifiers("CTRL+A") // Select all text
                        .build())
                    .then(new TypeOptions.Builder()
                        .withBeforeActionLog("Deleting selected text...")
                        .setTypeDelay(0.1)
                        .build()); // Then type new text
            }
            
            // Type the value
            chainBuilder = chainBuilder
                .then(new TypeOptions.Builder()
                    .withBeforeActionLog("Entering " + field.getName() + "...")
                    .withSuccessLog(field.getName() + " filled")
                    // Note: Sensitive field masking should be handled at the logging level
                    .build());
        }
        
        // Add submit button
        return chainBuilder
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + submitButtonName + "...")
                .withSuccessLog("Submit button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Submitting form...")
                .withSuccessLog("Form submitted successfully")
                .withAfterActionLog("Form completed with " + fields.size() + " fields in {duration}ms")
                .build())
            .build();
    }
    
    /**
     * Creates a navigation chain through menu hierarchy
     */
    public ActionConfig buildMenuNavigationChain(String... menuPath) {
        if (menuPath.length == 0) {
            throw new IllegalArgumentException("Menu path cannot be empty");
        }
        
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Opening " + menuPath[0] + " menu...")
            .withSuccessLog(menuPath[0] + " menu found");
            
        // First item is clicked
        chainBuilder = chainBuilder
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Clicking " + menuPath[0] + "...")
                .withSuccessLog(menuPath[0] + " opened")
                .setPauseAfterEnd(0.5) // Wait for menu animation
                .build());
                
        // Subsequent items might need hover
        for (int i = 1; i < menuPath.length; i++) {
            final String item = menuPath[i];
            final boolean isLast = (i == menuPath.length - 1);
            
            chainBuilder = chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for " + item + " in menu...")
                    .withSuccessLog(item + " found")
                    .build());
                    
            if (isLast) {
                // Click the final item
                chainBuilder = chainBuilder
                    .then(new ClickOptions.Builder()
                        .withBeforeActionLog("Selecting " + item + "...")
                        .withSuccessLog("Navigated to " + item)
                        .withAfterActionLog("Navigation completed: " + String.join(" > ", menuPath))
                        .build());
            } else {
                // Hover over intermediate items
                chainBuilder = chainBuilder
                    .then(new MouseMoveOptions.Builder()
                        .withBeforeActionLog("Hovering over " + item + "...")
                        .withSuccessLog("Submenu expanded")
                        .setPauseAfterEnd(0.3)
                        .setMoveMouseDelay(0.5f) // Smooth hover movement
                        .build());
            }
        }
        
        return chainBuilder.build();
    }
    
    /**
     * Creates a search workflow chain
     */
    public ActionConfig buildSearchChain(String searchBoxName, 
                                       String searchButtonName,
                                       boolean waitForResults,
                                       double resultWaitTime) {
        
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting search workflow...")
            .withSuccessLog("Search interface ready")
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + searchBoxName + "...")
                .withSuccessLog("Search box found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Focusing search box...")
                .withSuccessLog("Search box focused")
                .build())
            .then(new TypeOptions.Builder()
                .withBeforeActionLog("Entering search query...")
                .withSuccessLog("Query entered")
                .setTypeDelay(0.05) // Natural typing speed
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + searchButtonName + "...")
                .withSuccessLog("Search button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Executing search...")
                .withSuccessLog("Search initiated")
                .build());
                
        // Add result waiting if requested
        if (waitForResults) {
            chainBuilder = chainBuilder
                .then(new WaitOptions.Builder()
                    .setWaitSeconds(resultWaitTime)
                    .withBeforeActionLog("Waiting for search results...")
                    .withSuccessLog("Results loaded")
                    .withAfterActionLog("Search completed in {duration}ms")
                    .build());
        }
        
        return chainBuilder.build();
    }
    
    /**
     * Creates a file upload chain
     */
    public ActionConfig buildFileUploadChain(String uploadButtonName,
                                           String fileInputName,
                                           String confirmButtonName,
                                           boolean addDescription) {
        
        PatternFindOptions.Builder chainBuilder = new PatternFindOptions.Builder()
            .withBeforeActionLog("Starting file upload process...")
            .withSuccessLog("Upload interface ready")
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + uploadButtonName + "...")
                .withSuccessLog("Upload button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening file selector...")
                .withSuccessLog("File selector opened")
                .setPauseAfterEnd(1.0) // Wait for dialog
                .build());
                
        // Add file path entry
        if (fileInputName != null) {
            chainBuilder = chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for " + fileInputName + "...")
                    .withSuccessLog("File input found")
                    .build())
                .then(new TypeOptions.Builder()
                    .withBeforeActionLog("Entering file path...")
                    .withSuccessLog("File path entered")
                    .build());
        }
        
        // Add description if requested
        if (addDescription) {
            chainBuilder = chainBuilder
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for description field...")
                    .withSuccessLog("Description field found")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Focusing description...")
                    .build())
                .then(new TypeOptions.Builder()
                    .withBeforeActionLog("Adding file description...")
                    .withSuccessLog("Description added")
                    .build());
        }
        
        // Confirm upload
        return chainBuilder
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for " + confirmButtonName + "...")
                .withSuccessLog("Confirm button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Confirming upload...")
                .withSuccessLog("File uploaded successfully")
                .withAfterActionLog("Upload process completed in {duration}ms")
                .setPauseAfterEnd(2.0) // Wait for upload
                .build())
            .build();
    }
    
    /**
     * Helper class for form field definitions
     */
    public static class FormField {
        private final String name;
        private final boolean clear;
        private final boolean sensitive;
        
        public FormField(String name, boolean clear, boolean sensitive) {
            this.name = name;
            this.clear = clear;
            this.sensitive = sensitive;
        }
        
        public String getName() { return name; }
        public boolean shouldClear() { return clear; }
        public boolean isSensitive() { return sensitive; }
    }
}