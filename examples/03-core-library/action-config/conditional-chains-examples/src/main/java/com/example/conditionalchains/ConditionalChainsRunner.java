package com.example.conditionalchains;

import com.example.conditionalchains.examples.SimpleWorkingExample;
import com.example.conditionalchains.examples.EnhancedChainExample;
import com.example.conditionalchains.examples.DocumentationExamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs examples that demonstrate both the simple working approach and
 * the enhanced ConditionalActionChain with all documentation features.
 */
@Component
public class ConditionalChainsRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ConditionalChainsRunner.class);

    private final SimpleWorkingExample simpleExample;
    private final EnhancedChainExample enhancedExample;
    private final DocumentationExamples documentationExamples;
    
    public ConditionalChainsRunner(SimpleWorkingExample simpleExample,
                                   EnhancedChainExample enhancedExample,
                                   DocumentationExamples documentationExamples) {
        this.simpleExample = simpleExample;
        this.enhancedExample = enhancedExample;
        this.documentationExamples = documentationExamples;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("======================================");
        log.info("Conditional Chains Examples");
        log.info("======================================");
        log.info("");
        
        // Run simple working examples
        log.info(">>> Running Simple Working Examples <<<");
        simpleExample.runAllExamples();
        log.info("");
        
        // Run enhanced chain examples
        log.info(">>> Running Enhanced Chain Examples <<<");
        log.info("These demonstrate all features from the original documentation:");
        enhancedExample.runAllExamples();
        log.info("");
        
        // Run documentation examples
        log.info(">>> Running Documentation Examples <<<");
        log.info("These are the exact examples from the documentation:");
        documentationExamples.runAllExamples();
        log.info("");
        
        log.info("======================================");
        log.info("All examples completed!");
        log.info("======================================");
        
        log.info("");
        log.info("Key improvements in ConditionalActionChain:");
        log.info("✓ then() method for sequential action composition");
        log.info("✓ Convenience methods: click(), type(), scrollDown()");
        log.info("✓ Keyboard shortcuts: pressEnter(), pressTab(), pressCtrlS()");
        log.info("✓ Control flow: stopChain(), retry(), throwError()");
        log.info("✓ Proper conditional execution in perform() method");
        log.info("✓ No wait() method - timing via action configurations");
        log.info("✓ Model-based approach: states, not processes!");
        log.info("✓ All documentation examples work as originally intended!");
    }
}