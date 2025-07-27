package com.example.chaining.builders;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionChainOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.RepetitionOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Advanced workflow builder for complex automation scenarios.
 * Supports conditional execution, parallel chains, and workflow composition.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowBuilder {
    
    private final ChainBuilder chainBuilder;
    
    /**
     * Builds a complete user registration workflow
     */
    public Workflow buildRegistrationWorkflow() {
        Workflow workflow = new Workflow("User Registration");
        
        // Step 1: Navigate to registration
        workflow.addStep("Navigate to Registration", 
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for Sign Up link...")
                .withSuccessLog("Sign Up link found")
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Clicking Sign Up...")
                    .withSuccessLog("Registration page opened")
                    .setPauseAfterEnd(1.0)
                    .build())
                .build()
        );
        
        // Step 2: Fill registration form
        List<ChainBuilder.FormField> fields = List.of(
            new ChainBuilder.FormField("First Name", false, false),
            new ChainBuilder.FormField("Last Name", false, false),
            new ChainBuilder.FormField("Email", false, false),
            new ChainBuilder.FormField("Password", false, true),
            new ChainBuilder.FormField("Confirm Password", false, true)
        );
        
        workflow.addStep("Fill Registration Form",
            chainBuilder.buildFormChain(fields, "Create Account")
        );
        
        // Step 3: Handle verification
        workflow.addStep("Email Verification",
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for verification prompt...")
                .withSuccessLog("Verification required")
                .withFailureLog("No verification needed")
                .setPauseBeforeBegin(2.0)
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for 'Send Code' button...")
                    .withSuccessLog("Verification code sent")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Requesting verification code...")
                    .withSuccessLog("Code sent to email")
                    .build())
                .build()
        );
        
        // Step 4: Complete profile (conditional)
        workflow.addConditionalStep("Profile Completion",
            result -> result.isSuccess(), // Only if registration succeeded
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking for profile completion prompt...")
                .withSuccessLog("Profile completion available")
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Opening profile settings...")
                    .withSuccessLog("Profile page opened")
                    .build())
                .build()
        );
        
        return workflow;
    }
    
    /**
     * Builds a data export workflow with progress monitoring
     */
    public Workflow buildDataExportWorkflow() {
        Workflow workflow = new Workflow("Data Export");
        
        // Step 1: Open export dialog
        workflow.addStep("Open Export Dialog",
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for Export button...")
                .withSuccessLog("Export button found")
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Opening export options...")
                    .withSuccessLog("Export dialog opened")
                    .setPauseAfterEnd(0.5)
                    .build())
                .build()
        );
        
        // Step 2: Configure export options
        workflow.addStep("Configure Export",
            buildExportConfigChain()
        );
        
        // Step 3: Monitor progress
        workflow.addStep("Monitor Export Progress",
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for progress indicator...")
                .withSuccessLog("Export in progress")
                .setSearchDuration(30) // Long timeout for export
                .setPauseBeforeBegin(2.0) // Wait before checking progress
                .withAfterActionLog("Monitoring export progress...")
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Checking if export completed...")
                    .withSuccessLog("Export completed")
                    .withFailureLog("Export still in progress")
                    .setRepetition(new RepetitionOptions.Builder()
                        .setMaxTimesToRepeatActionSequence(10)
                        .setPauseBetweenActionSequences(3)
                        .build())
                    .build())
                .build()
        );
        
        // Step 4: Download results
        workflow.addStep("Download Results",
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for download link...")
                .withSuccessLog("Download ready")
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Downloading export file...")
                    .withSuccessLog("File downloaded successfully")
                    .withAfterActionLog("Export workflow completed in {duration}ms")
                    .build())
                .build()
        );
        
        return workflow;
    }
    
    /**
     * Builds a multi-document processing workflow
     */
    public Workflow buildDocumentProcessingWorkflow(List<String> documentTypes) {
        Workflow workflow = new Workflow("Document Processing");
        
        // Add dynamic steps for each document type
        for (String docType : documentTypes) {
            workflow.addStep("Process " + docType,
                buildDocumentProcessChain(docType)
            );
        }
        
        // Final validation step
        workflow.addStep("Validate All Documents",
            new PatternFindOptions.Builder()
                .withBeforeActionLog("Checking all documents processed...")
                .withSuccessLog("All documents valid")
                .withFailureLog("Some documents need attention")
                .then(new PatternFindOptions.Builder()
                    .withBeforeActionLog("Looking for 'Continue' button...")
                    .withSuccessLog("Ready to proceed")
                    .build())
                .then(new ClickOptions.Builder()
                    .withBeforeActionLog("Finalizing document processing...")
                    .withSuccessLog("Documents processed successfully")
                    .build())
                .build()
        );
        
        return workflow;
    }
    
    /**
     * Helper: Build export configuration chain
     */
    private ActionConfig buildExportConfigChain() {
        return new PatternFindOptions.Builder()
            .withBeforeActionLog("Configuring export format...")
            .withSuccessLog("Format options available")
            // Select format
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for CSV option...")
                .withSuccessLog("CSV format found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Selecting CSV format...")
                .withSuccessLog("CSV selected")
                .build())
            // Configure date range
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for date range selector...")
                .withSuccessLog("Date range found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening date picker...")
                .withSuccessLog("Date picker opened")
                .build())
            // Include all data checkbox
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for 'Include All Data' option...")
                .withSuccessLog("Option found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Enabling all data export...")
                .withSuccessLog("All data will be exported")
                .build())
            // Start export
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for Start Export button...")
                .withSuccessLog("Export button ready")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Starting export process...")
                .withSuccessLog("Export started")
                .setPauseAfterEnd(1.0)
                .build())
            .build();
    }
    
    /**
     * Helper: Build document processing chain
     */
    private ActionConfig buildDocumentProcessChain(String docType) {
        return new PatternFindOptions.Builder()
            .withBeforeActionLog("Looking for " + docType + " section...")
            .withSuccessLog(docType + " section found")
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Opening " + docType + " uploader...")
                .withSuccessLog("Uploader ready")
                .setPauseAfterEnd(0.5)
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for file selector...")
                .withSuccessLog("File selector found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Selecting " + docType + " file...")
                .withSuccessLog("File selected")
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Verifying " + docType + " preview...")
                .withSuccessLog(docType + " loaded correctly")
                .setPauseBeforeBegin(1.0)
                .build())
            .then(new PatternFindOptions.Builder()
                .withBeforeActionLog("Looking for 'Confirm' button...")
                .withSuccessLog("Confirm button found")
                .build())
            .then(new ClickOptions.Builder()
                .withBeforeActionLog("Confirming " + docType + "...")
                .withSuccessLog(docType + " processed")
                .build())
            .build();
    }
    
    /**
     * Workflow container class
     */
    public static class Workflow {
        private final String name;
        private final List<WorkflowStep> steps = new ArrayList<>();
        
        public Workflow(String name) {
            this.name = name;
        }
        
        public void addStep(String name, ActionConfig action) {
            steps.add(new WorkflowStep(name, action, null));
        }
        
        public void addConditionalStep(String name, 
                                     Consumer<ActionResult> condition,
                                     ActionConfig action) {
            steps.add(new WorkflowStep(name, action, condition));
        }
        
        public List<WorkflowStep> getSteps() {
            return new ArrayList<>(steps);
        }
        
        public String getName() {
            return name;
        }
        
        public void logStructure() {
            log.info("Workflow: {} ({} steps)", name, steps.size());
            for (int i = 0; i < steps.size(); i++) {
                WorkflowStep step = steps.get(i);
                String type = step.isConditional() ? "conditional" : "standard";
                log.info("  {}. {} ({})", i + 1, step.getName(), type);
            }
        }
    }
    
    /**
     * Individual workflow step
     */
    public static class WorkflowStep {
        private final String name;
        private final ActionConfig action;
        private final Consumer<ActionResult> condition;
        
        public WorkflowStep(String name, ActionConfig action, Consumer<ActionResult> condition) {
            this.name = name;
            this.action = action;
            this.condition = condition;
        }
        
        public String getName() { return name; }
        public ActionConfig getAction() { return action; }
        public Consumer<ActionResult> getCondition() { return condition; }
        public boolean isConditional() { return condition != null; }
    }
}