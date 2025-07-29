package com.aurelius.fear_greed_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main entry point for the Spring Boot application.
 * Configures the application to enable scheduling and provides a RestTemplate bean.
 * Also configures CORS to allow frontend applications to access the API.
 */
@SpringBootApplication
@EnableScheduling
public class FearGreedTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FearGreedTrackerApplication.class, args);
	}

	/**
	 * Creates and configures a RestTemplate bean.
	 * RestTemplate is used for making HTTP requests to external APIs (like CNN's FGI API).
	 * @return A configured RestTemplate instance.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * Configures Cross-Origin Resource Sharing (CORS) for the application.
	 * This allows the frontend application (running on a different port/domain)
	 * to make requests to this backend API.
	 *
	 * IMPORTANT: For production, restrict `allowedOrigins` to your specific frontend domains.
	 *
	 * @return A WebMvcConfigurer bean with CORS settings.
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**") // Apply CORS to all endpoints under /api
						// Explicitly list all allowed origins when allowCredentials is true
						.allowedOrigins(
								//your allowed origins only for better safety, never use *
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true); // Keep this if your app needs to send cookies/auth
			}
		};
	}
}
