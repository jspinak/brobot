package io.github.jspinak.brobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.jspinak.brobot.test.config.ComprehensiveTestConfig;

@SpringBootApplication(
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@Import(ComprehensiveTestConfig.class)
public class BrobotTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrobotTestApplication.class, args);
    }
}
