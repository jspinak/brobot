package com.example.mocking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

/** Simple standalone application for build testing. */
@SpringBootApplication
@Slf4j
public class SimpleStandaloneApp {

    public static void main(String[] args) {
        log.info("Enhanced Mocking Example - Standalone Build Test");
        SpringApplication.run(SimpleStandaloneApp.class, args);
    }
}
