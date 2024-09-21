package io.github.jspinak.brobot.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;

@TestConfiguration
public class TestConfig {
    @Bean
    public CommandLineRunner logDatabaseInfo(Environment env, DataSource dataSource) {
        return args -> {
            System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
            System.out.println("DataSource URL: " + dataSource.getConnection().getMetaData().getURL());
        };
    }
}