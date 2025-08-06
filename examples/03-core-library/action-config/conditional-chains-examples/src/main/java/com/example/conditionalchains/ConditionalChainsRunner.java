package com.example.conditionalchains;

import com.example.conditionalchains.examples.BasicFindExample;
import com.example.conditionalchains.examples.CustomLogicExample;
import com.example.conditionalchains.examples.MultiStepWorkflowExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Runs all ConditionalActionChain examples to demonstrate the various patterns.
 */
@Component
public class ConditionalChainsRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ConditionalChainsRunner.class);

    private final BasicFindExample basicFindExample;
    private final MultiStepWorkflowExample multiStepExample;
    private final CustomLogicExample customLogicExample;
    
    public ConditionalChainsRunner(BasicFindExample basicFindExample,
                                  MultiStepWorkflowExample multiStepExample,
                                  CustomLogicExample customLogicExample) {
        this.basicFindExample = basicFindExample;
        this.multiStepExample = multiStepExample;
        this.customLogicExample = customLogicExample;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("======================================");
        log.info("ConditionalActionChain Examples");
        log.info("======================================");
        log.info("");
        
        // Run basic find examples
        log.info(">>> Running Basic Find Examples <<<");
        basicFindExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Run multi-step workflow examples
        log.info(">>> Running Multi-Step Workflow Examples <<<");
        multiStepExample.runAllExamples();
        log.info("");
        
        Thread.sleep(1000);
        
        // Run custom logic examples
        log.info(">>> Running Custom Logic Examples <<<");
        customLogicExample.runAllExamples();
        log.info("");
        
        log.info("======================================");
        log.info("All examples completed!");
        log.info("======================================");
        
        log.info("");
        log.info("Key takeaways:");
        log.info("✓ ConditionalActionChain provides elegant conditional execution");
        log.info("✓ ifFound/ifNotFound for find operations");
        log.info("✓ ifSuccess/ifFailure for action operations");
        log.info("✓ Chains can be nested for complex workflows");
        log.info("✓ Custom lambdas enable sophisticated logic");
        log.info("✓ Great for error handling and recovery flows");
    }
}