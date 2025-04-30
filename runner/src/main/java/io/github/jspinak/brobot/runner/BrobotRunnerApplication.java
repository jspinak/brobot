package io.github.jspinak.brobot.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BrobotRunnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrobotRunnerApplication.class, args);
		BrobotRunnerApp.main(args);
	}
}
