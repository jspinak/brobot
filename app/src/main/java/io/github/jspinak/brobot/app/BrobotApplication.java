package io.github.jspinak.brobot.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {
		"io.github.jspinak.brobot",
		"io.github.jspinak.brobot.library-features",
		"io.github.jspinak.brobot.app"})
@EnableScheduling
public class BrobotApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(BrobotApplication.class)
			.headless(false)
			.run(args);
	}
}