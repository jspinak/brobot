package com.claude.automator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.claude.automator",
    "io.github.jspinak.brobot"  // Include Brobot components
})
public class ClaudeAutomatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClaudeAutomatorApplication.class, args);
    }
}