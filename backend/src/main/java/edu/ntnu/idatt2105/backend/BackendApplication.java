package edu.ntnu.idatt2105.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot entry point for the backend application.
 *
 * <p>Enables component scanning, JPA entity scanning, and JPA repository scanning
 * across the {@code edu.ntnu.idatt2105.backend} base package.
 */
@SpringBootApplication(scanBasePackages = {
    "edu.ntnu.idatt2105.backend",
})
@EntityScan(basePackages = {
    "edu.ntnu.idatt2105.backend",
    "no.ntnu.resturant_manager"
})
@EnableJpaRepositories(basePackages = {
    "edu.ntnu.idatt2105.backend",
})
public class BackendApplication {

  /**
   * Application entry point.
   *
   * @param args command-line arguments passed to the JVM
   */
  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

}
