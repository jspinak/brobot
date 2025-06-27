package io.github.jspinak.brobot;

import io.github.jspinak.brobot.config.BrobotConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Import(BrobotConfig.class)
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "io.github.jspinak.brobot.test.mock"})
public class BrobotTestApplication {
    public static void main(String[] args) {
    }
}