package io.github.jspinak.brobot;

import org.sikuli.script.Pattern;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ComponentScan
@EnableElasticsearchRepositories
public class BrobotSpringBeanConfig {

    public BrobotSpringBeanConfig() {
        new Pattern(); // loads OpenCV from SikuliX
    }
}
