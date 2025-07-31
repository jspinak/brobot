package com.example.actionhierarchy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.actionhierarchy",
    "io.github.jspinak.brobot"
})
public class ActionHierarchyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActionHierarchyApplication.class, args);
    }
}