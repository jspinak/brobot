package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.BrobotSpringBeanConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(BrobotSpringBeanConfig.class)
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "io.github.jspinak.brobot.test.mock"})
public class BrobotTestApplication {
    public static void main(String[] args) {
    }
}