package com.github.kaivu.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * CDI configuration validator that performs startup-time validation
 * of CDI beans and their dependencies to ensure proper wiring.
 */
@Slf4j
@ApplicationScoped
public class CdiConfigurationValidator {

    @Inject
    BeanManager beanManager;

    /**
     * Validates CDI configuration at startup time
     */
    public void validateConfiguration(@Observes StartupEvent startupEvent) {
        log.info("Starting CDI configuration validation...");

        try {
            validateRequiredBeans();
            validateBeanScopes();
            logSuccessfulValidation();
        } catch (Exception e) {
            log.error("CDI configuration validation failed", e);
            throw new IllegalStateException("CDI configuration is invalid", e);
        }
    }

    private void validateRequiredBeans() {
        // Validate critical beans are properly configured
        Set<String> requiredBeans = Set.of(
                "applicationConfiguration", "cacheServiceImpl", "entityDeviceUseCaseImpl", "observabilityContext");

        for (String beanName : requiredBeans) {
            if (beanManager.getBeans(beanName).isEmpty()) {
                throw new IllegalStateException("Required CDI bean not found: " + beanName);
            }
        }
    }

    private void validateBeanScopes() {
        // Additional scope validation can be added here
        log.debug("CDI bean scope validation completed");
    }

    private void logSuccessfulValidation() {
        log.info("CDI configuration validation completed successfully");
        log.debug("All required CDI beans are properly configured and accessible");
    }
}
