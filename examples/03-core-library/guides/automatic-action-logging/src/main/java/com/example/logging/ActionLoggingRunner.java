package com.example.logging;

import com.example.logging.examples.BasicLoggingExample;
import com.example.logging.examples.ActionChainLoggingExample;
import com.example.logging.examples.ContextualLoggingExample;
import com.example.logging.workflows.LoginWorkflow;
import com.example.logging.workflows.FormAutomation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs all action logging examples.
 */
@Component
@Slf4j
public class ActionLoggingRunner implements CommandLineRunner {
    
    private final BasicLoggingExample basicExample;
    private final ActionChainLoggingExample chainExample;
    private final ContextualLoggingExample contextExample;
    private final LoginWorkflow loginWorkflow;
    private final FormAutomation formAutomation;
    
    public ActionLoggingRunner(BasicLoggingExample basicExample,
                              ActionChainLoggingExample chainExample,
                              ContextualLoggingExample contextExample,
                              LoginWorkflow loginWorkflow,
                              FormAutomation formAutomation) {
        this.basicExample = basicExample;
        this.chainExample = chainExample;
        this.contextExample = contextExample;
        this.loginWorkflow = loginWorkflow;
        this.formAutomation = formAutomation;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("================================================");
        log.info("Action Logging Examples");
        log.info("================================================");
        log.info("");
        
        // Basic logging examples
        log.info(">>> Running Basic Logging Examples <<<");
        basicExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Action chain logging
        log.info(">>> Running Action Chain Logging Examples <<<");
        chainExample.runExample();
        log.info("");
        
        Thread.sleep(1000);
        
        // Contextual logging with MDC
        log.info(">>> Running Contextual Logging Examples <<<");
        contextExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Real-world workflows
        log.info(">>> Running Real-World Workflows <<<");
        
        log.info("--- Login Workflow ---");
        loginWorkflow.demonstrateLogin();
        log.info("");
        
        Thread.sleep(1000);
        
        log.info("--- Form Automation ---");
        formAutomation.demonstrateFormAutomation();
        log.info("");
        
        log.info("================================================");
        log.info("All examples completed!");
        log.info("================================================");
        
        log.info("");
        log.info("Key takeaways:");
        log.info("✓ Use SLF4J with @Slf4j for easy logging");
        log.info("✓ Log at appropriate levels (DEBUG, INFO, WARN, ERROR)");
        log.info("✓ Include timing information for performance tracking");
        log.info("✓ Use MDC for contextual information");
        log.info("✓ Structure logs for easy parsing and analysis");
        log.info("✓ Chain actions with ActionChainOptions");
        log.info("✓ Always log both successes and failures");
        
        log.info("");
        log.info("Check the log files for detailed output!");
    }
}