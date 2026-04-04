package edu.ntnu.idatt2105.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "edu.ntnu.idatt2105.backend",
    "no.ntnu.resturant_manager"
})
@EntityScan(basePackages = {
    "edu.ntnu.idatt2105.backend",
    "no.ntnu.resturant_manager"
})
@EnableJpaRepositories(basePackages = {
    "edu.ntnu.idatt2105.backend",
    "no.ntnu.resturant_manager"
})
public class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

}
