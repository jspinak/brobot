package com.example.combiningfinds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.combiningfinds",
    "io.github.jspinak.brobot"
})
public class CombiningFindsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CombiningFindsApplication.class, args);
    }
}