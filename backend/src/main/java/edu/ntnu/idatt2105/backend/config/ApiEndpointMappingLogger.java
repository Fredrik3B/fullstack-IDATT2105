package edu.ntnu.idatt2105.backend.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class ApiEndpointMappingLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiEndpointMappingLogger.class);

	private static final List<String> INTERESTING_PREFIXES = List.of(
		"/api/checklists",
		"/api/temperature-measurements",
		"/api/auth"
	);

	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	public ApiEndpointMappingLogger(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logMappings() {
		Map<RequestMappingInfo, ?> mappings = requestMappingHandlerMapping.getHandlerMethods();

		long total = mappings.size();
		long interesting = mappings.keySet().stream()
			.flatMap(info -> info.getPatternValues().stream())
			.filter(this::isInteresting)
			.count();

		LOGGER.info("Spring MVC mappings registered: total={} interesting={}", total, interesting);

		mappings.keySet().stream()
			.flatMap(info -> info.getPatternValues().stream())
			.filter(this::isInteresting)
			.sorted()
			.forEach(path -> LOGGER.info("Mapped endpoint: {}", path));
	}

	private boolean isInteresting(String path) {
		if (path == null) return false;
		return INTERESTING_PREFIXES.stream().anyMatch(path::startsWith);
	}
}

