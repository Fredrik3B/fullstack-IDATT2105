package edu.ntnu.idatt2105.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * MVC configuration that applies a global {@code /api} prefix to all REST controllers.
 *
 * <p>Every class annotated with {@code @RestController} inside the
 * {@code edu.ntnu.idatt2105.backend} package automatically receives the {@code /api} prefix,
 * keeping the prefix out of individual controller mappings.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * Prepends {@code /api} to all {@link org.springframework.web.bind.annotation.RestController}
   * handler mappings within the backend base package.
   *
   * @param configurer the path match configurer to modify
   */
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class)
        .and(HandlerTypePredicate.forBasePackage("edu.ntnu.idatt2105.backend")));
  }
}