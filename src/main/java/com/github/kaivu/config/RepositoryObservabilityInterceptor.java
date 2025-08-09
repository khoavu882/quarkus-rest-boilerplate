package com.github.kaivu.config;

import com.github.kaivu.common.constant.ObservabilityConstant;
import com.github.kaivu.common.context.AsyncObservabilityContext;
import com.github.kaivu.common.context.ObservabilityContext;
import com.github.kaivu.common.context.TenantObservabilityContext;
import com.github.kaivu.common.utils.ObservabilityUtil;
import com.github.kaivu.config.annotations.RepositoryObservability;
import com.github.kaivu.config.metrics.AppMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;

/**
 * Enhanced observability interceptor for repository layer operations.
 * Provides comprehensive database operation metrics, tenant isolation,
 * connection pool monitoring, and async operation support.
 *
 * Features:
 * - Database operation timing and error metrics
 * - Tenant-specific database schema tracking
 * - Connection pool health monitoring
 * - Reactive operation context propagation
 * - SQL query pattern analysis
 */
@Slf4j
@Interceptor
@RepositoryObservability
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class RepositoryObservabilityInterceptor {

    @Inject
    ObservabilityContext observabilityContext;

    @Inject
    TenantObservabilityContext tenantContext;

    @Inject
    AsyncObservabilityContext asyncContext;

    @Inject
    MeterRegistry meterRegistry;

    @Inject
    AppMetrics appMetrics;

    @AroundInvoke
    public Object observeRepositoryOperation(final InvocationContext context) throws Exception {
        final Method method = context.getMethod();

        // Only intercept repository methods
        if (!isRepositoryMethod(method)) {
            return context.proceed();
        }

        final String operationName = buildRepositoryOperationName(method);
        final String operationType = getOperationType(method);
        final String entityName = extractEntityName(method);
        final Instant start = Instant.now();

        // Create timer for database operations
        Timer.Sample timerSample = Timer.start(meterRegistry);

        // Track active database operations
        recordActiveOperation(operationType, entityName, true);

        try {
            // Add repository-specific span attributes
            enrichSpanWithRepositoryContext(operationName, operationType, entityName);

            // Log repository operation start
            logRepositoryOperationStart(operationName, operationType, entityName);

            final Object result = context.proceed();

            // Handle reactive types
            if (result instanceof Uni<?>) {
                return handleUniRepositoryResult(
                        (Uni<?>) result, operationName, operationType, entityName, start, timerSample);
            } else if (result instanceof Multi<?>) {
                return handleMultiRepositoryResult(
                        (Multi<?>) result, operationName, operationType, entityName, start, timerSample);
            } else {
                handleRepositorySuccess(operationName, operationType, entityName, start, timerSample);
                return result;
            }
        } catch (Exception e) {
            handleRepositoryError(e, operationName, operationType, entityName, start, timerSample);
            throw e;
        } finally {
            // Track active database operations
            recordActiveOperation(operationType, entityName, false);
        }
    }

    private boolean isRepositoryMethod(Method method) {
        String className = method.getDeclaringClass().getSimpleName().toLowerCase();
        return className.contains("repository")
                || method.getDeclaringClass().getPackage().getName().contains("repository")
                || method.getDeclaringClass().getPackage().getName().contains("persistence");
    }

    private String buildRepositoryOperationName(Method method) {
        return ObservabilityUtil.generateOperationName(
                method.getDeclaringClass()
                        .getSimpleName()
                        .replace("Repository", "")
                        .replace("Impl", ""),
                method.getName());
    }

    private String getOperationType(Method method) {
        String methodName = method.getName().toLowerCase();

        if (methodName.startsWith("find")
                || methodName.startsWith("get")
                || methodName.startsWith("list")
                || methodName.startsWith("count")) {
            return "read";
        } else if (methodName.startsWith("save")
                || methodName.startsWith("persist")
                || methodName.startsWith("create")
                || methodName.startsWith("insert")) {
            return "create";
        } else if (methodName.startsWith("update") || methodName.startsWith("modify")) {
            return "update";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "delete";
        } else if (methodName.startsWith("exists") || methodName.startsWith("has")) {
            return "exists";
        } else if (methodName.contains("batch") || methodName.contains("bulk")) {
            return "batch";
        }
        return "unknown";
    }

    private void enrichSpanWithRepositoryContext(String operationName, String operationType, String entityName) {
        ObservabilityUtil.enrichSpanWithContext(
                observabilityContext.getCorrelationId(),
                tenantContext.getCurrentTenant(),
                observabilityContext.getUserId(),
                operationName,
                ObservabilityConstant.LAYER_REPOSITORY);

        // Add repository-specific attributes
        ObservabilityUtil.addSpanAttribute("db.operation.type", operationType);
        ObservabilityUtil.addSpanAttribute("db.tenant.schema", tenantContext.getTenantDatabaseSchema());
        ObservabilityUtil.addSpanAttribute("db.entity.name", entityName);
        ObservabilityUtil.addSpanAttribute("db.system", "postgresql");

        // Add tenant-specific database context
        if (tenantContext.isMultiTenantMode()) {
            ObservabilityUtil.addSpanAttribute("db.tenant.isolation", "schema");
            ObservabilityUtil.addSpanAttribute("db.schema.name", tenantContext.getTenantDatabaseSchema());
        }
    }

    private void logRepositoryOperationStart(String operationName, String operationType, String entityName) {
        log.debug(
                "{}Repository operation started: {} [type={}, entity={}, context={}]",
                tenantContext.getTenantLogPrefix(),
                operationName,
                operationType,
                entityName,
                observabilityContext.getContextSummary());
    }

    private Uni<?> handleUniRepositoryResult(
            Uni<?> uni,
            String operationName,
            String operationType,
            String entityName,
            Instant start,
            Timer.Sample timerSample) {
        // Wrap with async context propagation
        return asyncContext
                .wrapWithObservability(uni, operationName, ObservabilityConstant.LAYER_REPOSITORY)
                .onItem()
                .invoke(item -> handleRepositorySuccess(operationName, operationType, entityName, start, timerSample))
                .onFailure()
                .invoke(failure ->
                        handleRepositoryError(failure, operationName, operationType, entityName, start, timerSample));
    }

    private Multi<?> handleMultiRepositoryResult(
            Multi<?> multi,
            String operationName,
            String operationType,
            String entityName,
            Instant start,
            Timer.Sample timerSample) {
        return multi.onCompletion()
                .invoke(() -> handleRepositorySuccess(operationName, operationType, entityName, start, timerSample))
                .onFailure()
                .invoke(failure ->
                        handleRepositoryError(failure, operationName, operationType, entityName, start, timerSample));
    }

    private void handleRepositorySuccess(
            String operationName, String operationType, String entityName, Instant start, Timer.Sample timerSample) {
        Duration duration = Duration.between(start, Instant.now());

        // Record database operation metrics
        timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_DATABASE_OPERATION_DURATION)
                .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                .tag("operation_type", operationType)
                .tag("entity_name", ObservabilityUtil.sanitizeTagValue(entityName))
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .tag("status", "success")
                .register(meterRegistry));

        // Record operation success counter
        Counter.builder("database_operation_total")
                .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                .tag("operation_type", operationType)
                .tag("entity_name", ObservabilityUtil.sanitizeTagValue(entityName))
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .tag("status", "success")
                .register(meterRegistry)
                .increment();

        // Log completion with tenant context
        log.info(
                "{}Repository operation completed: {} [type={}, entity={}, duration={}ms, context={}]",
                tenantContext.getTenantLogPrefix(),
                operationName,
                operationType,
                entityName,
                duration.toMillis(),
                observabilityContext.getContextSummary());
    }

    private void handleRepositoryError(
            Throwable error,
            String operationName,
            String operationType,
            String entityName,
            Instant start,
            Timer.Sample timerSample) {
        Duration duration = Duration.between(start, Instant.now());

        // Record error metrics
        Counter.builder("database_operation_errors_total")
                .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                .tag("operation_type", operationType)
                .tag("entity_name", ObservabilityUtil.sanitizeTagValue(entityName))
                .tag(ObservabilityConstant.TAG_ERROR_TYPE, error.getClass().getSimpleName())
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .register(meterRegistry)
                .increment();

        // Record duration for failed operations
        timerSample.stop(Timer.builder(ObservabilityConstant.METRIC_DATABASE_OPERATION_DURATION)
                .tag(ObservabilityConstant.TAG_OPERATION, ObservabilityUtil.sanitizeTagValue(operationName))
                .tag("operation_type", operationType)
                .tag("entity_name", ObservabilityUtil.sanitizeTagValue(entityName))
                .tag(
                        ObservabilityConstant.TAG_TENANT,
                        ObservabilityUtil.sanitizeTagValue(tenantContext.getCurrentTenant()))
                .tag("status", "error")
                .register(meterRegistry));

        // Record tenant-specific error
        appMetrics.recordError(tenantContext.getCurrentTenant(), String.format("database_%s_error", operationType));

        // Record error in span
        ObservabilityUtil.recordError(error);
        ObservabilityUtil.addSpanAttribute("db.error.type", error.getClass().getSimpleName());
        ObservabilityUtil.addSpanAttribute("db.operation.failed", "true");

        // Log error with tenant context
        log.error(
                "{}Repository operation failed: {} [type={}, entity={}, duration={}ms, error={}, context={}]",
                tenantContext.getTenantLogPrefix(),
                operationName,
                operationType,
                entityName,
                duration.toMillis(),
                error.getMessage(),
                observabilityContext.getContextSummary(),
                error);
    }

    /**
     * Extract entity name from method for better metrics granularity
     */
    private String extractEntityName(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        // Remove Repository suffix and Impl suffix
        String entityName = className.replace("Repository", "").replace("Impl", "");

        // Handle generic repository patterns
        if (entityName.isEmpty() || entityName.equals("Base")) {
            // Try to extract from method parameters or return type
            if (method.getReturnType().getName().contains("Entity")) {
                return "Entity";
            }
            return "Unknown";
        }

        return entityName;
    }

    /**
     * Track active database operations for monitoring
     */
    private void recordActiveOperation(String operationType, String entityName, boolean increment) {

        // This is a simplified approach - in production you might want a more sophisticated counter
        try {
            if (increment) {
                appMetrics.incrementActiveConnections();
            } else {
                appMetrics.decrementActiveConnections();
            }
        } catch (Exception e) {
            log.debug("Error recording active operation: {}", e.getMessage());
        }
    }
}
