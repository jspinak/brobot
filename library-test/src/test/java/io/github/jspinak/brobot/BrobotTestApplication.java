package io.github.jspinak.brobot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = {"io.github.jspinak.brobot"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.startup\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.initialization\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*BrobotStartup.*")
    }
)
@Configuration
public class BrobotTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrobotTestApplication.class, args);
    }
}