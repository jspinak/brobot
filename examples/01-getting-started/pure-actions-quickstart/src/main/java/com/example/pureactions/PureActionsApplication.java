package com.example.pureactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.pureactions", "io.github.jspinak.brobot"})
public class PureActionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PureActionsApplication.class, args);
    }
}
