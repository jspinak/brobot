package io.github.jspinak.brobot.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.File;

@SpringBootApplication
@ComponentScan(basePackages = {"io.github.jspinak.brobot", "io.github.jspinak.brobot.app"})
@EnableScheduling
public class BrobotApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(BrobotApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
	}
}