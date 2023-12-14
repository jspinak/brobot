package io.github.jspinak.brobot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(BrobotSpringBeanConfig.class)
public class BrobotTestApplication {
    public static void main(String[] args) {}
}

