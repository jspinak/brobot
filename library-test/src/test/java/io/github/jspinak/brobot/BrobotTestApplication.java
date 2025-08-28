package io.github.jspinak.brobot;

import io.github.jspinak.brobot.config.BrobotConfig;
import io.github.jspinak.brobot.test.TestConfigurationPropertiesConfig;
import io.github.jspinak.brobot.test.TestLoggingConfig;
import io.github.jspinak.brobot.test.mock.MockBrobotLoggerConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.boot.SpringApplication;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Import({BrobotConfig.class, TestLoggingConfig.class, TestConfigurationPropertiesConfig.class, MockBrobotLoggerConfig.class,
        io.github.jspinak.brobot.test.config.MockScreenCaptureConfig.class})
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "io.github.jspinak.brobot.test.mock"})
public class BrobotTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrobotTestApplication.class, args);
    }
}