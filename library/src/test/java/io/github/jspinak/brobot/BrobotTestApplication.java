package io.github.jspinak.brobot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication // make sure this doesn't prevent Brobot from acting as a library
@Import(BrobotSpringBeanConfig.class)
public class BrobotTestApplication {
    public static void main(String[] args) {}
}



