package io.github.jspinak.brobot.app;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication//(scanBasePackages = {"io.github.jspinak.brobot"})
@ComponentScan({"io.github.jspinak.brobot"})
public class BrobotApplication {

	@Autowired
	private ApplicationContext applicationContext;
	@PostConstruct public void printBeans() {
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		System.out.println("printing available beans");
		for (String beanName : beanNames) {
			System.out.println(beanName);
		}
	}

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(BrobotApplication.class);
		builder.headless(false);
		ConfigurableApplicationContext context = builder.run(args);
		SpringApplication.run(BrobotApplication.class, args);
	}

}
