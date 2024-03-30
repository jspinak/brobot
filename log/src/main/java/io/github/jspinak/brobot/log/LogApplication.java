package io.github.jspinak.brobot.log;

import io.github.jspinak.brobot.testingAUTs.LogListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LogApplication {

	private final ElasticsearchLogWriter elasticsearchLogWriter;

	@Autowired
	public LogApplication(ElasticsearchLogWriter elasticsearchLogWriter) {
		this.elasticsearchLogWriter = elasticsearchLogWriter;
	}

	public static void main(String[] args) {
		SpringApplication.run(LogApplication.class, args);
	}

	// You can register the log event listener in a @Bean method if needed
	@Bean
	public void logListener() {
		LogListener.registerLogEventListener(elasticsearchLogWriter);
	}

}
