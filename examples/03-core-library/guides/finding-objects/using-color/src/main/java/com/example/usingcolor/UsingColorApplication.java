package com.example.usingcolor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.usingcolor",
    "io.github.jspinak.brobot"
})
public class UsingColorApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsingColorApplication.class, args);
    }
}