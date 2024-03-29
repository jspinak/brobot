package com.brobot.app;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication//(scanBasePackages = {"com.brobot.app", "io.github.jspinak.brobot"})
//@ComponentScan("com.brobot.app.database.mappers")
//@ComponentScan("com.brobot.app.restControllers")
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
		SpringApplication.run(BrobotApplication.class, args);
	}

}
