package com.github.kaivu.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import io.quarkus.qute.Qute;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.MessageInterpolator;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class AppMessageInterpolator implements MessageInterpolator {

    private static final Set<String> ALLOWED_ATTRIBUTES = Set.of("max", "min", "regexp", "value");

    // Fixed: Replace unsafe ConcurrentHashMap with Caffeine cache to prevent memory leaks
    private static final Cache<String, String> MESSAGE_CACHE = Caffeine.newBuilder()
            .maximumSize(1000) // Prevent unlimited growth
            .expireAfterWrite(Duration.ofHours(1)) // Auto-expire entries
            .recordStats() // Enable monitoring
            .build();

    @jakarta.ws.rs.core.Context
    ContainerRequestContext requestContext;

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, requestContext.getLanguage());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        return MESSAGE_CACHE.get(
                messageTemplate + AppConstant.DOT + locale.toString(),
                key -> processMessage(messageTemplate, context, locale));
    }

    private String processMessage(String messageTemplate, Context context, Locale locale) {
        try {
            String messageBundle =
                    ResourceBundleUtil.getKeyWithResourceBundle(AppConstant.I18N_VALIDATION, locale, messageTemplate);

            Map<String, Object> filteredAttributes =
                    context.getConstraintDescriptor().getAttributes().entrySet().stream()
                            .filter(entry -> ALLOWED_ATTRIBUTES.contains(entry.getKey()))
                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            return Qute.fmt(messageBundle, filteredAttributes);
        } catch (Exception ex) {
            log.warn("Message interpolation failed for template '{}': {}", messageTemplate, ex.getMessage());
            return messageTemplate;
        }
    }
}
